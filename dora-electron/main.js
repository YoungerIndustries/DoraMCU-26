const { app, BrowserWindow, ipcMain, Tray, Menu } = require('electron');
const path = require('path');
const { spawn } = require('child_process');
const fs = require('fs');
const http = require('http');
const https = require('https');

let mainWindow;
let tray;
let gameClientProcess = null;
let gameClientPath = null;
const SERVER_URL = 'http://localhost:8080';

// Find the game client executable
function findGameClient() {
  const possiblePaths = [
    path.join(__dirname, 'resources', 'DoraMCU.Client.exe'),
    path.join(__dirname, '..', 'unity-client', 'Build', 'DoraMCU.Client.exe'),
    path.join(app.getPath('userData'), 'DoraMCU.Client.exe')
  ];

  for (const p of possiblePaths) {
    if (fs.existsSync(p)) {
      return p;
    }
  }
  return null;
}

// Download the game client executable from the server
async function downloadGameClient(url, destPath) {
  return new Promise((resolve, reject) => {
    const client = url.startsWith('https') ? https : http;
    const destDir = path.dirname(destPath);
    if (!fs.existsSync(destDir)) {
      fs.mkdirSync(destDir, { recursive: true });
    }

    sendToRenderer('game-client-deployment-status', { status: 'downloading', message: 'Downloading game client...' });

    const fileStream = fs.createWriteStream(destPath);
    client.get(url, (res) => {
      if (res.statusCode === 404) {
        reject(new Error('Game client executable not found on server'));
        return;
      }
      if (res.statusCode !== 200) {
        reject(new Error(`Download failed with status ${res.statusCode}`));
        return;
      }

      const totalSize = parseInt(res.headers['content-length'], 10) || 0;
      let downloaded = 0;

      res.on('data', (chunk) => {
        downloaded += chunk.length;
        if (totalSize) {
          const percent = Math.round((downloaded / totalSize) * 100);
          sendToRenderer('game-client-deployment-status', {
            status: 'downloading',
            message: `Downloading... ${percent}%`,
            progress: percent
          });
        }
      });

      res.pipe(fileStream);
      fileStream.on('finish', () => {
        fileStream.close();
        sendToRenderer('game-client-deployment-status', { status: 'downloaded', message: 'Game client downloaded' });
        resolve(destPath);
      });
    }).on('error', (err) => {
      fs.unlink(destPath, () => {}); // cleanup
      reject(err);
    });
  });
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    minWidth: 800,
    minHeight: 600,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false
    },
    icon: path.join(__dirname, 'resources', 'icon.ico'),
    title: 'DoraMCU Game Client'
  });

  mainWindow.loadFile('renderer/index.html');

  // Create tray icon
  tray = new Tray(path.join(__dirname, 'resources', 'icon.ico'));
  const contextMenu = Menu.buildFromTemplate([
    { label: 'Show Window', click: () => mainWindow.show() },
    { label: 'Game Client Status', click: () => fetchGameClientStatus() },
    { type: 'separator' },
    { label: 'Stop Game Client', click: () => stopGameClient() },
    { label: 'Quit', click: () => app.quit() }
  ]);
  tray.setToolTip('DoraMCU Game Client');
  tray.setContextMenu(contextMenu);

  tray.on('click', () => {
    if (mainWindow.isVisible()) {
      mainWindow.hide();
    } else {
      mainWindow.show();
    }
  });

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

function startGameClient() {
  gameClientPath = findGameClient();

  if (!gameClientPath) {
    console.warn('Game client executable not found locally, attempting download...');
    sendToRenderer('game-client-deployment-status', { status: 'downloading', message: 'Game client not found, downloading...' });
    const destPath = path.join(app.getPath('userData'), 'DoraMCU.Client.exe');
    downloadGameClient(SERVER_URL + '/api/v1/game-client/download', destPath)
      .then((savedPath) => {
        gameClientPath = savedPath;
        launchGameClient();
      })
      .catch((err) => {
        console.error('Failed to download game client:', err.message);
        sendToRenderer('game-client-deployment-status', { status: 'error', message: 'Download failed: ' + err.message });
      });
    return;
  }

  launchGameClient();
}

function launchGameClient() {
  if (!gameClientPath) {
    sendToRenderer('game-client-deployment-status', { status: 'error', message: 'Game client path not found' });
    return;
  }

  console.log('Starting game client from:', gameClientPath);
  sendToRenderer('game-client-deployment-status', { status: 'starting', message: 'Starting game client...' });
  updateTrayTooltip('Game Client Starting...');

  gameClientProcess = spawn(gameClientPath, [], {
    detached: false,
    stdio: ['pipe', 'pipe', 'pipe']
  });

  gameClientProcess.stdout.on('data', (data) => {
    console.log('[GameClient]', data.toString());
  });

  gameClientProcess.stderr.on('data', (data) => {
    console.error('[GameClient Error]', data.toString());
  });

  gameClientProcess.on('close', (code) => {
    console.log(`[GameClient] Process exited with code ${code}`);
    sendToRenderer('game-client-deployment-status', { status: 'stopped', message: `Game client stopped with code ${code}` });
    updateTrayTooltip('Game Client Stopped');
    gameClientProcess = null;
  });

  // Mark as started after a short delay (Unity client takes time to initialize)
  setTimeout(() => {
    if (gameClientProcess && !gameClientProcess.killed) {
      sendToRenderer('game-client-deployment-status', { status: 'started', message: 'Game client started successfully' });
      updateTrayTooltip('Game Client Running');
    }
  }, 3000);
}

function stopGameClient() {
  if (gameClientProcess) {
    gameClientProcess.kill('SIGTERM');
    sendToRenderer('game-client-deployment-status', { status: 'stopping', message: 'Game client stopping...' });
  }
}

function updateTrayTooltip(text) {
  if (tray) {
    tray.setToolTip(text);
  }
}

function sendToRenderer(channel, data) {
  if (mainWindow) {
    mainWindow.webContents.send(channel, data);
  }
}

async function fetchGameClientStatus() {
  try {
    const data = await new Promise((resolve, reject) => {
      http.get(SERVER_URL + '/api/v1/startup/progress', (res) => {
        let body = '';
        res.on('data', (chunk) => body += chunk);
        res.on('end', () => {
          try {
            resolve(JSON.parse(body));
          } catch (e) {
            reject(e);
          }
        });
      }).on('error', reject);
    });
    sendToRenderer('game-client-status-update', data);
  } catch (error) {
    console.error('Failed to fetch game client status:', error.message);
  }
}

// IPC Handlers
ipcMain.handle('download-and-start-game-client', async (event, serverUrl) => {
  const url = serverUrl || SERVER_URL;
  const destPath = path.join(app.getPath('userData'), 'DoraMCU.Client.exe');

  try {
    if (!findGameClient()) {
      await downloadGameClient(url + '/api/v1/game-client/download', destPath);
      gameClientPath = destPath;
    } else {
      gameClientPath = findGameClient();
    }
    launchGameClient();
    return { success: true };
  } catch (err) {
    sendToRenderer('game-client-deployment-status', { status: 'error', message: err.message });
    return { success: false, error: err.message };
  }
});

ipcMain.handle('start-game-client', () => {
  startGameClient();
  return { success: true };
});

ipcMain.handle('stop-game-client', () => {
  stopGameClient();
  return { success: true };
});

ipcMain.handle('get-game-client-status', async () => {
  await fetchGameClientStatus();
  return { running: gameClientProcess !== null };
});

// App lifecycle
app.whenReady().then(() => {
  createWindow();

  // Auto-start game client
  startGameClient();
});

app.on('window-all-closed', () => {
  stopGameClient();
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});

app.on('before-quit', () => {
  stopGameClient();
});

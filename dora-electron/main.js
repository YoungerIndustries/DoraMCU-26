const { app, BrowserWindow, BrowserView, ipcMain, Tray, Menu, session, dialog } = require('electron');
const path = require('path');
const { spawn } = require('child_process');
const fs = require('fs');
const http = require('http');
const https = require('https');
const net = require('net');

let mainWindow;
let tray;
let gameClientProcess = null;
let gameClientPath = null;

// ─── Music Player (BrowserView) ──────────────────────────────────────────────

let musicPlayerView = null;
let musicPlayerVisible = false;

// Create the music player BrowserView
function createMusicPlayerView(options = {}) {
  if (musicPlayerView) {
    return { success: true };
  }

  const url = options.url || 'https://www.pandora.com';
  const width = options.width || 400;
  const height = options.height || 600;

  musicPlayerView = new BrowserView({
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      webSecurity: false
    }
  });

  // Position the view in the top-right area of the main window
  const bounds = {
    x: mainWindow.getBounds().width - width - 20,
    y: 60, // Below the title bar
    width: width,
    height: height
  };
  musicPlayerView.setBounds(bounds);
  musicPlayerView.webContents.loadURL(url);

  console.log(`[MusicPlayer] BrowserView created, navigating to ${url}`);
  return { success: true };
}

// Show the music player BrowserView
function showMusicPlayerView() {
  if (!musicPlayerView) {
    return { success: false, error: 'Music player not created' };
  }
  mainWindow.setBrowserView(musicPlayerView);
  musicPlayerVisible = true;

  // Restore bounds
  const bounds = musicPlayerView.getBounds();
  musicPlayerView.setBounds(bounds);

  console.log('[MusicPlayer] BrowserView shown');
  return { success: true };
}

// Hide the music player BrowserView
function hideMusicPlayerView() {
  if (!musicPlayerView) {
    return { success: false, error: 'Music player not created' };
  }
  mainWindow.setBrowserView(null);
  musicPlayerVisible = false;
  console.log('[MusicPlayer] BrowserView hidden');
  return { success: true };
}

// Toggle the music player visibility
function toggleMusicPlayerView() {
  if (musicPlayerVisible) {
    return hideMusicPlayerView();
  } else {
    return showMusicPlayerView();
  }
}

// Navigate the music player to a URL
function navigateMusicPlayerView(url) {
  if (!musicPlayerView) {
    return { success: false, error: 'Music player not created' };
  }
  musicPlayerView.webContents.loadURL(url);
  console.log(`[MusicPlayer] Navigated to ${url}`);
  return { success: true };
}

// Reload the music player
function reloadMusicPlayerView() {
  if (!musicPlayerView) {
    return { success: false, error: 'Music player not created' };
  }
  musicPlayerView.webContents.reload();
  console.log('[MusicPlayer] Reloaded');
  return { success: true };
}

// Set volume for the music player (via webContents audio settings)
function setMusicPlayerVolume(volume) {
  if (!musicPlayerView) {
    return { success: false, error: 'Music player not created' };
  }
  // Electron's audioVolume is 0-1
  musicPlayerView.webContents.setAudioVolume(Math.max(0, Math.min(1, volume)));
  console.log(`[MusicPlayer] Volume set to ${Math.round(volume * 100)}%`);
  return { success: true };
}

// Destroy the music player BrowserView
function destroyMusicPlayerView() {
  if (musicPlayerView) {
    if (musicPlayerVisible) {
      mainWindow.setBrowserView(null);
    }
    musicPlayerView.webContents.destroy();
    musicPlayerView = null;
    musicPlayerVisible = false;
    console.log('[MusicPlayer] BrowserView destroyed');
  }
  return { success: true };
}

// ─── Embedded Spring Boot Server ─────────────────────────────────────────────
let springBootProcess = null;
let embeddedServerPort = null;
let embeddedMode = false; // true when Electron manages the Spring Boot process

// Dynamic server URL — set after port allocation
let SERVER_URL = 'http://localhost:8080';

// Track whether the Spring Boot backend is reachable
let serverOnline = false;

// ─── Embedded Spring Boot Server ─────────────────────────────────────────────

// Locate the bundled Spring Boot JAR
function findEmbeddedJar() {
  const searchPaths = [
    // Development: JAR in build/libs next to dora-electron
    path.join(__dirname, '..', 'build', 'libs'),
    // Packaged: inside Electron's resources directory
    path.join(process.resourcesPath, 'server'),
    // Fallback: app userData directory
    path.join(app.getPath('userData'), 'server')
  ];

  for (const dir of searchPaths) {
    try {
      if (fs.existsSync(dir)) {
        const files = fs.readdirSync(dir)
          .filter(f => f.endsWith('.jar')
            && !f.endsWith('.sources.jar')
            && !f.endsWith('.javadoc.jar')
            && !f.includes('-plain.jar')  // Exclude non-executable plain JARs
          )
          // Prefer Spring Boot executable JARs (larger files with launcher manifest)
          .sort((a, b) => {
            try {
              const sizeA = fs.statSync(path.join(dir, a)).size;
              const sizeB = fs.statSync(path.join(dir, b)).size;
              return sizeB - sizeA; // Largest first (Spring Boot JARs are bigger)
            } catch { return 0; }
          });
        if (files.length > 0) {
          const jarFile = files[0];
          console.log(`[DoraMCU] Found embedded JAR: ${path.join(dir, jarFile)}`);
          return path.join(dir, jarFile);
        }
      }
    } catch (err) {
      console.warn(`[DoraMCU] Error searching for JAR in ${dir}:`, err.message);
    }
  }
  return null;
}

// Allocate a free port for the embedded server
function allocatePort() {
  return new Promise((resolve, reject) => {
    const tmpServer = net.createServer();
    tmpServer.listen(0, () => {
      const port = tmpServer.address().port;
      tmpServer.close(() => resolve(port));
    });
    tmpServer.on('error', reject);
  });
}

// Start the embedded Spring Boot server as a child process
async function startEmbeddedServer() {
  const jarPath = findEmbeddedJar();
  if (!jarPath) {
    console.warn('[DoraMCU] No embedded JAR found — running in external server mode');
    return false;
  }

  try {
    embeddedServerPort = await allocatePort();
    SERVER_URL = `http://localhost:${embeddedServerPort}`;
    console.log(`[DoraMCU] Starting embedded Spring Boot server on port ${embeddedServerPort}`);

    sendToRenderer('game-client-deployment-status', {
      status: 'starting',
      message: 'Starting DoraMCU server...'
    });

    springBootProcess = spawn('java', [
      '-Xmx4G',
      '-XX:+UseG1GC',
      '-XX:MaxGCPauseMillis=200',
      `-Dserver.port=${embeddedServerPort}`,
      '-Ddoramcu.launcher.mode=server',
      '-Dspring.profiles.active=dev',
      '-jar',
      jarPath
    ], {
      stdio: ['pipe', 'pipe', 'pipe']
    });

    // Log server output
    springBootProcess.stdout.on('data', (data) => {
      const line = data.toString().trim();
      if (line) console.log(`[Server] ${line}`);
    });
    springBootProcess.stderr.on('data', (data) => {
      const line = data.toString().trim();
      if (line) console.error(`[Server] ${line}`);
    });

    springBootProcess.on('error', (err) => {
      console.error('[DoraMCU] Failed to start Spring Boot process:', err.message);
      sendToRenderer('game-client-deployment-status', {
        status: 'error',
        message: 'Failed to start server: ' + err.message
      });
    });

    springBootProcess.on('exit', (code, signal) => {
      console.log(`[DoraMCU] Spring Boot process exited with code ${code}, signal ${signal}`);
      embeddedMode = false;
      sendToRenderer('game-client-deployment-status', {
        status: 'stopped',
        message: `Server process exited (code: ${code})`
      });
    });

    embeddedMode = true;

    // Wait for the server to be healthy
    return await waitForEmbeddedServer();
  } catch (err) {
    console.error('[DoraMCU] Error starting embedded server:', err);
    return false;
  }
}

// Poll the embedded server health endpoint until it's ready
async function waitForEmbeddedServer() {
  const maxAttempts = 60; // 60 seconds max
  const intervalMs = 1000;

  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    const online = await checkServerHealth();
    if (online) {
      console.log(`[DoraMCU] Embedded server is ready after ${attempt} second(s)`);
      sendToRenderer('game-client-deployment-status', {
        status: 'ready',
        message: 'Server is ready'
      });
      return true;
    }

    if (attempt % 10 === 0) {
      sendToRenderer('game-client-deployment-status', {
        status: 'starting',
        message: `Starting server... (${attempt}s)`
      });
    }

    // Check if the process died
    if (springBootProcess && springBootProcess.killed) {
      console.error('[DoraMCU] Embedded server process died during startup');
      return false;
    }

    await new Promise(resolve => setTimeout(resolve, intervalMs));
  }

  console.error('[DoraMCU] Embedded server failed to start within timeout');
  sendToRenderer('game-client-deployment-status', {
    status: 'error',
    message: 'Server startup timed out after 60 seconds'
  });
  return false;
}

// Gracefully shutdown the embedded server
function stopEmbeddedServer() {
  if (!springBootProcess || !embeddedMode) return;

  console.log('[DoraMCU] Shutting down embedded server...');

  // Try graceful shutdown via actuator endpoint first
  const shutdownUrl = `${SERVER_URL}/actuator/shutdown`;
  http.post(shutdownUrl, {
    method: 'POST',
    hostname: new URL(shutdownUrl).hostname,
    path: new URL(shutdownUrl).pathname
  }).on('error', () => {
    // Actuator shutdown failed, fall back to kill
    console.warn('[DoraMCU] Actuator shutdown failed, killing process');
    if (springBootProcess) {
      springBootProcess.kill('SIGTERM');
      setTimeout(() => {
        if (springBootProcess && !springBootProcess.killed) {
          springBootProcess.kill('SIGKILL');
        }
      }, 5000);
    }
  });

  // Force kill after timeout if graceful shutdown doesn't work
  setTimeout(() => {
    if (springBootProcess && !springBootProcess.killed) {
      console.warn('[DoraMCU] Force killing embedded server process');
      springBootProcess.kill('SIGKILL');
    }
  }, 10000);

  springBootProcess = null;
  embeddedMode = false;
}

// Find the game client executable (validates file size to skip corrupt downloads)
function findGameClient() {
  const possiblePaths = [
    path.join(__dirname, 'resources', 'DoraMCU.Client.exe'),
    path.join(__dirname, '..', 'unity-client', 'Build', 'DoraMCU.Client.exe'),
    path.join(app.getPath('userData'), 'DoraMCU.Client.exe')
  ];

  for (const p of possiblePaths) {
    try {
      if (fs.existsSync(p)) {
        const stats = fs.statSync(p);
        // Skip corrupt or incomplete files (less than 1KB is not a valid executable)
        if (stats.size >= 1024) {
          return p;
        } else {
          console.warn(`Skipping corrupt game client file (${stats.size} bytes): ${p}`);
        }
      }
    } catch (err) {
      console.warn(`Cannot access game client file ${p}:`, err.message);
    }
  }
  return null;
}

// Download the game client executable from the server
async function downloadGameClient(url, destPath, timeoutMs) {
  const timeout = timeoutMs || 120000; // Default 2 minute timeout
  return new Promise((resolve, reject) => {
    const client = url.startsWith('https') ? https : http;
    const destDir = path.dirname(destPath);
    if (!fs.existsSync(destDir)) {
      fs.mkdirSync(destDir, { recursive: true });
    }

    sendToRenderer('game-client-deployment-status', { status: 'downloading', message: 'Downloading game client...' });

    const fileStream = fs.createWriteStream(destPath);
    const timeoutId = setTimeout(() => {
      req.abort();
      fs.unlink(destPath, () => {}); // cleanup partial file
      reject(new Error(`Download timed out after ${timeout / 1000} seconds. The game client binary may not be available on the server.`));
    }, timeout);

    const req = client.get(url, (res) => {
      if (res.statusCode === 404) {
        clearTimeout(timeoutId);
        reject(new Error('Game client executable not found on server'));
        return;
      }
      if (res.statusCode === 403) {
        clearTimeout(timeoutId);
        reject(new Error('Access denied — the game client download requires authentication or the binary has not been uploaded yet.'));
        return;
      }
      if (res.statusCode !== 200) {
        clearTimeout(timeoutId);
        reject(new Error(`Download failed with status ${res.statusCode}`));
        return;
      }

      const totalSize = parseInt(res.headers['content-length'], 10) || 0;
      let downloaded = 0;
      let lastProgress = 0;

      res.on('data', (chunk) => {
        downloaded += chunk.length;
        // Reset timeout on each chunk received (server is still sending data)
        clearTimeout(timeoutId);
        setTimeout(() => {
          req.abort();
          fs.unlink(destPath, () => {});
          reject(new Error(`Download stalled — no data received for ${timeout / 1000} seconds.`));
        }, timeout);

        if (totalSize) {
          const percent = Math.round((downloaded / totalSize) * 100);
          // Report progress every 5% to avoid spam
          if (percent - lastProgress >= 5) {
            lastProgress = percent;
            sendToRenderer('game-client-deployment-status', {
              status: 'downloading',
              message: `Downloading... ${percent}%`,
              progress: percent
            });
          }
        }
      });

      res.pipe(fileStream);
      fileStream.on('finish', () => {
        clearTimeout(timeoutId);
        fileStream.close();
        sendToRenderer('game-client-deployment-status', { status: 'downloaded', message: 'Game client downloaded' });
        resolve(destPath);
      });
    }).on('error', (err) => {
      clearTimeout(timeoutId);
      fs.unlink(destPath, () => {}); // cleanup
      reject(err);
    });
  });
}

// Check if the Spring Boot backend is reachable
function checkServerHealth() {
  return new Promise((resolve) => {
    // Try /actuator/health first, then fall back to root endpoint
    http.get(`${SERVER_URL}/actuator/health`, (res) => {
      let body = '';
      res.on('data', (chunk) => body += chunk);
      res.on('end', () => {
        if (res.statusCode === 200) {
          resolve(true);
        } else {
          // Health endpoint returned non-200; try root endpoint as fallback
          http.get(`${SERVER_URL}/`, (res2) => {
            res2.on('end', () => resolve(res2.statusCode === 200));
          }).on('error', () => resolve(false));
        }
      });
    }).on('error', () => {
      // Health endpoint unavailable; try root endpoint as fallback
      http.get(`${SERVER_URL}/`, (res) => {
        res.on('end', () => resolve(res.statusCode === 200));
      }).on('error', () => resolve(false));
    });
  });
}

// Inject the Electron bridge script into the web UI after it loads
function injectElectronBridge() {
  const bridgePath = path.join(__dirname, 'renderer', 'electron-bridge.js');

  // Inject the bridge script when the DOM is ready
  mainWindow.webContents.on('dom-ready', () => {
    if (fs.existsSync(bridgePath)) {
      const bridgeCode = fs.readFileSync(bridgePath, 'utf-8');
      mainWindow.webContents.executeJavaScript(bridgeCode).catch(err => {
        console.error('Failed to inject Electron bridge:', err.message);
      });
    } else {
      console.error('Electron bridge script not found at:', bridgePath);
    }
  });
}

// Set up session-wide request interception to handle CORS and API routing
function setupSessionIntercept() {
  const filter = { urls: ['http://*/api/*'] };
  session.defaultSession.webRequest.onBeforeSendHeaders(filter, (details, callback) => {
    // Forward auth token from localStorage if available
    callback({ requestHeaders: details.requestHeaders });
  });
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    minWidth: 900,
    minHeight: 700,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      webSecurity: false // Allow loading resources from localhost:8080
    },
    icon: path.join(__dirname, 'resources', 'icon.ico'),
    title: 'DoraMCU Desktop',
    show: false // Hide until ready
  });

  // Set up session intercepts for API routing
  setupSessionIntercept();

  // Inject the Electron bridge into the web UI
  injectElectronBridge();

  // Create the application menu
  const template = [
    {
      label: 'File',
      submenu: [
        { label: 'Reload', accelerator: 'CmdOrCtrl+R', click: () => {
          mainWindow.reload();
        }},
        { type: 'separator' },
        { label: 'Exit', accelerator: 'CmdOrCtrl+Q', click: () => app.quit() }
      ]
    },
    {
      label: 'View',
      submenu: [
        { label: 'Toggle Developer Tools', accelerator: 'F12', click: () => {
          mainWindow.webContents.toggleDevTools();
        }},
        { type: 'separator' },
        { label: 'Toggle Full Screen', role: 'togglefullscreen' }
      ]
    },
    {
      label: 'Game',
      submenu: [
        { label: 'Launch Game Client', click: () => {
          mainWindow.webContents.send('launch-game-client');
        }},
        { label: 'Stop Game Client', click: () => {
          stopGameClient();
        }},
        { type: 'separator' },
        { label: 'Game Client Status', click: () => fetchGameClientStatus() }
      ]
    },
    {
      label: 'Help',
      submenu: [
        { label: 'About', click: () => {
          dialog.showMessageBox(mainWindow, {
            type: 'info',
            title: 'About DoraMCU Desktop',
            message: 'DoraMCU Desktop',
            detail: `Version ${app.getVersion()}\n\nA multi-universe tabletop RPG platform.`,
            buttons: ['OK']
          });
        }}
      ]
    }
  ];
  Menu.setApplicationMenu(Menu.buildFromTemplate(template));

  // Create tray icon
  tray = new Tray(path.join(__dirname, 'resources', 'icon.ico'));
  const contextMenu = Menu.buildFromTemplate([
    { label: 'Show Window', click: () => mainWindow.show() },
    { label: 'Launch Game Client', click: () => {
      mainWindow.webContents.send('launch-game-client');
    }},
    { label: 'Game Client Status', click: () => fetchGameClientStatus() },
    { type: 'separator' },
    { label: 'Stop Game Client', click: () => stopGameClient() },
    { label: 'Quit', click: () => app.quit() }
  ]);
  tray.setToolTip('DoraMCU Desktop');
  tray.setContextMenu(contextMenu);

  tray.on('click', () => {
    if (mainWindow.isVisible()) {
      mainWindow.hide();
    } else {
      mainWindow.show();
    }
  });

  // Check for external web URL configuration (supports dora-web unified entry point)
  const externalWebUrl = process.env.DORAMCU_WEB_URL || app.commandLine.getSwitchValue('web-url');
  
  // Try to start the embedded server first; fall back to external server mode
  (async () => {
    // If external web URL is configured, load it directly (bypasses embedded server)
    if (externalWebUrl) {
      console.log(`[DoraMCU] Loading external web URL: ${externalWebUrl}`);
      SERVER_URL = externalWebUrl;
      serverOnline = true;
      mainWindow.loadURL(externalWebUrl);
      return;
    }
    
    const embeddedStarted = await startEmbeddedServer();
    
    if (embeddedStarted) {
      // Embedded server is running — load the web UI
      serverOnline = true;
      mainWindow.loadURL(SERVER_URL);
    } else {
      // No embedded JAR or startup failed — try external server
      const externalOnline = await checkServerHealth();
      serverOnline = externalOnline;
      if (externalOnline) {
        console.log('[DoraMCU] Connected to external Spring Boot server');
        mainWindow.loadURL(SERVER_URL);
      } else {
        // Fall back to local offline page
        mainWindow.loadFile('renderer/index.html');
        console.warn('[DoraMCU] No server available, loading offline mode');
      }
    }
    // Show window once content is loaded
    mainWindow.once('ready-to-show', () => {
      mainWindow.show();
    });
  })();

  // Handle navigation - if server goes offline, reload to local page
  mainWindow.webContents.on('did-fail-load', async (event, code, desc) => {
    if (!serverOnline || desc.includes('net::')) {
      console.warn('Failed to load web UI, falling back to offline mode:', desc);
      const localPath = path.join(__dirname, 'renderer', 'index.html');
      if (fs.existsSync(localPath)) {
        mainWindow.loadFile('renderer/index.html');
      }
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
        // Verify the downloaded file actually exists before launching
        if (fs.existsSync(savedPath)) {
          gameClientPath = savedPath;
          launchGameClient();
        } else {
          const errMsg = 'Game client download succeeded but file not found at: ' + savedPath;
          console.error(errMsg);
          sendToRenderer('game-client-deployment-status', { status: 'error', message: errMsg });
        }
      })
      .catch((err) => {
        console.error('Failed to download game client:', err.message);
        sendToRenderer('game-client-deployment-status', { status: 'error', message: 'Download failed: ' + err.message });
      });
    return;
  }

  // Verify the found file actually exists and is not corrupt before spawning
  try {
    const stats = fs.statSync(gameClientPath);
    if (stats.size < 1024) {
      // File is corrupt — remove and download fresh
      const errMsg = 'Existing game client file is corrupt (size: ' + stats.size + ' bytes). Removing and downloading fresh...';
      console.warn(errMsg);
      sendToRenderer('game-client-deployment-status', { status: 'downloading', message: errMsg });
      fs.unlinkSync(gameClientPath);
      gameClientPath = null;
      const destPath = path.join(app.getPath('userData'), 'DoraMCU.Client.exe');
      downloadGameClient(SERVER_URL + '/api/v1/game-client/download', destPath)
        .then((savedPath) => {
          if (fs.existsSync(savedPath)) {
            gameClientPath = savedPath;
            launchGameClient();
          } else {
            sendToRenderer('game-client-deployment-status', { status: 'error', message: 'Download succeeded but file not found.' });
          }
        })
        .catch((err) => {
          sendToRenderer('game-client-deployment-status', { status: 'error', message: 'Download failed: ' + err.message });
        });
      return;
    }
  } catch (err) {
    const errMsg = 'Cannot access game client file: ' + err.message;
    console.error(errMsg);
    sendToRenderer('game-client-deployment-status', { status: 'error', message: errMsg });
    return;
  }

  launchGameClient();
}

function launchGameClient() {
  if (!gameClientPath) {
    sendToRenderer('game-client-deployment-status', { status: 'error', message: 'Game client path not found' });
    return;
  }

  // Validate the file is not empty or corrupt
  try {
    const stats = fs.statSync(gameClientPath);
    if (stats.size < 1024) {
      // File is too small to be a valid executable — likely corrupt from a failed download
      const errMsg = 'Game client file is corrupt or incomplete (size: ' + stats.size + ' bytes). Removing and retrying download...';
      console.error(errMsg);
      fs.unlinkSync(gameClientPath);
      gameClientPath = null;
      sendToRenderer('game-client-deployment-status', { status: 'error', message: errMsg });
      // Automatically retry the download
      const destPath = path.join(app.getPath('userData'), 'DoraMCU.Client.exe');
      downloadGameClient(SERVER_URL + '/api/v1/game-client/download', destPath)
        .then((savedPath) => {
          if (fs.existsSync(savedPath)) {
            gameClientPath = savedPath;
            launchGameClient();
          } else {
            sendToRenderer('game-client-deployment-status', { status: 'error', message: 'Retry download succeeded but file not found.' });
          }
        })
        .catch((err) => {
          sendToRenderer('game-client-deployment-status', { status: 'error', message: 'Retry download failed: ' + err.message });
        });
      return;
    }
  } catch (err) {
    const errMsg = 'Cannot access game client file: ' + err.message;
    console.error(errMsg);
    sendToRenderer('game-client-deployment-status', { status: 'error', message: errMsg });
    return;
  }

  console.log('Starting game client from:', gameClientPath);
  sendToRenderer('game-client-deployment-status', { status: 'starting', message: 'Starting game client...' });
  updateTrayTooltip('Game Client Starting...');

  try {
    gameClientProcess = spawn(gameClientPath, [], {
      detached: false,
      stdio: ['pipe', 'pipe', 'pipe']
    });
  } catch (err) {
    const errMsg = 'Failed to start game client: ' + err.message;
    console.error(errMsg);
    sendToRenderer('game-client-deployment-status', { status: 'error', message: errMsg });
    gameClientProcess = null;
    return;
  }

  gameClientProcess.stdout.on('data', (data) => {
    console.log('[GameClient]', data.toString());
  });

  gameClientProcess.stderr.on('data', (data) => {
    console.error('[GameClient Error]', data.toString());
  });

  gameClientProcess.on('error', (err) => {
    console.error('[GameClient Spawn Error]', err.message);
    sendToRenderer('game-client-deployment-status', { status: 'error', message: 'Game client error: ' + err.message });
    gameClientProcess = null;
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
    gameClientPath = findGameClient();
    if (!gameClientPath) {
      sendToRenderer('game-client-deployment-status', { status: 'downloading', message: 'Game client not found locally, downloading...' });
      await downloadGameClient(url + '/api/v1/game-client/download', destPath);
      gameClientPath = destPath;
    }

    // Verify the file actually exists and is not corrupt before trying to spawn
    if (!gameClientPath || !fs.existsSync(gameClientPath)) {
      const errMsg = 'Game client executable not found. The Unity game client has not been built or downloaded yet.';
      sendToRenderer('game-client-deployment-status', { status: 'error', message: errMsg });
      return { success: false, error: errMsg };
    }

    // Double-check file size — download may have produced a corrupt file
    try {
      const stats = fs.statSync(gameClientPath);
      if (stats.size < 1024) {
        const errMsg = 'Downloaded game client is corrupt or incomplete (' + stats.size + ' bytes). The Unity binary has not been uploaded to the server yet.';
        console.error(errMsg);
        fs.unlinkSync(gameClientPath);
        gameClientPath = null;
        sendToRenderer('game-client-deployment-status', { status: 'error', message: errMsg });
        return { success: false, error: errMsg };
      }
    } catch (statErr) {
      const errMsg = 'Cannot access downloaded game client file: ' + statErr.message;
      sendToRenderer('game-client-deployment-status', { status: 'error', message: errMsg });
      return { success: false, error: errMsg };
    }

    launchGameClient();
    return { success: true };
  } catch (err) {
    const errMsg = err.message || 'Failed to download game client';
    sendToRenderer('game-client-deployment-status', { status: 'error', message: errMsg });
    return { success: false, error: errMsg };
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

// Show a native desktop notification
ipcMain.handle('show-notification', async (event, title, body) => {
  const { Notification } = require('electron');
  if (Notification.isSupported()) {
    const notification = new Notification({
      title: title || 'DoraMCU',
      body: body || '',
      icon: path.join(__dirname, 'resources', 'icon.png')
    });
    notification.show();
  }
  return { success: true };
});

// Show a native file open dialog
ipcMain.handle('open-file-dialog', async (event, options) => {
  const result = await dialog.showOpenDialog(mainWindow, options || {});
  return result;
});

// Show a native save dialog
ipcMain.handle('save-file-dialog', async (event, options) => {
  const result = await dialog.showSaveDialog(mainWindow, options || {});
  return result;
});

// Show a message box and return the response
ipcMain.handle('show-message-box', async (event, options) => {
  const result = await dialog.showMessageBox(mainWindow, options || {});
  return result;
});

// Get app info
ipcMain.handle('get-app-info', async () => {
  return {
    version: app.getVersion(),
    platform: process.platform,
    serverUrl: SERVER_URL,
    serverOnline: serverOnline
  };
});

// Check server health from renderer
ipcMain.handle('check-server-health', async () => {
  serverOnline = await checkServerHealth();
  return { online: serverOnline };
});

// Reload the web UI
ipcMain.handle('reload-web-ui', async () => {
  if (mainWindow) {
    mainWindow.reload();
  }
  return { success: true };
});

// ─── Music Player IPC Handlers ───────────────────────────────────────────────

ipcMain.handle('create-music-player', async (event, options) => {
  try {
    return createMusicPlayerView(options);
  } catch (err) {
    return { success: false, error: err.message };
  }
});

ipcMain.handle('show-music-player', async () => {
  try {
    return showMusicPlayerView();
  } catch (err) {
    return { success: false, error: err.message };
  }
});

ipcMain.handle('hide-music-player', async () => {
  try {
    return hideMusicPlayerView();
  } catch (err) {
    return { success: false, error: err.message };
  }
});

ipcMain.handle('toggle-music-player', async () => {
  try {
    return toggleMusicPlayerView();
  } catch (err) {
    return { success: false, error: err.message };
  }
});

ipcMain.handle('navigate-music-player', async (event, url) => {
  try {
    return navigateMusicPlayerView(url);
  } catch (err) {
    return { success: false, error: err.message };
  }
});

ipcMain.handle('reload-music-player', async () => {
  try {
    return reloadMusicPlayerView();
  } catch (err) {
    return { success: false, error: err.message };
  }
});

ipcMain.handle('set-music-player-volume', async (event, volume) => {
  try {
    return setMusicPlayerVolume(volume);
  } catch (err) {
    return { success: false, error: err.message };
  }
});

ipcMain.handle('destroy-music-player', async () => {
  try {
    return destroyMusicPlayerView();
  } catch (err) {
    return { success: false, error: err.message };
  }
});

// App lifecycle
app.whenReady().then(() => {
  createWindow();
  // Game client is launched on-demand by the user via the "Launch Game Client" button.
  // Auto-start is disabled to avoid EPERM file permission errors on Windows when the
  // renderer is not yet ready to receive IPC messages.
});

app.on('window-all-closed', () => {
  stopGameClient();
  stopEmbeddedServer();
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
  stopEmbeddedServer();
});

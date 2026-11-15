const { contextBridge, ipcRenderer } = require('electron');

// Expose protected methods that allow the renderer process to use
// the ipcRenderer without exposing the entire object
contextBridge.exposeInMainWorld('electronAPI', {
  // Game client IPC channels
  downloadAndStartGameClient: (serverUrl) => ipcRenderer.invoke('download-and-start-game-client', serverUrl),
  startGameClient: () => ipcRenderer.invoke('start-game-client'),
  stopGameClient: () => ipcRenderer.invoke('stop-game-client'),
  getGameClientStatus: () => ipcRenderer.invoke('get-game-client-status'),
  onGameClientDeploymentStatus: (callback) => {
    ipcRenderer.on('game-client-deployment-status', (event, data) => callback(data));
  },
  onGameClientStatusUpdate: (callback) => {
    ipcRenderer.on('game-client-status-update', (event, data) => callback(data));
  },

  // Desktop notifications
  showNotification: (title, body) => ipcRenderer.invoke('show-notification', title, body),

  // Native dialogs
  openFileDialog: (options) => ipcRenderer.invoke('open-file-dialog', options),
  saveFileDialog: (options) => ipcRenderer.invoke('save-file-dialog', options),
  showMessageBox: (options) => ipcRenderer.invoke('show-message-box', options),

  // App info
  getAppInfo: () => ipcRenderer.invoke('get-app-info'),
  checkServerHealth: () => ipcRenderer.invoke('check-server-health'),
  reloadWebUI: () => ipcRenderer.invoke('reload-web-ui'),

  // Music player
  createMusicPlayer: (options) => ipcRenderer.invoke('create-music-player', options),
  showMusicPlayer: () => ipcRenderer.invoke('show-music-player'),
  hideMusicPlayer: () => ipcRenderer.invoke('hide-music-player'),
  toggleMusicPlayer: () => ipcRenderer.invoke('toggle-music-player'),
  navigateMusicPlayer: (url) => ipcRenderer.invoke('navigate-music-player', url),
  reloadMusicPlayer: () => ipcRenderer.invoke('reload-music-player'),
  setMusicPlayerVolume: (volume) => ipcRenderer.invoke('set-music-player-volume', volume),
  destroyMusicPlayer: () => ipcRenderer.invoke('destroy-music-player'),

  // Check if running in Electron
  isElectron: true
});

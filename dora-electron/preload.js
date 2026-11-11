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
  }
});

const http = require('http');
const fs = require('fs');
const path = require('path');

const port = 8888;
const projectRoot = path.join(__dirname, '..');

const server = http.createServer((req, res) => {
  // Serve files from the project root
  // URL paths use forward slashes; convert to OS path separators
  let urlPath = req.url.split('?')[0]; // strip query strings
  let filePath = path.join(projectRoot, urlPath);

  // Security: ensure the resolved path is within the project root
  const realFilePath = path.resolve(filePath);
  const realProjectRoot = path.resolve(projectRoot);
  if (!realFilePath.startsWith(realProjectRoot + path.sep) && realFilePath !== realProjectRoot) {
    res.writeHead(403);
    res.end('Forbidden');
    return;
  }

  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(404);
      res.end('Not found: ' + err.message);
      return;
    }
    // Set content type based on extension
    let contentType = 'application/octet-stream';
    if (urlPath.endsWith('.js')) contentType = 'application/javascript';
    else if (urlPath.endsWith('.html')) contentType = 'text/html';
    else if (urlPath.endsWith('.css')) contentType = 'text/css';
    else if (urlPath.endsWith('.json')) contentType = 'application/json';
    else if (urlPath.endsWith('.jar')) contentType = 'application/java-archive';
    res.writeHead(200, {'Content-Type': contentType});
    res.end(data);
  });
});

server.listen(port, '0.0.0.0', () => {
  console.log(`File server running on http://0.0.0.0:${port}`);
  console.log(`Serving from: ${projectRoot}`);
});

const http = require('http');
const fs = require('fs');
const path = require('path');

const PROXY_PORT = 8080;
const API_PORT = 3001;
const STATIC_DIR = '/Users/zoulingxi/projects/bookstore-miniapp/uni/dist/build/h5';

const mimeTypes = {
  '.html': 'text/html',
  '.js': 'application/javascript',
  '.css': 'text/css',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
};

const server = http.createServer((req, res) => {
  // CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // API proxy
  if (req.url.startsWith('/api/') || req.url === '/health') {
    const options = {
      hostname: '127.0.0.1',
      port: API_PORT,
      path: req.url,
      method: req.method,
      headers: { ...req.headers, host: `127.0.0.1:${API_PORT}` },
    };

    const proxyReq = http.request(options, (proxyRes) => {
      res.writeHead(proxyRes.statusCode, proxyRes.headers);
      proxyRes.pipe(res);
    });

    proxyReq.on('error', (err) => {
      console.error('API proxy error:', err.message);
      res.writeHead(502);
      res.end(JSON.stringify({ error: 'Backend unavailable' }));
    });

    req.pipe(proxyReq);
    return;
  }

  // Static files
  let filePath = path.join(STATIC_DIR, req.url === '/' ? 'index.html' : req.url);
  const ext = path.extname(filePath).toLowerCase();
  const contentType = mimeTypes[ext] || 'application/octet-stream';

  fs.readFile(filePath, (err, content) => {
    if (err) {
      if (err.code === 'ENOENT') {
        // SPA fallback
        fs.readFile(path.join(STATIC_DIR, 'index.html'), (err2, content2) => {
          if (err2) {
            res.writeHead(404);
            res.end('Not found');
          } else {
            res.writeHead(200, {
              'Content-Type': 'text/html',
              'Cache-Control': 'no-store, no-cache, must-revalidate'
            });
            res.end(content2);
          }
        });
      } else {
        res.writeHead(500);
        res.end('Server error');
      }
    } else {
      const headers = {
        'Content-Type': contentType,
        'Cache-Control': ext === '.html' ? 'no-store, no-cache, must-revalidate' : 'max-age=0'
      };
      res.writeHead(200, headers);
      res.end(content);
    }
  });
});

server.listen(PROXY_PORT, '0.0.0.0', () => {
  console.log(`Proxy running on http://0.0.0.0:${PROXY_PORT}`);
  console.log(`  Static -> ${STATIC_DIR}`);
  console.log(`  API    -> http://127.0.0.1:${API_PORT}`);
});

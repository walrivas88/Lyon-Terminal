const http = require('http');
const fs = require('fs');
const path = require('path');

const server = http.createServer((req, res) => {
    const urlPath = req.url.split('?')[0];
    console.log(`[HTTP] Request received: ${urlPath}`);

    if (urlPath === '/Lyon_Terminal.apk' || urlPath === '/Android_Terminal.apk' || urlPath === '/download' || urlPath === '/app.apk') {
        const apkName = urlPath.includes('Android') ? 'Android_Terminal.apk' : 'Lyon_Terminal.apk';
        const targetPath = path.join(process.cwd(), apkName);

        if (fs.existsSync(targetPath)) {
            const stat = fs.statSync(targetPath);
            console.log(`[HTTP] Serving APK: ${targetPath} (${stat.size} bytes)`);
            res.writeHead(200, {
                'Content-Type': 'application/vnd.android.package-archive',
                'Content-Length': stat.size,
                'Content-Disposition': `attachment; filename="${apkName}"`,
                'Cache-Control': 'no-cache, no-store, must-revalidate',
                'Access-Control-Allow-Origin': '*'
            });
            const stream = fs.createReadStream(targetPath);
            stream.on('error', (err) => {
                console.error(`[HTTP] Stream error: ${err.message}`);
                if (!res.headersSent) {
                    res.writeHead(500, { 'Content-Type': 'text/plain' });
                    res.end('Error streaming APK file.');
                }
            });
            stream.pipe(res);
            return;
        } else {
            console.error(`[HTTP] APK file not found at: ${targetPath}`);
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            res.end(`APK file not found on server (${apkName}).`);
            return;
        }
    }

    if (urlPath === '/glibc.tar.gz' && fs.existsSync('/app/glibc.tar.gz')) {
        const stat = fs.statSync('/app/glibc.tar.gz');
        res.writeHead(200, {
            'Content-Type': 'application/gzip',
            'Content-Length': stat.size,
            'Content-Disposition': 'attachment; filename="glibc.tar.gz"'
        });
        fs.createReadStream('/app/glibc.tar.gz').pipe(res);
        return;
    }

    if (urlPath === '/opencode-linux-x64.tar.gz' && fs.existsSync('/app/opencode-linux-x64.tar.gz')) {
        const stat = fs.statSync('/app/opencode-linux-x64.tar.gz');
        res.writeHead(200, {
            'Content-Type': 'application/gzip',
            'Content-Length': stat.size,
            'Content-Disposition': 'attachment; filename="opencode-linux-x64.tar.gz"'
        });
        fs.createReadStream('/app/opencode-linux-x64.tar.gz').pipe(res);
        return;
    }

    res.writeHead(200, {'Content-Type': 'text/html; charset=utf-8'});
    const html = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lyon Terminal - Standalone APK Download</title>
    <style>
        * { box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, monospace;
            background: #0f172a;
            color: #f8fafc;
            margin: 0;
            padding: 2rem 1rem;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }
        .card {
            background: #1e293b;
            border: 1px solid #334155;
            border-radius: 16px;
            padding: 2.5rem;
            max-width: 620px;
            width: 100%;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.6);
        }
        h1 { color: #38bdf8; margin: 0 0 0.5rem 0; font-size: 1.8rem; display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; }
        .tag { display: inline-block; padding: 0.2rem 0.6rem; border-radius: 9999px; font-size: 0.75rem; font-weight: bold; background: #ef4444; color: white; }
        .tag-adb { background: #f59e0b; color: #000; }
        .tag-pkg { background: #10b981; color: white; }
        p { color: #94a3b8; line-height: 1.5; font-size: 0.95rem; }
        .btn-primary {
            display: block;
            background: #10b981;
            color: white;
            text-decoration: none;
            text-align: center;
            font-size: 1.15rem;
            font-weight: bold;
            padding: 1rem 1.5rem;
            border-radius: 10px;
            margin: 1.5rem 0 0.75rem 0;
            box-shadow: 0 10px 15px -3px rgba(16, 185, 129, 0.4);
            transition: transform 0.1s, background 0.2s;
        }
        .btn-primary:hover { background: #059669; transform: translateY(-1px); }
        .specs {
            background: #0f172a;
            border: 1px solid #1e293b;
            border-radius: 10px;
            padding: 1.25rem;
            font-family: monospace;
            font-size: 0.85rem;
            line-height: 1.7;
            color: #cbd5e1;
        }
        .highlight { color: #38bdf8; font-weight: bold; }
        .amber { color: #f59e0b; }
        .green { color: #10b981; }
    </style>
</head>
<body>
    <div class="card">
        <h1>⚡ Lyon Terminal <span class="tag">ROOT</span> <span class="tag tag-adb">ADB</span> <span class="tag tag-pkg">PKG</span></h1>
        <p>Standalone Linux Shell Terminal APK for Android 9 up to Android 17+ (ARM64 & x86_64).</p>
        
        <a href="/Lyon_Terminal.apk" class="btn-primary">⬇️ Download Lyon_Terminal.apk (Direct APK)</a>
        
        <div class="specs">
            <span class="highlight">[SYSTEM INFO]</span><br>
            • Package Name: Lyon Terminal<br>
            • Package ID: com.lyon.luxterminal<br>
            • Compatibility: Android 9 (API 28) to Android 17+<br>
            • Toolchain: Python 3, Node.js, Git, OpenCode AI, APT, PRoot<br>
        </div>
    </div>
</body>
</html>`;
    res.end(html);
});

server.listen(3000, '0.0.0.0', () => {
    console.log('Download server running on http://0.0.0.0:3000');
});

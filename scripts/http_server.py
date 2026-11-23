import http.server
import socketserver
import sys
PORT = 8888
DIRECTORY = r"C:\Users\kenba\IdeaProjects\DoraMCU\build\libs"
class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)
with socketserver.TCPServer(("", PORT), Handler) as httpd:
    print(f"Serving on port {PORT}")
    httpd.serve_forever()

#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import socketserver
import json
import pprint

class MyTCPHandler(socketserver.BaseRequestHandler):
    """
    The request handler class for our server.
    """

    def handle(self):
        self.data = self.request.recv(1024).strip()
        print("{} wrote:".format(self.client_address[0]))
        decoded = self.data.decode('utf8')
        json_data = json.loads(decoded)
        pprint.pprint(json_data)
        self.request.sendall(self.data.upper())

if __name__ == "__main__":

    if len(sys.argv) != 2:
        sys.exit(1)

    with socketserver.TCPServer(('', int(sys.argv[1])), MyTCPHandler) as server:
        server.serve_forever()

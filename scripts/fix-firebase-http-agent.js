// Workaround: Node 19+ default keepAlive breaks firebase-tools token refresh (node-fetch@2)
const http = require("http");
const https = require("https");
http.globalAgent = new http.Agent({ keepAlive: false });
https.globalAgent = new https.Agent({ keepAlive: false });

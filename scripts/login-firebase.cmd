@echo off
set "NODE_DIR=C:\Users\anurag.mn\Desktop\Eduapp\.tools\node-v24.17.0-win-x64"
set "PATH=%NODE_DIR%;%PATH%"
set "FIREBASE=C:\Users\anurag.mn\Desktop\Eduapp\.tools\firebase-cli\node_modules\.bin\firebase.cmd"
echo.
echo Firebase login for project eduai-e090e
echo A browser window will open. Sign in with your Google account.
echo.
"%FIREBASE%" login
"%FIREBASE%" use eduai-e090e
"%FIREBASE%" projects:list
echo.
echo Done. Restart Cursor, then check Settings - MCP - firebase.
pause

@echo off
set "NODE_DIR=C:\Users\anurag.mn\Desktop\Eduapp\.tools\node-v24.17.0-win-x64"
set "PATH=%NODE_DIR%;%PATH%"
set "FIREBASE=C:\Users\anurag.mn\Desktop\Eduapp\.tools\firebase-cli\node_modules\.bin\firebase.cmd"
set "TOKEN_FILE=C:\Users\anurag.mn\Desktop\Eduapp\.tools\firebase-ci-token.txt"
set "PROJECT_DIR=C:\Users\anurag.mn\Desktop\Eduapp"

if exist "%TOKEN_FILE%" (
  set /p FIREBASE_TOKEN=<"%TOKEN_FILE%"
)

"%FIREBASE%" experimental:mcp --dir "%PROJECT_DIR%" --only firestore,auth,core

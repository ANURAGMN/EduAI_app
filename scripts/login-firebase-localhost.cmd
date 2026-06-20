@echo off
set "NODE_DIR=C:\Users\anurag.mn\Desktop\Eduapp\.tools\node-v24.17.0-win-x64"
set "PATH=%NODE_DIR%;%PATH%"
set "FIREBASE=C:\Users\anurag.mn\Desktop\Eduapp\.tools\firebase-cli\node_modules\.bin\firebase.cmd"
cd /d "C:\Users\anurag.mn\Desktop\Eduapp"

echo.
echo ============================================================
echo  Firebase login (localhost - easiest on Windows)
echo ============================================================
echo  Browser will open automatically. Sign in with Google.
echo  Do NOT use --no-localhost unless this fails.
echo.
pause

"%FIREBASE%" login --reauth

if errorlevel 1 (
  echo Login failed.
  pause
  exit /b 1
)

"%FIREBASE%" use eduai-e090e
"%FIREBASE%" projects:list
echo.
echo SUCCESS. Restart Cursor, check Settings - MCP - firebase.
pause

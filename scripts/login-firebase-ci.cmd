@echo off
set "NODE_DIR=C:\Users\anurag.mn\Desktop\Eduapp\.tools\node-v24.17.0-win-x64"
set "PATH=%NODE_DIR%;%PATH%"
set "FIREBASE=C:\Users\anurag.mn\Desktop\Eduapp\.tools\firebase-cli\node_modules\.bin\firebase.cmd"
cd /d "C:\Users\anurag.mn\Desktop\Eduapp"

echo.
echo ============================================================
echo  Firebase CI login
echo ============================================================
echo.
echo BEFORE YOU START:
echo   - Close ALL auth.firebase.tools browser tabs
echo   - Keep THIS window open until finished
echo.
echo WHEN BROWSER OPENS:
echo   - Use the NEW link from THIS window only
echo   - Sign in, then click COPY on the success page
echo   - Paste here within 60 seconds
echo.
echo DO NOT paste codes from localhost:9005 URLs
echo.
pause

"%FIREBASE%" login:ci --no-localhost
if errorlevel 1 (
  echo.
  echo Login failed. Try save-firebase-token.cmd with a token from Cloud Shell.
  pause
  exit /b 1
)

echo.
echo Copy the token line above (starts with 1//)
echo.
powershell -ExecutionPolicy Bypass -File "%~dp0save-firebase-token.ps1"
pause

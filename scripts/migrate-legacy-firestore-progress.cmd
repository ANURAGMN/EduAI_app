@echo off
set "NODE_DIR=C:\Users\anurag.mn\Desktop\Eduapp\.tools\node-v24.17.0-win-x64"
set "PATH=%NODE_DIR%;%PATH%"
set "NODE_OPTIONS=--require C:\Users\anurag.mn\Desktop\Eduapp\scripts\fix-firebase-http-agent.js"
cd /d "C:\Users\anurag.mn\Desktop\Eduapp"

if "%1"=="--dry-run" (
  node scripts\migrate-legacy-firestore-progress.js --dry-run --all-users
) else (
  node scripts\migrate-legacy-firestore-progress.js --all-users
)
pause

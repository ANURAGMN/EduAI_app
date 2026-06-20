@echo off
set "NODE_DIR=C:\Users\anurag.mn\Desktop\Eduapp\.tools\node-v24.17.0-win-x64"
set "PATH=%NODE_DIR%;%PATH%"
set "FIREBASE=C:\Users\anurag.mn\Desktop\Eduapp\.tools\firebase-cli\node_modules\.bin\firebase.cmd"
cd /d "C:\Users\anurag.mn\Desktop\Eduapp"

echo.
echo ============================================================
echo  Firebase login --no-localhost  (ONLY if localhost failed)
echo ============================================================
echo.
echo WRONG code (do NOT paste this):
echo   4/0AdkVLPwXgH0mpcbXsFrMJA6F8hvpzw8...
echo   ^^ This is from Google URL - it will ALWAYS fail
echo.
echo RIGHT code (paste this):
echo   Shown ON THE PAGE after login, in a copy box, usually
echo   looks like:  5E367-ABCD-1234  or a UUID
echo   Page title: "Firebase CLI Login Successful"
echo   NOT from the browser address bar!
echo.
pause

"%FIREBASE%" login --reauth --no-localhost

if errorlevel 1 (
  echo.
  echo Failed. Try login-firebase-localhost.cmd instead.
  pause
  exit /b 1
)

"%FIREBASE%" use eduai-e090e
"%FIREBASE%" projects:list
pause

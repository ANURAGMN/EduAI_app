$ErrorActionPreference = "Stop"
$nodeDir = "C:\Users\anurag.mn\Desktop\Eduapp\.tools\node-v24.17.0-win-x64"
$firebase = "C:\Users\anurag.mn\Desktop\Eduapp\.tools\firebase-cli\node_modules\.bin\firebase.cmd"
$tokenFile = "C:\Users\anurag.mn\Desktop\Eduapp\.tools\firebase-ci-token.txt"
$projectDir = "C:\Users\anurag.mn\Desktop\Eduapp"
$httpFix = "C:\Users\anurag.mn\Desktop\Eduapp\scripts\fix-firebase-http-agent.js"

$env:PATH = "$nodeDir;$env:PATH"
$env:NODE_OPTIONS = "--require $httpFix"

if (Test-Path $tokenFile) {
    $env:FIREBASE_TOKEN = (Get-Content $tokenFile -Raw).Trim()
}

& $firebase experimental:mcp --dir $projectDir --only firestore,auth,core

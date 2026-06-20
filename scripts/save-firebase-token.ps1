# Save Firebase CI token and verify it works
$ErrorActionPreference = "Stop"
$nodeDir = Join-Path $PSScriptRoot "..\.tools\node-v24.17.0-win-x64"
$firebase = Join-Path $PSScriptRoot "..\.tools\firebase-cli\node_modules\.bin\firebase.cmd"
$tokenFile = Join-Path $PSScriptRoot "..\.tools\firebase-ci-token.txt"
$httpFix = Join-Path $PSScriptRoot "fix-firebase-http-agent.js"
$env:PATH = "$nodeDir;$env:PATH"
$env:NODE_OPTIONS = "--require $httpFix"

Write-Host ""
Write-Host "Paste your Firebase CI token (starts with 1//), then press Enter:"
Write-Host "Get it from: firebase login:ci --no-localhost  (in Cloud Shell or after login-firebase-ci.cmd)"
Write-Host ""
$token = Read-Host "Token"

$token = $token.Trim()
if (-not $token.StartsWith("1//")) {
    Write-Host "ERROR: Token should start with 1//" -ForegroundColor Red
    exit 1
}

Set-Content -Path $tokenFile -Value $token -NoNewline
Write-Host "Saved to: $tokenFile" -ForegroundColor Green

$env:FIREBASE_TOKEN = $token
Write-Host "Verifying..."
& $firebase projects:list
if ($LASTEXITCODE -eq 0) {
    Write-Host "SUCCESS. Restart Cursor, then check Settings -> MCP -> firebase." -ForegroundColor Green
} else {
    Write-Host "Token saved but verification failed. Check the token is complete (one long line)." -ForegroundColor Yellow
}

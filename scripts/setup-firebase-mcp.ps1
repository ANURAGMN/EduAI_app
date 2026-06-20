# Firebase MCP setup (uses portable Node + local firebase-tools in .tools/)
$nodeDir = Join-Path $PSScriptRoot "..\.tools\node-v24.17.0-win-x64"
$firebase = Join-Path $PSScriptRoot "..\.tools\firebase-cli\node_modules\.bin\firebase.cmd"
$env:PATH = "$nodeDir;$env:PATH"

Write-Host "Node:" (& (Join-Path $nodeDir "node.exe") --version)
Write-Host "Firebase:" (& $firebase --version)
Write-Host "Logging into Firebase (browser will open)..."
& $firebase login

Write-Host "Active project:"
& $firebase use eduai-e090e
& $firebase projects:list

Write-Host "Done. Restart Cursor, then check Settings -> MCP -> firebase (should be green)."

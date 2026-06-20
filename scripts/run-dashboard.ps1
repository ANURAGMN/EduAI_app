# Generate Firestore DAU / retention dashboard
# Usage: .\scripts\run-dashboard.ps1

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$out = Join-Path $root "reports\dashboard.html"

Write-Host "Generating metrics dashboard..."
Push-Location $root
node scripts/metrics-retention-dau.js --html $out
Pop-Location

if (Test-Path $out) {
    Write-Host "`nOpen: $out"
    Start-Process $out
} else {
    Write-Host "Dashboard file not created — check Firebase token in .tools/firebase-ci-token.txt"
    exit 1
}

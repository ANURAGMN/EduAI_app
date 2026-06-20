# Interactive helper — updates AdMob IDs in local.properties (Step 1 of launch checklist).
$propsPath = Join-Path $PSScriptRoot "..\local.properties"
$examplePath = Join-Path $PSScriptRoot "..\local.properties.example"

Write-Host ""
Write-Host "=== AdMob production IDs (Step 1) ===" -ForegroundColor Cyan
Write-Host "1. Open https://admob.google.com"
Write-Host "2. Apps -> Add app -> Android -> com.ncert7.aitutorandlab"
Write-Host "   (or link existing Firebase app eduai-e090e / EduAI)"
Write-Host "3. Ad units -> Add ad unit -> Banner -> name e.g. simulation_banner"
Write-Host "4. Copy App ID (ca-app-pub-...~...) and Banner ad unit ID (ca-app-pub-.../...)"
Write-Host ""

if (-not (Test-Path $propsPath)) {
    if (Test-Path $examplePath) {
        Copy-Item $examplePath $propsPath
        Write-Host "Created local.properties from example." -ForegroundColor Yellow
    } else {
        New-Item -Path $propsPath -ItemType File -Force | Out-Null
    }
}

$current = Get-Content $propsPath -Raw
function Get-Prop($name) {
    if ($current -match "(?m)^$name=(.*)$") { return $matches[1].Trim() }
    return ""
}

$appId = Read-Host "AdMob App ID [current: $(Get-Prop 'ADMOB_APP_ID')]"
$bannerId = Read-Host "Banner ad unit ID [current: $(Get-Prop 'BANNER_AD_UNIT_ID')]"
$testDevice = Read-Host "Test device ID (optional, Enter to skip) [current: $(Get-Prop 'ADMOB_TEST_DEVICE_ID')]"

function Set-Prop($name, $value) {
    if ([string]::IsNullOrWhiteSpace($value)) { return }
    $pattern = "(?m)^$name=.*$"
    $line = "$name=$value"
    if ($current -match $pattern) {
        $script:current = $current -replace $pattern, $line
    } else {
        $script:current = ($current.TrimEnd() + "`n" + $line + "`n")
    }
}

Set-Prop "ADMOB_APP_ID" $appId
Set-Prop "BANNER_AD_UNIT_ID" $bannerId
Set-Prop "ADMOB_TEST_DEVICE_ID" $testDevice

Set-Content -Path $propsPath -Value $current.TrimEnd() -NoNewline
Add-Content -Path $propsPath -Value ""

Write-Host ""
Write-Host "Updated local.properties. Rebuild the app:" -ForegroundColor Green
Write-Host "  .\gradlew.bat assembleDebug"
Write-Host ""

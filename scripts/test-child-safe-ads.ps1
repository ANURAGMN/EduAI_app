# Test child-safe AdMob config + ad gate on device
param(
    [string]$Serial = "",
    [switch]$Debug,
    [string]$ReleaseApk = "c:\Users\anurag.mn\Desktop\Eduapp\app\build\outputs\apk\release\app-release.apk",
    [string]$DebugApk = "c:\Users\anurag.mn\Desktop\Eduapp\app\build\outputs\apk\debug\app-debug.apk"
)

$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$pkg = "com.ncert7.aitutorandlab"
$apk = if ($Debug) { $DebugApk } else { $ReleaseApk }
$shots = "c:\Users\anurag.mn\Desktop\Eduapp\_ad_test_shots"
New-Item -ItemType Directory -Force -Path $shots | Out-Null

function AdbArgs { if ($Serial) { @("-s", $Serial) } else { @() } }

function Shot($name) {
    Start-Sleep -Seconds 2
    & $adb @AdbArgs shell screencap -p "/sdcard/$name.png" | Out-Null
    & $adb @AdbArgs pull "/sdcard/$name.png" "$shots\$name.png" 2>&1 | Out-Null
    Write-Host "  screenshot: $name.png"
}

function TapText($text) {
    & $adb @AdbArgs shell uiautomator dump /sdcard/ui.xml 2>&1 | Out-Null
    & $adb @AdbArgs pull /sdcard/ui.xml "$env:TEMP\ad_ui.xml" 2>&1 | Out-Null
    if (-not (Test-Path "$env:TEMP\ad_ui.xml")) { return $false }
    $raw = Get-Content "$env:TEMP\ad_ui.xml" -Raw
    $escaped = [regex]::Escape($text)
    if ($raw -match "text=`"$escaped`"[^>]*bounds=`"\[(\d+),(\d+)\]\[(\d+),(\d+)\]`"") {
        $x = [int](([int]$matches[1] + [int]$matches[3]) / 2)
        $y = [int](([int]$matches[2] + [int]$matches[4]) / 2)
        & $adb @AdbArgs shell input tap $x $y
        Write-Host "  tapped '$text' at ($x,$y)"
        return $true
    }
    Write-Host "  could not find: $text"
    return $false
}

$devices = & $adb devices | Select-String "device$" | Where-Object { $_ -notmatch "List of" }
if (-not $devices) {
    Write-Host "No device connected. Enable USB debugging and reconnect."
    exit 1
}
if (-not $Serial) {
    $Serial = ($devices[0] -split "\s+")[0]
    Write-Host "Using device: $Serial"
}

Write-Host "`n=== Install $(if ($Debug) { 'debug' } else { 'release' }) APK ==="
& $adb -s $Serial install -r $apk 2>&1

Write-Host "`n=== Launch + check MobileAdsInitializer ==="
& $adb -s $Serial logcat -c | Out-Null
& $adb -s $Serial shell input keyevent KEYCODE_WAKEUP | Out-Null
& $adb -s $Serial shell am force-stop $pkg
Start-Sleep -Seconds 1
& $adb -s $Serial shell monkey -p $pkg -c android.intent.category.LAUNCHER 1 2>&1 | Out-Null
Start-Sleep -Seconds 5

& $adb -s $Serial logcat -d | Select-String -Pattern "MobileAdsInitializer|childDirected|maxRating" | Select-Object -Last 5

Shot "01_launch"

Write-Host "`n=== Sign in if on login screen ==="
if (TapText "Continue with Gmail") {
    Start-Sleep -Seconds 3
    TapText "mail2anuragmn@gmail.com" | Out-Null
    Start-Sleep -Seconds 10
}

Shot "02_home"

Write-Host "`n=== Trigger ad gate (6+ learning clicks) ==="
for ($i = 1; $i -le 7; $i++) {
    TapText "View All Chapters" | Out-Null
    Start-Sleep -Seconds 2
    & $adb -s $Serial shell input keyevent KEYCODE_BACK
    Start-Sleep -Seconds 1
    Write-Host "  click cycle $i"
}

Start-Sleep -Seconds 2
TapText "View All Chapters" | Out-Null
Start-Sleep -Seconds 4
Shot "03_ad_dialog_or_chapters"

Write-Host "`n=== Logcat: ads ==="
& $adb -s $Serial logcat -d -t 200 | Select-String -Pattern "MobileAdsInitializer|ClickAdGate|BannerAdView|AdManager|AdDialog" | Select-Object -Last 20

Write-Host "`nDone. Screenshots: $shots"

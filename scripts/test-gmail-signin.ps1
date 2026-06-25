# Gmail sign-in scenario tests via adb (device required)
param(
    [string]$Serial = "123249b7",
    [string]$Apk = "c:\Users\anurag.mn\Desktop\Eduapp\app\build\outputs\apk\release\app-release.apk"
)

$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$pkg = "com.ncert7.aitutorandlab"
$shots = "c:\Users\anurag.mn\Desktop\Eduapp\_gmail_test_shots"
New-Item -ItemType Directory -Force -Path $shots | Out-Null

function Shot($name) {
    Start-Sleep -Seconds 2
    & $adb -s $Serial shell screencap -p "/sdcard/$name.png" | Out-Null
    & $adb -s $Serial pull "/sdcard/$name.png" "$shots\$name.png" 2>&1 | Out-Null
    Write-Host "  screenshot: $name.png"
}

function Clear-Logcat {
    & $adb -s $Serial logcat -c | Out-Null
}

function Get-GoogleSignInLogs {
    & $adb -s $Serial logcat -d -t 80 | Select-String -Pattern "GoogleSignIn|GoogleLoginButton|GoogleSignInStatus" | Select-Object -Last 15
}

function Tap-UiText($text) {
    & $adb -s $Serial shell uiautomator dump /sdcard/ui.xml 2>&1 | Out-Null
    & $adb -s $Serial pull /sdcard/ui.xml "$env:TEMP\eduai_ui.xml" 2>&1 | Out-Null
    if (-not (Test-Path "$env:TEMP\eduai_ui.xml")) { return $false }
    $raw = Get-Content "$env:TEMP\eduai_ui.xml" -Raw
    $escaped = [regex]::Escape($text)
    if ($raw -match "text=`"$escaped`"[^>]*bounds=`"\[(\d+),(\d+)\]\[(\d+),(\d+)\]`"") {
        $x = [int](([int]$matches[1] + [int]$matches[3]) / 2)
        $y = [int](([int]$matches[2] + [int]$matches[4]) / 2)
        & $adb -s $Serial shell input tap $x $y
        Write-Host "  tapped '$text' at ($x,$y)"
        return $true
    }
    Write-Host "  could not find UI text: $text"
    return $false
}

function Launch-App {
    & $adb -s $Serial shell am force-stop $pkg
    Start-Sleep -Seconds 1
    & $adb -s $Serial shell am start -n "$pkg/.MainActivity" | Out-Null
    Start-Sleep -Seconds 3
}

Write-Host "`n=== Scenario 1: First-time user (fresh app data) ==="
& $adb -s $Serial shell pm clear $pkg 2>&1 | Out-Null
Clear-Logcat
Launch-App
Shot "s1_login_fresh"
Tap-UiText "Continue with Gmail" | Out-Null
Start-Sleep -Seconds 2
Shot "s1_after_gmail_tap"
Write-Host "  logs:"
Get-GoogleSignInLogs | ForEach-Object { Write-Host "    $_" }

Write-Host "`n=== Scenario 2: Cancel Google picker (Back), return to login ==="
& $adb -s $Serial shell input keyevent KEYCODE_BACK
Start-Sleep -Seconds 2
Shot "s2_after_cancel_back"
Write-Host "  logs:"
Get-GoogleSignInLogs | ForEach-Object { Write-Host "    $_" }

Write-Host "`n=== Scenario 3: Tap Gmail again after cancel ==="
Clear-Logcat
Tap-UiText "Continue with Gmail" | Out-Null
Start-Sleep -Seconds 2
Shot "s3_gmail_tap_again"
Write-Host "  logs:"
Get-GoogleSignInLogs | ForEach-Object { Write-Host "    $_" }

Write-Host "`n=== Scenario 4: Second open (app data kept, still logged out) ==="
& $adb -s $Serial shell input keyevent KEYCODE_BACK
Start-Sleep -Seconds 1
Launch-App
Shot "s4_relaunch_login"
Tap-UiText "Continue with Gmail" | Out-Null
Start-Sleep -Seconds 2
Shot "s4_gmail_picker_relaunch"
Write-Host "  logs:"
Get-GoogleSignInLogs | ForEach-Object { Write-Host "    $_" }

Write-Host "`nDone. Screenshots in: $shots"

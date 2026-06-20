# EduAI

NCERT Class 7 learning app — AI tutor, math agent, HTML simulations, progress tracking, and AdMob monetization.

**Package:** `com.ncert7.aitutorandlab`  
**Firebase:** `eduai-e090e`

## Documentation

| Doc | Description |
|-----|-------------|
| [**App structure & key items**](docs/APP_STRUCTURE.md) | Architecture, packages, analytics, ads, Firestore paths, team setup |
| [AdMob + Firebase setup](scripts/admob-firebase-setup.md) | Console linking & payments checklist |

## Quick start

```powershell
# 1. Copy secrets template
copy local.properties.example local.properties
# 2. Add app/google-services.json from Firebase Console
# 3. Verify config
.\scripts\verify-admob-config.ps1
# 4. Build
.\gradlew.bat assembleDebug
```

## Metrics

```powershell
node scripts/query-firestore-analytics.js <email>
node scripts/metrics-retention-dau.js --html reports/dashboard.html
```

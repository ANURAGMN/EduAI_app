# AdMob + Firebase setup checklist (EduAI)

App: **com.ncert7.aitutorandlab**  
Firebase project: **eduai-e090e**  
Publisher ID: **6484226294015492** (from your production AdMob IDs)

Run local verification first:

```powershell
.\scripts\verify-admob-config.ps1
```

---

## Step 1 — Link AdMob app to Firebase

Do **one** of these (both achieve the same link):

### Option A: From Firebase
1. Open [Firebase Console](https://console.firebase.google.com/) → **eduai-e090e**
2. **Project settings** (gear) → **Integrations** tab
3. Find **Google AdMob** → **Link**
4. Select your AdMob app for **EduAI** / package `com.ncert7.aitutorandlab`

### Option B: From AdMob
1. Open [AdMob Console](https://apps.admob.com/)
2. **Apps** → select EduAI Android app
3. **App settings** → **Link to Firebase project**
4. Choose **eduai-e090e**

**Verify:** In Firebase → Analytics → **Monetization**, you should eventually see ad revenue (may take 24–48 hours after first impressions).

---

## Step 2 — Confirm ad units match the app

In AdMob → **Apps** → EduAI → **Ad units**:

| Setting | Expected value |
|---------|----------------|
| Format | Banner |
| App ID | `ca-app-pub-6484226294015492~5849133177` |
| Banner unit | `ca-app-pub-6484226294015492/9077776218` |

These must match `local.properties`:

```properties
ADMOB_APP_ID=ca-app-pub-6484226294015492~5849133177
BANNER_AD_UNIT_ID=ca-app-pub-6484226294015492/9077776218
```

---

## Step 3 — Complete payments (required to receive earnings)

1. AdMob → **Payments** → **Manage settings**
2. Complete **Payments profile** (name, address)
3. Add **Tax information** (W-8/W-9 as applicable)
4. Add **Payment method** (bank transfer — India: wire/EFT per Google’s instructions)
5. Set **Payment threshold** (default $100)

Until this is done, ads can still serve in test/production, but **payouts are blocked**.

---

## Step 4 — Policy & app-ads.txt (required for Class 7 / child audience)

**Play rejected ads when child-safe settings were missing.** Code now sets:
- `TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE`
- `TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE`
- `MAX_AD_CONTENT_RATING_G` (General audiences only)

### AdMob console (do before resubmit)

1. [AdMob](https://apps.admob.com/) → **Apps** → EduAI → **App settings**
2. Set **Child-directed** / COPPA treatment to **Yes** (or match your Play target audience)
3. **Blocking controls** → **Block sensitive categories** — enable blocks for:
   - Dating, Mature, Gambling, Get rich quick, Politics (recommended)
4. **Policy center** — resolve any active violations

### Play Console (do before resubmit)

1. **Policy** → **App content** → **Target audience** — if users under 13, declare accurately (Families policy applies)
2. **Policy** → **App content** → **Ads** — declare that app contains ads
3. **Policy** → **App content** → **Content ratings** → **Start questionnaire** (or **Update**) — answer ad-related questions consistently with child-directed ads
4. If rated **Everyone** / **PEGI 3** / **USK 0**, ads must be G-rated only (matches code above)

### app-ads.txt

1. Play Console → link app when published
2. Host `app-ads.txt` on your developer website domain if required

---

## Step 5 — Test vs production

| Build | Ad IDs | Test device |
|-------|--------|-------------|
| Debug | Production IDs OK if test device configured | Set `ADMOB_TEST_DEVICE_ID` in local.properties |
| Release | Must use production IDs | No test device flag |

Logcat filters:
- `MobileAdsInitializer` — confirms test vs production IDs
- `AdManager` — load / impression / click
- `ClickAdGate` — ad shown after 5 clicks/day

---

## Step 6 — Post-launch monitoring

| Tool | Use |
|------|-----|
| AdMob → Reports | Impressions, eCPM, revenue |
| Firebase → Analytics → Monetization | Ad revenue linked to Firebase events |
| `node scripts/query-firestore-analytics.js` | Per-user click/simulation events |
| `node scripts/metrics-retention-dau.js --html` | DAU / retention dashboard |

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Sample ads only | Wrong unit ID or test device not set |
| No fill / error code 3 | New unit — wait 24h; check app approved in AdMob |
| Firebase Monetization empty | Link not complete; wait 24–48h after impressions |
| Ad never shows in app | Check `ClickAdGate` — first 5 clicks/day are ad-free |

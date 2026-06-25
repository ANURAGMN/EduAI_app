process.env.NODE_OPTIONS = require("path").join(__dirname, "fix-firebase-http-agent.js");
require("./fix-firebase-http-agent.js");

const fs = require("fs");
const path = require("path");

process.env.FIREBASE_TOKEN = fs
  .readFileSync(path.join(__dirname, "../.tools/firebase-ci-token.txt"), "utf8")
  .trim();

const auth = require("../.tools/firebase-cli/node_modules/firebase-tools/lib/auth");
const { Client } = require("../.tools/firebase-cli/node_modules/firebase-tools/lib/apiv2");
auth.setRefreshToken(process.env.FIREBASE_TOKEN);

const PROJECT = "eduai-e090e";
const APP_NAME = "eduai_app";

function fv(f, key) {
  const v = f?.[key];
  if (!v) return null;
  if (v.stringValue != null) return v.stringValue;
  if (v.integerValue != null) return Number(v.integerValue);
  if (v.doubleValue != null) return Number(v.doubleValue);
  return null;
}

function parseToMs(value) {
  if (value == null) return null;
  if (typeof value === "number") return value;
  if (typeof value === "string") {
    if (/^\d{4}-\d{2}-\d{2}$/.test(value)) return Date.parse(value + "T12:00:00.000Z");
    const n = Date.parse(value);
    return Number.isNaN(n) ? null : n;
  }
  return null;
}

function toDayUTC(ms) {
  if (ms == null) return null;
  return new Date(ms).toISOString().slice(0, 10);
}

function toDayIST(ms) {
  if (ms == null) return null;
  return new Date(ms + 5.5 * 3600000).toISOString().slice(0, 10);
}

function emailFromContainer(containerId) {
  return containerId.replace(`${APP_NAME}_`, "");
}

function containerIdFromEmail(email) {
  return `${APP_NAME}_${email}`;
}

async function listTopLevel(client, collection) {
  const parent = `projects/${PROJECT}/databases/(default)/documents/${collection}`;
  let pageToken;
  const ids = [];
  do {
    const res = await client.request({
      method: "GET",
      path: `/${parent}`,
      queryParams: { pageSize: "100", showMissing: "true", ...(pageToken ? { pageToken } : {}) },
    });
    for (const d of res.body.documents || []) {
      const id = d.name.split("/").pop();
      if (id.startsWith(`${APP_NAME}_`)) ids.push(id);
    }
    pageToken = res.body.nextPageToken;
  } while (pageToken);
  return ids;
}

async function listSubRecords(client, parentDoc, sub) {
  const base = `projects/${PROJECT}/databases/(default)/documents/${parentDoc}/${sub}`;
  let pageToken;
  const docs = [];
  do {
    const res = await client.request({
      method: "GET",
      path: `/${base}`,
      queryParams: { pageSize: "100", showMissing: "true", ...(pageToken ? { pageToken } : {}) },
    });
    docs.push(...(res.body.documents || []));
    pageToken = res.body.nextPageToken;
  } while (pageToken);
  return docs;
}

async function listUsers(client) {
  const docs = await listTopLevel(client, "users").then(async () => {
    let pageToken;
    const out = [];
    const parent = `projects/${PROJECT}/databases/(default)/documents/users`;
    do {
      const res = await client.request({
        method: "GET",
        path: `/${parent}`,
        queryParams: { pageSize: "100", ...(pageToken ? { pageToken } : {}) },
      });
      out.push(...(res.body.documents || []));
      pageToken = res.body.nextPageToken;
    } while (pageToken);
    return out;
  });
  return docs.filter((d) => fv(d.fields, "appName") === APP_NAME);
}

function addDayActivity(map, email, day) {
  if (!day) return;
  if (!map.has(email)) map.set(email, new Set());
  map.get(email).add(day);
}

function dauFromActivity(activityByUser) {
  const dau = new Map();
  for (const [email, days] of activityByUser) {
    for (const day of days) {
      if (!dau.has(day)) dau.set(day, new Set());
      dau.get(day).add(email);
    }
  }
  return dau;
}

function mergeActivity(...maps) {
  const combined = new Map();
  for (const m of maps) {
    for (const [email, days] of m) {
      if (!combined.has(email)) combined.set(email, new Set());
      for (const d of days) combined.get(email).add(d);
    }
  }
  return combined;
}

function cohortRetention(cohortEmails, cohortDay, activity, dayFn, offsets) {
  const result = {};
  const d0 = Date.parse(cohortDay + "T00:00:00.000Z");
  for (const offset of offsets) {
    const target =
      dayFn === toDayUTC
        ? new Date(d0 + offset * 86400000).toISOString().slice(0, 10)
        : toDayIST(d0 + offset * 86400000);
    const active = cohortEmails.filter((e) => activity.get(e)?.has(target)).length;
    result[`D${offset}`] = { active, pct: cohortEmails.length ? (active / cohortEmails.length) * 100 : 0 };
  }
  return result;
}

function escapeHtml(s) {
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}

function renderHtml(report) {
  const dauRows = report.dauDays
    .map(
      (d) =>
        `<tr><td>${d.day}</td><td>${d.users}</td><td>${d.sessions}</td><td>${d.clicks}</td></tr>`
    )
    .join("");
  const cohortRows = report.cohorts
    .map(
      (c) =>
        `<tr><td>${c.day}</td><td>${c.n}</td><td>${c.d1}</td><td>${c.d7}</td><td>${c.d30}</td></tr>`
    )
    .join("");
  const clickRows = report.topClickTypes
    .map(([type, count]) => `<tr><td>${escapeHtml(type)}</td><td>${count}</td></tr>`)
    .join("");

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <title>EduAI Metrics — ${report.generatedAt}</title>
  <style>
    body { font-family: system-ui, sans-serif; margin: 24px; background: #f6f7fb; color: #1a1a2e; }
    h1 { margin-bottom: 4px; }
    .sub { color: #666; margin-bottom: 24px; }
    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; margin-bottom: 28px; }
    .card { background: #fff; border-radius: 10px; padding: 16px; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
    .card .val { font-size: 28px; font-weight: 700; }
    .card .lbl { font-size: 13px; color: #666; margin-top: 4px; }
    table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,.08); margin-bottom: 28px; }
    th, td { padding: 10px 14px; text-align: left; border-bottom: 1px solid #eee; }
    th { background: #eef1ff; font-size: 13px; }
    h2 { margin-top: 32px; font-size: 18px; }
    .note { font-size: 13px; color: #555; max-width: 720px; line-height: 1.5; }
  </style>
</head>
<body>
  <h1>EduAI Metrics Dashboard</h1>
  <p class="sub">Generated ${report.generatedAt} · Timezone: ${report.tz} · Firebase ${PROJECT}</p>

  <div class="grid">
    <div class="card"><div class="val">${report.totalUsers}</div><div class="lbl">Registered users</div></div>
    <div class="card"><div class="val">${report.activeUsers}</div><div class="lbl">Users with activity</div></div>
    <div class="card"><div class="val">${report.todayDau}</div><div class="lbl">DAU today</div></div>
    <div class="card"><div class="val">${report.totalClicks}</div><div class="lbl">Analytics clicks (all time)</div></div>
    <div class="card"><div class="val">${report.totalSessions}</div><div class="lbl">Session records</div></div>
  </div>

  <h2>DAU — last ${report.dauDays.length} days</h2>
  <p class="note">Unique users per day from sessions + progress + analytics events.</p>
  <table>
    <thead><tr><th>Day</th><th>Unique users</th><th>Session records</th><th>Click events</th></tr></thead>
    <tbody>${dauRows || "<tr><td colspan='4'>No data</td></tr>"}</tbody>
  </table>

  <h2>Retention by signup cohort</h2>
  <table>
    <thead><tr><th>Cohort (signup day)</th><th>Users</th><th>D1</th><th>D7</th><th>D30</th></tr></thead>
    <tbody>${cohortRows || "<tr><td colspan='5'>No cohorts</td></tr>"}</tbody>
  </table>

  <h2>Top click types (analytics)</h2>
  <table>
    <thead><tr><th>interactionType / screen</th><th>Count</th></tr></thead>
    <tbody>${clickRows || "<tr><td colspan='2'>No clicks</td></tr>"}</tbody>
  </table>

  <p class="note">Re-run: <code>node scripts/metrics-retention-dau.js --html reports/dashboard.html</code></p>
</body>
</html>`;
}

async function main() {
  const args = process.argv.slice(2);
  const tz = args.includes("--utc") ? "UTC" : "IST";
  const dayFn = tz === "UTC" ? toDayUTC : toDayIST;
  const htmlIdx = args.indexOf("--html");
  const htmlOut =
    htmlIdx >= 0 ? args[htmlIdx + 1] : path.join(__dirname, "../reports/dashboard.html");
  const daysArg = args.find((a) => a.startsWith("--days="));
  const dauWindow = daysArg ? Number(daysArg.split("=")[1]) : 14;

  const client = new Client({
    urlPrefix: "https://firestore.googleapis.com",
    apiVersion: "v1",
  });

  console.log(`=== EduAI metrics (${tz}) ===\n`);

  const users = await listUsers(client);
  const cohortByDay = {};
  for (const u of users) {
    const email = fv(u.fields, "email") || u.name.split("/").pop();
    const created = parseToMs(fv(u.fields, "createdAt"));
    const day = dayFn(created);
    if (!day) continue;
    if (!cohortByDay[day]) cohortByDay[day] = [];
    cohortByDay[day].push(email);
  }

  const sessionActivity = new Map();
  let sessionCount = 0;
  const sessionsByDay = new Map();
  for (const container of await listTopLevel(client, "sessions")) {
    const email = emailFromContainer(container);
    for (const s of await listSubRecords(client, `sessions/${container}`, "records")) {
      sessionCount++;
      const startMs =
        parseToMs(fv(s.fields, "sessionStartTime")) || parseToMs(fv(s.fields, "sessionDate"));
      const day = dayFn(startMs);
      addDayActivity(sessionActivity, email, day);
      if (day) sessionsByDay.set(day, (sessionsByDay.get(day) || 0) + 1);
    }
  }

  const progressActivity = new Map();
  for (const container of await listTopLevel(client, "progress")) {
    const email = emailFromContainer(container);
    for (const p of await listSubRecords(client, `progress/${container}`, "records")) {
      const ts = parseToMs(
        fv(p.fields, "lastAccessedAt") || fv(p.fields, "updatedAt") || fv(p.fields, "completedAt")
      );
      addDayActivity(progressActivity, email, dayFn(ts));
    }
  }

  const analyticsActivity = new Map();
  const clicksByDay = new Map();
  const clickTypeCounts = new Map();
  const funnelCounts = new Map();
  let totalClicks = 0;
  let totalFunnel = 0;
  for (const container of await listTopLevel(client, "analytics")) {
    const email = emailFromContainer(container);
    for (const e of await listSubRecords(client, `analytics/${container}`, "events")) {
      const f = e.fields || {};
      const eventType = fv(f, "eventType");
      const entryMs = parseToMs(fv(f, "entryTime"));
      const day = dayFn(entryMs);
      if (day) addDayActivity(analyticsActivity, email, day);
      if (eventType === "CLICK") {
        totalClicks++;
        const type = `${fv(f, "screenName")}/${fv(f, "interactionType") || "?"}`;
        clickTypeCounts.set(type, (clickTypeCounts.get(type) || 0) + 1);
        if (day) clicksByDay.set(day, (clicksByDay.get(day) || 0) + 1);
      }
      if (eventType === "FUNNEL") {
        totalFunnel++;
        const step = fv(f, "interactionType") || fv(f, "conceptId") || "?";
        funnelCounts.set(step, (funnelCounts.get(step) || 0) + 1);
      }
    }
  }

  const combined = mergeActivity(sessionActivity, progressActivity, analyticsActivity);
  const dau = dauFromActivity(combined);
  const sortedDays = [...dau.keys()].sort();
  const recentDays = sortedDays.slice(-dauWindow);
  const todayStr = dayFn(Date.now());

  console.log(`Users: ${users.length} | Session records: ${sessionCount} | Analytics clicks: ${totalClicks} | Funnel steps: ${totalFunnel}`);
  console.log(`Users with activity: ${combined.size}\n`);

  console.log(`--- DAU (last ${dauWindow} days) ---`);
  const dauDays = [];
  for (const day of recentDays) {
    const usersN = dau.get(day).size;
    const sess = sessionsByDay.get(day) || 0;
    const clicks = clicksByDay.get(day) || 0;
    console.log(`${day}: ${usersN} users | sessions=${sess} | clicks=${clicks}`);
    dauDays.push({ day, users: usersN, sessions: sess, clicks });
  }

  console.log("\n--- Retention by signup cohort ---");
  const cohorts = [];
  for (const cohortDay of Object.keys(cohortByDay).sort().slice(-15)) {
    const emails = cohortByDay[cohortDay];
    const ret = cohortRetention(emails, cohortDay, combined, dayFn, [1, 7, 30]);
    const fmt = (k) => `${ret[k].pct.toFixed(1)}% (${ret[k].active})`;
    console.log(`${cohortDay} n=${emails.length} | D1 ${fmt("D1")} | D7 ${fmt("D7")} | D30 ${fmt("D30")}`);
    cohorts.push({
      day: cohortDay,
      n: emails.length,
      d1: fmt("D1"),
      d7: fmt("D7"),
      d30: fmt("D30"),
    });
  }

  console.log("\n--- Top click types ---");
  const topClickTypes = [...clickTypeCounts.entries()].sort((a, b) => b[1] - a[1]).slice(0, 12);
  for (const [type, count] of topClickTypes) {
    console.log(`  ${type}: ${count}`);
  }

  console.log("\n--- Funnel steps (all users) ---");
  const funnelSteps = [...funnelCounts.entries()].sort((a, b) => a[0].localeCompare(b[0]));
  for (const [step, count] of funnelSteps) {
    console.log(`  ${step}: ${count}`);
  }

  if (args.includes("--html") || args.includes("--html-only")) {
    const report = {
      generatedAt: new Date().toISOString(),
      tz,
      totalUsers: users.length,
      activeUsers: combined.size,
      todayDau: dau.get(todayStr)?.size || 0,
      totalClicks,
      totalFunnel,
      totalSessions: sessionCount,
      dauDays,
      cohorts,
      topClickTypes,
      funnelSteps,
    };
    const outPath = path.resolve(htmlOut);
    fs.mkdirSync(path.dirname(outPath), { recursive: true });
    fs.writeFileSync(outPath, renderHtml(report), "utf8");
    console.log(`\nHTML dashboard written: ${outPath}`);
  }
}

main().catch((e) => {
  console.error("Error:", e.message);
  process.exit(1);
});

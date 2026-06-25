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

function fieldVal(f, key) {
  const v = f[key];
  if (!v) return null;
  if (v.stringValue != null) return v.stringValue;
  if (v.integerValue != null) return Number(v.integerValue);
  if (v.doubleValue != null) return Number(v.doubleValue);
  return null;
}

function docName(fullPath) {
  return fullPath.split("/").pop();
}

async function listEvents(email) {
  const docId = `eduai_app_${email}`;
  const parent = `projects/eduai-e090e/databases/(default)/documents/analytics/${docId}/events`;

  const client = new Client({
    urlPrefix: "https://firestore.googleapis.com",
    apiVersion: "v1",
  });

  let pageToken;
  const docs = [];
  do {
    const res = await client.request({
      method: "GET",
      path: `/${parent}`,
      queryParams: { pageSize: "100", ...(pageToken ? { pageToken } : {}) },
    });
    docs.push(...(res.body.documents || []));
    pageToken = res.body.nextPageToken;
  } while (pageToken);

  return docs.map((doc) => {
    const f = doc.fields || {};
    return {
      id: docName(doc.name),
      screenName: fieldVal(f, "screenName"),
      eventType: fieldVal(f, "eventType"),
      conceptId: fieldVal(f, "conceptId"),
      source: fieldVal(f, "source"),
      interactionType: fieldVal(f, "interactionType"),
      entryTime: fieldVal(f, "entryTime"),
      exitTime: fieldVal(f, "exitTime"),
      durationMillis: fieldVal(f, "durationMillis"),
      syncedAt: fieldVal(f, "syncedAt"),
    };
  });
}

async function main() {
  const email = process.argv[2] || "mail2anuragmn@gmail.com";
  const events = await listEvents(email);
  events.sort((a, b) => (b.entryTime || 0) - (a.entryTime || 0));

  const clicks = events.filter((e) => e.eventType === "CLICK");
  const funnel = events.filter((e) => e.eventType === "FUNNEL");
  const contentClicks = events.filter(
    (e) => e.screenName === "CONTENT" && e.eventType === "CLICK"
  );
  const simClicks = events.filter(
    (e) => e.screenName === "SIMULATION" && e.eventType === "CLICK"
  );
  const viewerExits = events.filter(
    (e) => e.screenName === "SIMULATION_VIEWER" && e.eventType === "EXIT"
  );
  const completes = events.filter((e) => e.eventType === "COMPLETE");

  console.log(`\n=== Analytics for ${email} ===`);
  console.log(`Total events: ${events.length}`);
  console.log(`CLICK events: ${clicks.length} (content=${contentClicks.length}, simulation=${simClicks.length})`);
  console.log(`FUNNEL events: ${funnel.length}`);
  if (funnel.length > 0) {
    const funnelCounts = {};
    for (const e of funnel) {
      const step = e.interactionType || e.conceptId || "?";
      funnelCounts[step] = (funnelCounts[step] || 0) + 1;
    }
    console.log("Funnel breakdown:", funnelCounts);
  }
  console.log(`SIMULATION_VIEWER exits: ${viewerExits.length}`);
  console.log(`COMPLETE events: ${completes.length}`);

  console.log("\n--- Latest 10 events ---");
  for (const e of events.slice(0, 10)) {
    const when = e.entryTime ? new Date(e.entryTime).toISOString() : "?";
    const duration =
      e.durationMillis != null ? `${Math.round(e.durationMillis / 1000)}s` : "-";
    console.log(
      `[${when}] ${e.screenName}/${e.eventType}` +
        (e.conceptId ? ` concept=${e.conceptId}` : "") +
        (e.source ? ` source=${e.source}` : "") +
        (e.interactionType ? ` type=${e.interactionType}` : "") +
        (e.eventType === "EXIT" ? ` duration=${duration}` : "")
    );
  }
}

main().catch((err) => {
  console.error(err.message || err);
  process.exit(1);
});

process.env.NODE_OPTIONS = require("path").join(__dirname, "fix-firebase-http-agent.js");
require("./fix-firebase-http-agent.js");

const fs = require("fs");
const path = require("path");

process.env.FIREBASE_TOKEN = fs.readFileSync(
  path.join(__dirname, "../.tools/firebase-ci-token.txt"),
  "utf8"
).trim();

const auth = require("../.tools/firebase-cli/node_modules/firebase-tools/lib/auth");
const { Client } = require("../.tools/firebase-cli/node_modules/firebase-tools/lib/apiv2");
auth.setRefreshToken(process.env.FIREBASE_TOKEN);

function fieldVal(f, key) {
  const v = f[key];
  if (!v) return null;
  if (v.stringValue != null) return v.stringValue;
  if (v.integerValue != null) return Number(v.integerValue);
  return null;
}

function docName(path) {
  return path.split("/").pop();
}

async function main() {
  const email = process.argv[2] || "mail2anuragmn@gmail.com";
  const docId = `eduai_app_${email}`;
  const parent = `projects/eduai-e090e/databases/(default)/documents/progress/${docId}/records`;

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

  const today = new Date().toISOString().slice(0, 10);
  const startOfDay = new Date(today + "T00:00:00.000Z").getTime();
  const endOfDay = startOfDay + 86400000;

  const simToday = { en: [], kn: [], unknown: [] };
  const simAll = { en: 0, kn: 0, unknown: 0, legacyNoLangSuffix: 0 };
  const newFormatToday = [];

  for (const doc of docs) {
    const f = doc.fields || {};
    const itemType = (fieldVal(f, "itemType") || "").toUpperCase();
    if (itemType !== "SIMULATION") continue;

    const itemId = fieldVal(f, "itemId") || "?";
    const lang = (fieldVal(f, "language") || "missing").toLowerCase();
    const status = fieldVal(f, "status") || "?";
    const completedAt = fieldVal(f, "completedAt");
    const docIdPart = docName(doc.name);
    const hasLangSuffix = docIdPart.endsWith("_en") || docIdPart.endsWith("_kn");

    if (lang === "en") simAll.en++;
    else if (lang === "kn") simAll.kn++;
    else simAll.unknown++;
    if (!hasLangSuffix) simAll.legacyNoLangSuffix++;

    if (
      status === "COMPLETED" &&
      completedAt != null &&
      completedAt >= startOfDay &&
      completedAt < endOfDay
    ) {
      const entry = { docId: docIdPart, itemId, lang, completedAt: new Date(completedAt).toISOString() };
      if (lang === "en") simToday.en.push(entry);
      else if (lang === "kn") simToday.kn.push(entry);
      else simToday.unknown.push(entry);
      if (hasLangSuffix) newFormatToday.push(entry);
    }
  }

  console.log(`Firestore: progress/${docId}/records`);
  console.log(`Total docs: ${docs.length}`);
  console.log(`Today (UTC date ${today}): English SIMULATION completed = ${simToday.en.length}`);
  console.log(`Today: Kannada SIMULATION completed = ${simToday.kn.length}`);
  if (simToday.unknown.length) console.log(`Today: unknown language = ${simToday.unknown.length}`);

  console.log("\n--- Today's English sims ---");
  simToday.en.forEach((e) => console.log(`  ${e.docId} | ${e.itemId} | lang=${e.lang}`));

  console.log("\n--- Today's Kannada sims ---");
  simToday.kn.forEach((e) => console.log(`  ${e.docId} | ${e.itemId} | lang=${e.lang}`));

  console.log("\n--- All SIMULATION by language field ---");
  console.log(JSON.stringify(simAll, null, 2));
  console.log(`New-format doc IDs completed today: ${newFormatToday.length}`);
}

main().catch((e) => {
  console.error("Error:", e.message);
  process.exit(1);
});

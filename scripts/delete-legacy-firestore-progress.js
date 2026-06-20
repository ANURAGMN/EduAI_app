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

const PROJECT = "eduai-e090e";
const STUDENT_DOC = process.argv[2] || "eduai_app_mail2anuragmn@gmail.com";

function fieldVal(f, key) {
  const v = f[key];
  if (!v) return null;
  return v.stringValue ?? (v.integerValue != null ? v.integerValue : null);
}

function isLegacyFormatDoc(doc) {
  const id = doc.name.split("/").pop();
  if (id.endsWith("_en") || id.endsWith("_kn")) return false;
  const type = (fieldVal(doc.fields, "itemType") || "").toUpperCase();
  return type === "SIMULATION" || type === "SIMULATION_AGENT";
}

async function listAll(client, parent) {
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
  return docs;
}

async function main() {
  const client = new Client({
    urlPrefix: "https://firestore.googleapis.com",
    apiVersion: "v1",
  });
  const parent = `projects/${PROJECT}/databases/(default)/documents/progress/${STUDENT_DOC}/records`;
  const docs = await listAll(client, parent);
  const legacy = docs.filter(isLegacyFormatDoc);
  console.log(`Correct path: progress/${STUDENT_DOC}/records`);
  console.log(`Legacy-format SIMULATION docs (no _en/_kn suffix): ${legacy.length}`);

  for (const doc of legacy) {
    await client.request({ method: "DELETE", path: `/${doc.name}` });
    console.log("Deleted:", doc.name.split("/").pop());
  }
  console.log(`Done. Removed ${legacy.length} legacy-format simulation docs.`);
}

main().catch((e) => {
  console.error("Error:", e.message);
  process.exit(1);
});

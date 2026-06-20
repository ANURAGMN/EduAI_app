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
const ORPHAN_DOC_ID = process.argv[2] || "mail2anuragmn@gmail.com";

async function listAllRecords(client, studentDocId) {
  const parent = `projects/${PROJECT}/databases/(default)/documents/progress/${studentDocId}/records`;
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

async function deleteDoc(client, docName) {
  await client.request({
    method: "DELETE",
    path: `/${docName}`,
  });
}

async function main() {
  const client = new Client({
    urlPrefix: "https://firestore.googleapis.com",
    apiVersion: "v1",
  });

  console.log(`Listing orphaned docs: progress/${ORPHAN_DOC_ID}/records`);
  const docs = await listAllRecords(client, ORPHAN_DOC_ID);
  console.log(`Found ${docs.length} documents to delete.`);

  if (docs.length === 0) {
    console.log("Nothing to delete.");
    return;
  }

  let deleted = 0;
  for (const doc of docs) {
    await deleteDoc(client, doc.name);
    deleted++;
    if (deleted % 10 === 0) console.log(`Deleted ${deleted}/${docs.length}...`);
  }

  console.log(`Done. Deleted ${deleted} orphaned documents.`);
}

main().catch((e) => {
  console.error("Error:", e.message);
  process.exit(1);
});

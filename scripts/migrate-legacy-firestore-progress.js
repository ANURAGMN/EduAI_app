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
const ITEM_TYPES = [
  "SIMULATION_AGENT",
  "SIMULATION",
  "MATH_AGENT",
  "REVISION_AGENT",
  "SCIENCE_AGENT",
  "CONCEPT",
];

const dryRun = process.argv.includes("--dry-run");
const allUsers = process.argv.includes("--all-users");
const studentArg = process.argv.find((a) => a.startsWith("--student="));
const defaultEmail = "mail2anuragmn@gmail.com";

function fieldVal(fields, key) {
  const v = fields?.[key];
  if (!v) return null;
  if (v.stringValue != null) return v.stringValue;
  if (v.integerValue != null) return v.integerValue;
  if (v.booleanValue != null) return v.booleanValue;
  if (v.nullValue != null) return null;
  return null;
}

function docId(fullName) {
  return fullName.split("/").pop();
}

function isNewFormat(id) {
  return id.endsWith("_en") || id.endsWith("_kn");
}

function parseLegacyDocId(id) {
  for (const type of ITEM_TYPES) {
    const prefix = `${type}_`;
    if (id.startsWith(prefix)) {
      return { itemType: type, itemId: id.slice(prefix.length) };
    }
  }
  return null;
}

function progressRecordDocId(itemType, itemId, language) {
  return `${itemType}_${itemId}_${language}`;
}

function toFirestoreFields(obj) {
  const fields = {};
  for (const [key, value] of Object.entries(obj)) {
    if (value == null) {
      fields[key] = { nullValue: null };
    } else if (typeof value === "string") {
      fields[key] = { stringValue: value };
    } else if (typeof value === "number" && Number.isInteger(value)) {
      fields[key] = { integerValue: String(value) };
    } else if (typeof value === "boolean") {
      fields[key] = { booleanValue: value };
    }
  }
  return fields;
}

function fromFirestoreFields(fields) {
  const out = {};
  for (const [key, value] of Object.entries(fields || {})) {
    out[key] = fieldVal(fields, key);
  }
  return out;
}

async function listStudentDocIds(client) {
  const parent = `projects/${PROJECT}/databases/(default)/documents/progress`;
  let pageToken;
  const ids = [];
  do {
    const res = await client.request({
      method: "GET",
      path: `/${parent}`,
      queryParams: {
        pageSize: "100",
        showMissing: "true",
        ...(pageToken ? { pageToken } : {}),
      },
    });
    for (const doc of res.body.documents || []) {
      ids.push(docId(doc.name));
    }
    pageToken = res.body.nextPageToken;
  } while (pageToken);
  return ids.filter((id) => id.startsWith("eduai_app_"));
}

async function listRecords(client, studentDocId) {
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

async function getDoc(client, studentDocId, recordId) {
  const name = `projects/${PROJECT}/databases/(default)/documents/progress/${studentDocId}/records/${recordId}`;
  try {
    const res = await client.request({ method: "GET", path: `/${name}` });
    return res.body;
  } catch (e) {
    if (e.message?.includes("404") || e.message?.includes("NOT_FOUND")) return null;
    throw e;
  }
}

async function setDoc(client, studentDocId, recordId, fields) {
  const name = `projects/${PROJECT}/databases/(default)/documents/progress/${studentDocId}/records/${recordId}`;
  await client.request({
    method: "PATCH",
    path: `/${name}`,
    body: { fields: toFirestoreFields(fields) },
  });
}

async function deleteDoc(client, fullName) {
  await client.request({ method: "DELETE", path: `/${fullName}` });
}

async function migrateStudent(client, studentDocId) {
  const docs = await listRecords(client, studentDocId);
  const legacy = docs.filter((d) => !isNewFormat(docId(d.name)));
  const newKeys = new Set(
    docs
      .filter((d) => isNewFormat(docId(d.name)))
      .map((d) => `${fieldVal(d.fields, "itemType")}|${fieldVal(d.fields, "itemId")}`)
  );

  const stats = { migrated: 0, deletedDuplicate: 0, skipped: 0, errors: 0 };

  console.log(`\n--- ${studentDocId} (${legacy.length} legacy docs) ---`);

  for (const doc of legacy) {
    const id = docId(doc.name);
    const parsed = parseLegacyDocId(id);
    if (!parsed) {
      console.log(`SKIP unknown id format: ${id}`);
      stats.skipped++;
      continue;
    }

    const { itemType, itemId } = parsed;
    const key = `${itemType}|${itemId}`;
    const data = fromFirestoreFields(doc.fields);
    const enId = progressRecordDocId(itemType, itemId, "en");
    const knId = progressRecordDocId(itemType, itemId, "kn");
    const hasEn = docs.some((d) => docId(d.name) === enId);
    const hasKn = docs.some((d) => docId(d.name) === knId);

    if (hasEn || hasKn || newKeys.has(key)) {
      console.log(`${dryRun ? "[dry-run] " : ""}DELETE duplicate legacy: ${id} (new format exists)`);
      if (!dryRun) await deleteDoc(client, doc.name);
      stats.deletedDuplicate++;
      continue;
    }

    const migrated = {
      ...data,
      itemType,
      itemId,
      language: "en",
      appName: data.appName || "eduai_app",
      syncedAt: Date.now(),
    };

    console.log(`${dryRun ? "[dry-run] " : ""}MIGRATE ${id} -> ${enId} (language=en)`);
    if (!dryRun) {
      await setDoc(client, studentDocId, enId, migrated);
      await deleteDoc(client, doc.name);
    }
    stats.migrated++;
  }

  return stats;
}

async function main() {
  const client = new Client({
    urlPrefix: "https://firestore.googleapis.com",
    apiVersion: "v1",
  });

  let studentIds;
  if (allUsers) {
    studentIds = await listStudentDocIds(client);
    console.log(`Found ${studentIds.length} eduai_app student containers`);
  } else {
    const email = studentArg ? studentArg.split("=")[1] : defaultEmail;
    studentIds = [`eduai_app_${email}`];
  }

  const totals = { migrated: 0, deletedDuplicate: 0, skipped: 0, errors: 0 };
  console.log(dryRun ? "DRY RUN — no writes" : "LIVE MIGRATION");

  for (const studentDocId of studentIds) {
    try {
      const stats = await migrateStudent(client, studentDocId);
      for (const k of Object.keys(totals)) totals[k] += stats[k];
    } catch (e) {
      console.error(`ERROR ${studentDocId}:`, e.message);
      totals.errors++;
    }
  }

  console.log("\n=== Totals ===");
  console.log(JSON.stringify(totals, null, 2));
}

main().catch((e) => {
  console.error("Fatal:", e.message);
  process.exit(1);
});

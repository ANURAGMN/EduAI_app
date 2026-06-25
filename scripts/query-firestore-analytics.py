#!/usr/bin/env python3
"""Query Firestore analytics events for a user (same data as query-firestore-analytics.js)."""

from __future__ import annotations

import json
import sys
import urllib.error
import urllib.parse
import urllib.request
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path

PROJECT = "eduai-e090e"
CLIENT_ID = "563584335869-fgrhgmd47bqnekij5i8b5pr03ho849e6.apps.googleusercontent.com"
CLIENT_SECRET = "j9iVZfS8kkCEFUPaAeJV0sAi"
TOKEN_PATH = Path(__file__).resolve().parent.parent / ".tools" / "firebase-ci-token.txt"


def load_refresh_token() -> str:
    return TOKEN_PATH.read_text(encoding="utf-8").strip()


def get_access_token(refresh_token: str) -> str:
    body = urllib.parse.urlencode(
        {
            "refresh_token": refresh_token,
            "client_id": CLIENT_ID,
            "client_secret": CLIENT_SECRET,
            "grant_type": "refresh_token",
        }
    ).encode()
    req = urllib.request.Request(
        "https://oauth2.googleapis.com/token",
        data=body,
        method="POST",
        headers={"Content-Type": "application/x-www-form-urlencoded"},
    )
    with urllib.request.urlopen(req, timeout=60) as resp:
        data = json.loads(resp.read().decode())
    token = data.get("access_token")
    if not token:
        raise RuntimeError(f"Token refresh failed: {data}")
    return token


def field_val(fields: dict, key: str):
    v = fields.get(key)
    if not v:
        return None
    if "stringValue" in v:
        return v["stringValue"]
    if "integerValue" in v:
        return int(v["integerValue"])
    if "doubleValue" in v:
        return float(v["doubleValue"])
    if "nullValue" in v:
        return None
    return None


def list_events(access_token: str, email: str) -> list[dict]:
    doc_id = f"eduai_app_{email}"
    parent = (
        f"projects/{PROJECT}/databases/(default)/documents/"
        f"analytics/{urllib.parse.quote(doc_id, safe='')}/events"
    )
    url = f"https://firestore.googleapis.com/v1/{parent}?pageSize=100"
    events: list[dict] = []

    while url:
        req = urllib.request.Request(
            url,
            headers={"Authorization": f"Bearer {access_token}"},
        )
        with urllib.request.urlopen(req, timeout=60) as resp:
            payload = json.loads(resp.read().decode())
        for doc in payload.get("documents", []):
            f = doc.get("fields", {})
            name = doc.get("name", "")
            events.append(
                {
                    "id": name.split("/")[-1],
                    "screenName": field_val(f, "screenName"),
                    "eventType": field_val(f, "eventType"),
                    "conceptId": field_val(f, "conceptId"),
                    "source": field_val(f, "source"),
                    "interactionType": field_val(f, "interactionType"),
                    "entryTime": field_val(f, "entryTime"),
                    "exitTime": field_val(f, "exitTime"),
                    "durationMillis": field_val(f, "durationMillis"),
                    "syncedAt": field_val(f, "syncedAt"),
                }
            )
        next_token = payload.get("nextPageToken")
        url = (
            f"https://firestore.googleapis.com/v1/{parent}?pageSize=100&pageToken={next_token}"
            if next_token
            else None
        )
    return events


def fmt_time(ms) -> str:
    if ms is None:
        return "?"
    return datetime.fromtimestamp(ms / 1000, tz=timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")


def main() -> None:
    email = sys.argv[1] if len(sys.argv) > 1 else "mail2anuragmn@gmail.com"
    refresh = load_refresh_token()
    token = get_access_token(refresh)
    events = list_events(token, email)
    events.sort(key=lambda e: e.get("entryTime") or 0, reverse=True)

    clicks = [e for e in events if e.get("eventType") == "CLICK"]
    funnel = [e for e in events if e.get("eventType") == "FUNNEL"]
    content = [e for e in clicks if e.get("screenName") == "CONTENT"]
    sim = [e for e in clicks if e.get("screenName") == "SIMULATION"]

    print(f"\n=== Analytics for {email} ===")
    print(f"Total events: {len(events)}")
    print(
        f"CLICK events: {len(clicks)} (content={len(content)}, simulation={len(sim)})"
    )
    print(f"FUNNEL events: {len(funnel)}")
    if funnel:
        counts = Counter(
            e.get("interactionType") or e.get("conceptId") or "?" for e in funnel
        )
        print("Funnel breakdown:", dict(counts))

    print("\n--- Latest 15 events ---")
    for e in events[:15]:
        when = fmt_time(e.get("entryTime"))
        line = f"[{when}] {e.get('screenName')}/{e.get('eventType')}"
        if e.get("conceptId"):
            line += f" concept={e['conceptId']}"
        if e.get("source"):
            line += f" source={e['source']}"
        if e.get("interactionType"):
            line += f" type={e['interactionType']}"
        if e.get("eventType") == "EXIT" and e.get("durationMillis") is not None:
            line += f" duration={round(e['durationMillis'] / 1000)}s"
        print(line)


if __name__ == "__main__":
    try:
        main()
    except urllib.error.HTTPError as err:
        body = err.read().decode(errors="replace")
        print(f"HTTP {err.code}: {body}", file=sys.stderr)
        sys.exit(1)
    except Exception as err:
        print(err, file=sys.stderr)
        sys.exit(1)

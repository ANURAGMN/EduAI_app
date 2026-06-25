import os
import shutil
import sqlite3

TMP = os.path.join(os.path.dirname(__file__), "..", "_tmp_db")
QDIR = os.path.join(TMP, "query")
os.makedirs(QDIR, exist_ok=True)

for name in ("eduai_database", "eduai_database-wal", "eduai_database-shm"):
    src = os.path.join(TMP, name)
    if os.path.isfile(src):
        shutil.copy2(src, os.path.join(QDIR, name))

db = os.path.join(QDIR, "eduai_database")
con = sqlite3.connect(db)
cur = con.cursor()

cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
print("Tables:", [r[0] for r in cur.fetchall()])

cur.execute("SELECT COUNT(*) FROM app_analytics")
print("app_analytics count:", cur.fetchone()[0])

cur.execute(
    """
    SELECT analyticsId, screenName, eventType, interactionType, studentId, isSynced, entryTime
    FROM app_analytics
    ORDER BY entryTime DESC
    LIMIT 25
    """
)
print("\nRecent events:")
for row in cur.fetchall():
    print(" ", row)

cur.execute(
    """
    SELECT interactionType, COUNT(*)
    FROM app_analytics
    WHERE eventType = 'FUNNEL'
    GROUP BY interactionType
    ORDER BY interactionType
    """
)
print("\nFunnel breakdown:", cur.fetchall())
con.close()

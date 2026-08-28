#!/usr/bin/env python3
"""Repair UTF-8 mojibake in simulation HTML using ftfy."""
from __future__ import annotations

import sys
from pathlib import Path

try:
    import ftfy
except ImportError:
    print("Run: pip install ftfy", file=sys.stderr)
    raise SystemExit(1)

SIM = Path(__file__).resolve().parent.parent / "Simulations"


def main() -> int:
    dry = "--dry-run" in sys.argv
    total_files = 0
    for path in sorted(SIM.glob("*.html")):
        text = path.read_text(encoding="utf-8")
        fixed = ftfy.fix_text(text)
        if fixed == text:
            continue
        total_files += 1
        print(path.name)
        if not dry:
            path.write_text(fixed, encoding="utf-8", newline="\n")
    print(f"\n{'Would fix' if dry else 'Fixed'} {total_files} HTML file(s)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

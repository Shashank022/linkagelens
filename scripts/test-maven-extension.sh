#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/integration-tests/maven-auto"
rm -rf target
mvn -B -ntp verify
test -f target/linkagelens/linkagelens.json
python3 - <<'PY'
import json
p='target/linkagelens/linkagelens.json'
data=json.load(open(p))
assert data['tool'] == 'LinkageLens'
print('Maven Guardian integration passed:', p)
PY

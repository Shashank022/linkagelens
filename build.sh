#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"
rm -rf build dist
mkdir -p build/classes build/test-classes dist
find src/main/java -name '*.java' | sort > build/main-sources.txt
javac --release 17 -d build/classes @build/main-sources.txt
find src/test/java -name '*.java' | sort > build/test-sources.txt
javac --release 17 -cp build/classes -d build/test-classes @build/test-sources.txt
java -cp build/classes:build/test-classes io.github.shashank022.linkagelens.LinkageLensTest
jar --create --file dist/linkagelens.jar --main-class io.github.shashank022.linkagelens.Main -C build/classes .
echo "Built dist/linkagelens.jar"

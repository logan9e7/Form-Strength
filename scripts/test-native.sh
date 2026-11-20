#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_PLATFORM_JAR:?Set ANDROID_PLATFORM_JAR}"
: "${ECJ_JAR:?Set ECJ_JAR}"
: "${JSON_JAR:?Set JSON_JAR to org.json 20240303 jar}"
python3 - <<'PY'
import re, shutil
from pathlib import Path
out=Path('build/test-classes')
if out.is_symlink() or out.is_file():out.unlink()
elif out.exists():shutil.rmtree(out)
out.mkdir(parents=True)
# Compile all app Java, including SDK-isolated helpers added during maintenance.
test_sources=sorted(Path('tests').glob('*.java'))
if not test_sources:raise SystemExit('No native Java tests found')
sources=sorted(Path('app/src/main/java').rglob('*.java'))+test_sources
def quote(path):
 value=str(path)
 if '\n' in value or '\r' in value:raise SystemExit('Newlines in Java source paths are unsupported')
 return '"'+value.replace('\\','\\\\').replace('"','\\"')+'"'
Path('build/test-sources.args').write_text('\n'.join(map(quote,sources))+'\n')
# Test entry points are public top-level *Test classes. Helpers compile but do not run.
entry_points=[]
for source in test_sources:
 text=re.sub(r'/\*.*?\*/|//[^\n]*','',source.read_text(),flags=re.S)
 if not re.search(r'public\s+static\s+void\s+main\s*\(\s*String\s*(?:\[\s*\]|\.\.\.)',text):continue
 package=re.search(r'^\s*package\s+([A-Za-z_][\w.]*)\s*;',text,re.M)
 name=source.stem
 if not re.fullmatch(r'[A-Za-z_$][\w$]*',name):raise SystemExit('Invalid native test class name: '+name)
 entry_points.append((package.group(1)+'.' if package else '')+name)
if not entry_points:raise SystemExit('No runnable native Java tests found')
Path('build/test-main-classes.txt').write_text('\n'.join(entry_points)+'\n')
PY
java -jar "$ECJ_JAR" -8 -cp "$JSON_JAR:$ANDROID_PLATFORM_JAR" -d build/test-classes @build/test-sources.args
while IFS= read -r test_class; do
 [[ -n "$test_class" ]] || continue
 printf 'Running native test: %s\n' "$test_class"
 java -cp "build/test-classes:$JSON_JAR:$ANDROID_PLATFORM_JAR" "$test_class"
done < build/test-main-classes.txt
# Host-side SQL/build orchestration contracts complement JVM validation. They do
# not emulate SQLiteOpenHelper, Android class loading or framework lifecycle.
python3 tests/sqlite-migration.test.py
python3 tests/build-contract.test.py

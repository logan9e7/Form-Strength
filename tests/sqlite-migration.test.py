"""Execute production schema SQL on host SQLite; not Android instrumentation.

The source assertions bind these SQL checks to onCreate and the v4->v5 branch.
Android SQLiteOpenHelper lifecycle/upgrade still needs a real Android target.
"""
import re
import sqlite3
from pathlib import Path

source = (Path(__file__).resolve().parents[1] / 'app/src/main/java/com/logan/formstrength/LocalStore.java').read_text()

def body(name):
    match = re.search(r'\b' + name + r'\([^)]*\)\s*\{', source)
    assert match, f'missing production method {name}'
    start = match.end()
    depth = 1
    for end in range(start, len(source)):
        depth += (source[end] == '{') - (source[end] == '}')
        if depth == 0:
            return source[start:end]
    raise AssertionError(f'unclosed method {name}')

def sql(name):
    statements = re.findall(r'db\.execSQL\("([^"\\]*(?:\\.[^"\\]*)*)"\)', body(name))
    assert statements, f'no literal schema SQL in {name}'
    # Schema is ASCII literal SQL, with no Java escapes or interpolated values.
    assert all('\\' not in statement for statement in statements)
    return statements

assert re.search(r'super\(context,\s*"form_strength.db",\s*null,\s*5\)', source), 'schema version must be 5'
assert 'createActivities(db)' in body('onCreate'), 'new database must create activities'
assert 'createActivityIndex(db)' in body('createActivities'), 'new activity table must get day index'
assert re.search(r'if\s*\(\s*oldVersion\s*<\s*5\s*\)\s*createActivityIndex\(db\)', body('onUpgrade')), 'v4->v5 must invoke index creation'
index_sql = sql('createActivityIndex')
assert len(index_sql) == 1 and re.search(r'CREATE INDEX IF NOT EXISTS activity_day ON activity_entries\(day\)', index_sql[0]), 'activity index must be idempotent'

with sqlite3.connect(':memory:') as db:
    # Recreate the v4 activity table without the v5 index, then preserve real rows.
    for statement in sql('createActivities'):
        db.execute(statement)
    db.execute('PRAGMA user_version=4')
    db.executemany('INSERT INTO activity_entries VALUES (?,?,?)', [
        ('walk-1', '2026-11-18', '{"name":"Walking","minutes":30}'),
        ('cycle-1', '2026-11-19', '{"name":"Cycling","minutes":20}')])
    before = db.execute('SELECT * FROM activity_entries ORDER BY id').fetchall()
    with db:
        for statement in index_sql:
            db.execute(statement)
        db.execute('PRAGMA user_version=5')
    assert db.execute('PRAGMA user_version').fetchone()[0] == 5
    assert before == db.execute('SELECT * FROM activity_entries ORDER BY id').fetchall(), 'migration lost or changed payloads'
    assert db.execute('PRAGMA index_info(activity_day)').fetchall() == [(0, 1, 'day')]
    assert 'activity_day' in str(db.execute('EXPLAIN QUERY PLAN SELECT payload FROM activity_entries WHERE day=?', ('2026-11-18',)).fetchall()), 'day filter should use index'
    for statement in index_sql:
        db.execute(statement)  # Already-created index must be harmless.

with sqlite3.connect(':memory:') as db:
    for statement in sql('createActivities') + index_sql:
        db.execute(statement)
    assert 'activity_day' in str(db.execute('PRAGMA index_list(activity_entries)').fetchall())
print('PASS host SQLite schema: production v4->v5 index SQL preserves rows, repeated creation, fresh table and indexed day query; Android lifecycle remains pending.')

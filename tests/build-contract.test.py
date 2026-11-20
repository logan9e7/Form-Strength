"""Hermetic build orchestration checks; this does not validate Android bytecode."""
import json, os, shutil, subprocess, tempfile
from pathlib import Path

repo=Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory(prefix='form strength build ') as temp:
 root=Path(temp)/'project with spaces';root.mkdir()
 (root/'scripts').mkdir();shutil.copyfile(repo/'scripts/build.sh',root/'scripts/build.sh')
 source=root/'app/src/main/java/example/Main Class.java';source.parent.mkdir(parents=True);source.write_text('// source-path contract')
 (root/'app/src/main/res').mkdir(parents=True);(root/'app/src/main/assets').mkdir()
 (root/'app/src/main/AndroidManifest.xml').write_text('<manifest xmlns:android="http://schemas.android.com/apk/res/android" android:versionName="1.6.3" android:versionCode="12"><uses-sdk android:minSdkVersion="26"/></manifest>')
 bin_dir=Path(temp)/'tool bin';bin_dir.mkdir();tools=Path(temp)/'android build tools';tools.mkdir()
 stub='''#!/usr/bin/env python3
import os,sys,json,zipfile,shlex,shutil
from pathlib import Path
name=Path(sys.argv[0]).name;args=sys.argv[1:]
with open(os.environ['BUILD_TEST_CALLS'],'a') as f:f.write(json.dumps([name,args])+'\\n')
if name=='java':
 if os.environ.get('BUILD_TEST_FAIL_JAVA'):sys.exit(9)
 paths=shlex.split(Path(next(a[1:] for a in args if a.startswith('@'))).read_text())
 assert paths==['app/src/main/java/example/Main Class.java'],paths
 out=Path(args[args.index('-d')+1]);(out/'example').mkdir(parents=True,exist_ok=True);(out/'example/Main.class').write_bytes(b'fresh class')
elif name=='aapt2':
 with zipfile.ZipFile(args[args.index('-o')+1],'w'):pass
elif name=='d8':
 assert args[-1]=='build/classes.jar',args
 with zipfile.ZipFile(args[-1]) as z:assert z.namelist()==['example/Main.class'],z.namelist()
 assert not Path('build/dex/stale.dex').exists()
 (Path(args[args.index('--output')+1])/'classes.dex').write_bytes(b'dex test')
elif name=='apksigner':
 if args[0]=='sign':shutil.copyfile(args[-1],args[args.index('--out')+1])
elif name=='zipalign':
 if '-c' not in args:shutil.copyfile(args[-2],args[-1])
'''
 for name,folder in [('java',bin_dir),('aapt2',tools),('d8',tools),('zipalign',tools),('apksigner',tools)]:
  p=folder/name;p.write_text(stub);p.chmod(0o755)
 calls=Path(temp)/'calls.jsonl';env=os.environ.copy();env.update(PATH=str(bin_dir)+os.pathsep+env['PATH'],ANDROID_PLATFORM_JAR=str(Path(temp)/'android platform.jar'),ANDROID_BUILD_TOOLS=str(tools),ECJ_JAR=str(Path(temp)/'compiler jar.jar'),BUILD_TEST_CALLS=str(calls))
 for key in ('APK_KEYSTORE','APK_KEYSTORE_PASSWORD','APK_KEY_PASSWORD','APK_KEY_ALIAS','APK_OUTPUT_NAME','APK_VERSION_NAME','APK_VERSION_CODE'):env.pop(key,None)
 for suffix in ['classes/stale.class','dex/stale.dex','compiled/stale.zip']:
  p=root/'build'/suffix;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b'stale')
 subprocess.run(['bash','scripts/build.sh'],cwd=root,env=env,check=True,capture_output=True,text=True)
 artifact=root/'outputs/Form-Strength-1.6.3-unsigned.apk';assert artifact.exists()
 recorded=[json.loads(x) for x in calls.read_text().splitlines()]
 assert sum(n=='d8' for n,a in recorded)==1
 assert not (root/'build/classes/stale.class').exists()
 assert not (root/'build/compiled/stale.zip').exists()
 with __import__('zipfile').ZipFile(artifact) as z:assert z.namelist()==['classes.dex']
 # Environment metadata is a guard, not a way to mislabel package identity.
 for key,value in [('APK_VERSION_NAME','9.9.9'),('APK_VERSION_CODE','99'),('APK_OUTPUT_NAME','../escape.apk'),('APK_OUTPUT_NAME','bad name.apk')]:
  invalid=env.copy();invalid[key]=value
  assert subprocess.run(['bash','scripts/build.sh'],cwd=root,env=invalid,capture_output=True).returncode!=0,(key,value)
 custom=env.copy();custom.update(APK_OUTPUT_NAME='qa-special.apk',APK_VERSION_NAME='1.6.3',APK_VERSION_CODE='12')
 subprocess.run(['bash','scripts/build.sh'],cwd=root,env=custom,check=True,capture_output=True)
 assert (root/'outputs/qa-special.apk').exists()
 signed=env.copy();signed.update(APK_KEYSTORE='/private/path with spaces/release.p12',APK_KEYSTORE_PASSWORD='store-test',APK_KEY_PASSWORD='key-test',APK_KEY_ALIAS='qa-signing')
 subprocess.run(['bash','scripts/build.sh'],cwd=root,env=signed,check=True,capture_output=True)
 assert (root/'outputs/Form-Strength-1.6.3.apk').exists()
 sign_args=[a for n,a in [json.loads(x) for x in calls.read_text().splitlines()] if n=='apksigner' and a[0]=='sign'][-1]
 assert sign_args[sign_args.index('--ks-key-alias')+1]=='qa-signing'
 assert sign_args[sign_args.index('--ks-pass')+1]=='env:APK_KEYSTORE_PASSWORD'
 assert sign_args[sign_args.index('--key-pass')+1]=='env:APK_KEY_PASSWORD'
 assert 'store-test' not in sign_args and 'key-test' not in sign_args
 env['BUILD_TEST_FAIL_JAVA']='1'
 failed=subprocess.run(['bash','scripts/build.sh'],cwd=root,env=env,capture_output=True,text=True)
 assert failed.returncode!=0
 assert not artifact.exists(),'failed same-version build must not leave a stale release artifact'
 print('PASS build contract: paths with spaces, clean outputs, one archive/D8 call, manifest version guards, safe custom output and failed-build cleanup.')

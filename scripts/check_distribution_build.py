"""No-secret Gradle smoke checks; requires the normal JDK/Android SDK setup."""
import os, subprocess
from pathlib import Path
root=Path(__file__).resolve().parent.parent
env=os.environ.copy()
env['BASTI_VERSION_NAME']='1.1-config-check'
for key in ['BASTI_KEYSTORE_PATH','BASTI_STORE_PASSWORD','BASTI_KEY_ALIAS','BASTI_KEY_PASSWORD']:
    env[key]=''
base=['./gradlew','--console=plain']
cases=[
 ('release_missing_credentials',['assembleRelease'],{'BASTI_VERSION_CODE':'1000101'},False,'Signed release requires BASTI_KEYSTORE_PATH'),
 ('bundle_missing_credentials',['bundleRelease'],{'BASTI_VERSION_CODE':'1000101'},False,'Signed release requires BASTI_KEYSTORE_PATH'),
 ('overflow_rejected',['help'],{'BASTI_VERSION_CODE':'2100000001'},False,'BASTI_VERSION_CODE must be'),
 ('noninteger_rejected',['help'],{'BASTI_VERSION_CODE':'oops'},False,'BASTI_VERSION_CODE must be'),
 ('maximum_code_configures',['help'],{'BASTI_VERSION_CODE':'2100000000'},True,'BUILD SUCCESSFUL'),
 ('release_graph_guard',['assembleRelease','--dry-run'],{'BASTI_VERSION_CODE':'1000101'},True,':app:verifyDistributionSigning SKIPPED'),
]
for name,args,values,success,expected in cases:
    case_env=env | values
    p=subprocess.run(base+args,cwd=root,env=case_env,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    okay=(p.returncode==0)==success and expected in p.stdout
    print(name, 'PASS' if okay else 'FAIL',f'(exit {p.returncode})',flush=True)
    if not okay:
        raise SystemExit('Unexpected Gradle validation result; inspect locally without printing credentials')

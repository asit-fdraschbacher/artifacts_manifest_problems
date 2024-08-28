#!/bin/bash

if [ $# -eq 0 ]; then
    >&2 echo "Usage: check_ct.sh {apk_path}"
    exit 1
fi

zip tmp-apks.zip "$1"
/usr/local/opt/openjdk/bin/java -jar "/usr/local/Cellar/bundletool/1.16.0/libexec/bundletool-all.jar" check-transparency --mode=apk --apk-zip=tmp-apks.zip
rm tmp-apks.zip

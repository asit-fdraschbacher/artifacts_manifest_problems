#!/bin/bash

if [ $# -eq 0 ]; then
    >&2 echo "Usage: check_ct.sh {apk_path}"
    exit 1
fi

zip tmp-apks.zip "$1"
bundletool check-transparency --mode=apk --apk-zip=tmp-apks.zip
rm tmp-apks.zip

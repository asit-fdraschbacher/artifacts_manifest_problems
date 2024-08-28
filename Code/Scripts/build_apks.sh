#!/bin/bash

if [ $# -eq 0 ]; then
    >&2 echo "Usage: build_apks.sh {aab_path} {ks_path} {ks_pass}"
    exit 1
fi

aab_name=$(basename -- "$1")
aab_name="${aab_name%.*}"
ks_name=$(basename -- "$2")
ks_name="${ks_name%.*}"

bundletool build-apks --bundle="$1" --output="$aab_name".apks --ks=$2 --ks-key-alias="$ks_name" --ks-pass=pass:"$3"
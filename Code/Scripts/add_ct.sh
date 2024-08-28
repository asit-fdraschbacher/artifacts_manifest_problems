#!/bin/bash

if [ $# -eq 0 ]; then
    >&2 echo "Usage: add_ct.sh {aab_path} {ks_path}"
    exit 1
fi

aab_name=$(basename -- "$1")
aab_name="${aab_name%.*}"
ks_name=$(basename -- "$2")
ks_name="${ks_name%.*}"

bundletool add-transparency --bundle="$1" --output="$aab_name".signed.aab --ks="$2" --ks-key-alias="$ks_name"

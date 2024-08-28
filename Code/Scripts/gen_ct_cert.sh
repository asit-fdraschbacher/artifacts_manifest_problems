#!/bin/bash

if [ $# -eq 0 ]; then
    >&2 echo "Usage: gen_ct_cert.sh {password} {file_path}"
    exit 1
fi

keytool -genkeypair -alias "$2" -keyalg RSA -keysize 4096 -sigalg SHA256withRSA -dname "cn=example.com,ou=exampleou,dc=example,dc=com" -keypass "$1" -startdate "2019/12/19 00:00:00" -validity 50000 -storetype JKS -storepass "$1" -keystore "$2".jks
keytool -export -keystore "$2".jks -alias "$2" -rfc -file "$2".cert -keypass "$1" -storepass "$1"
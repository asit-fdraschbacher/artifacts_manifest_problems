# Utility scripts for working with AAB and CT
These utilities speed up common tasks when working with bundletool, Google's official Code Transparency implementation.

## add_ct.sh
Adds a Code Transparency to an AAB file. Usage: add_ct.sh {aab_path} {keystore_path}

## build_apks.sh
Builds signed APKs from an AAB file. Usage: build_apks.sh {aab_path} {keystore_path} {keystore_pass}

## check_ct.sh
Checks the Code Transparency of an APK file. Usage: check_ct.sh {apk_path}

## gen_ct_cert.sh
Generates a keystore for use in generating a CT. Usage: gen_ct_cert.sh {password} {file_path}
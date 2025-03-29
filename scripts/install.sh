#!/bin/bash

#
# Author: donhk
# Date: 29/03/25
# Licence: MTI
#

if [[ -z "${JAVA_HOME+x}" ]]; then
  echo "JAVA_HOME is unset"
  echo "configure it and try again"
  exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is not installed"
  exit 1
fi

WORK_DIRECTORY=$(pwd)
VIRTUALBOX_HOME="https://download.virtualbox.org/virtualbox"
VIRTUALBOX_VERSION=$(curl -s "${VIRTUALBOX_HOME}/LATEST-STABLE.TXT")
VIRTUALBOX_SDK=$(curl -s "${VIRTUALBOX_HOME}/${VIRTUALBOX_VERSION}/MD5SUMS" | grep -o 'VirtualBoxSDK-.*\.zip')
VIRTUALBOX_SDK_ZIP=vbox_sdk.zip
SDK_DESTINATION="vbox-glue/src/main/libs"
JAVA_VBOX_XPCOM="sdk/bindings/xpcom/java/vboxjxpcom.jar"
VBOX_GLUE_CODE="vbox-glue/src/main/java/dev/donhk/vbox"
VBOX_HOME="/usr/lib/virtualbox"


print_help(){
  echo ""
  echo " This script is a workaround to configure the virtualbox sdk dependencies"
  echo " because they are not available in maven, why? No f*cking idea but legend"
  echo " tells it is because they are not pure java as they depend on virtualbox"
  echo " native bindings installed"
}

print_env(){
  echo ""
  echo "-------------------------------------------------------------"
  echo "WORK_DIRECTORY        ${WORK_DIRECTORY}"
  echo "JAVA_HOME             ${JAVA_HOME}"
  echo "VIRTUALBOX_VERSION    ${VIRTUALBOX_VERSION}"
  echo "VIRTUALBOX_SDK        ${VIRTUALBOX_SDK}"
  echo "-------------------------------------------------------------"
  echo ""
}

download_vbox_sdk(){
  echo ""
  echo "Getting the latest version of VirtualBoxSDK for you ❤️"
  echo ""
  local SDK_LOCATION="${VIRTUALBOX_HOME}/${VIRTUALBOX_VERSION}/${VIRTUALBOX_SDK}"
  curl -o "${VIRTUALBOX_SDK_ZIP}" "${SDK_LOCATION}"
  unzip -oq "${VIRTUALBOX_SDK_ZIP}"
  cp "${WORK_DIRECTORY}/${JAVA_VBOX_XPCOM}" "${SDK_DESTINATION}/"
  rm -rf "${VIRTUALBOX_SDK_ZIP}"
  rm -rf "sdk"
  return 0
}

update_virtualbox_imports() {
  local path="${VBOX_GLUE_CODE}/"
  local major_minor
  major_minor=$(echo "$VIRTUALBOX_VERSION" | cut -d. -f1,2 | tr '.' '_')
  local new_prefix="org.virtualbox_${major_minor}."

  echo ""
  echo "Updating SDK version of virtualbox in java files🔍"
  echo ""

  find "$path" -type f -name '*.java' -print0 | while IFS= read -r -d '' file; do
    echo "Updating 📃 ${file}"
    sed -i -E "s/org\.virtualbox_[0-9]+_[0-9]+\./$new_prefix/g" "$file"
  done

  echo ""
  echo "This updated the API virtualbox_a_b. to virtualbox_c_d. but you still"
  echo "need to make sure the code compiles, some releases change the API and"
  echo "that need manual changes to update the code 😣"
}

check_virtualbox() {
  local INSTALLED_VIRTUALBOX_VERSION
  INSTALLED_VIRTUALBOX_VERSION=$(VBoxManage --version)

  if [[ -z "${INSTALLED_VIRTUALBOX_VERSION}" ]]; then
    echo "💀 VirtualBox is not installed"
    exit 1
  fi

  local INSTALLED_EXT_PACK
  INSTALLED_EXT_PACK=$(VBoxManage list extpacks | grep -oP 'Version:\s*\K[\d.]+' )

  if [[ -z "${INSTALLED_EXT_PACK}" ]]; then
    echo "💀 VirtualBox Extension Pack is not installed"
    exit 1
  fi

  echo "👍 VirtualBox installed: ${INSTALLED_VIRTUALBOX_VERSION}"
  echo "👍 Extension Pack version: ${INSTALLED_EXT_PACK}"

  # Trim the VirtualBox version before the 'r' (e.g., 7.1.6r167084 → 7.1.6)
  local MAIN_VBOX_VERSION=${INSTALLED_VIRTUALBOX_VERSION%%r*}

  if [[ "${MAIN_VBOX_VERSION}" != "${INSTALLED_EXT_PACK}" ]]; then
    echo "💀 Versions mismatch"
    exit 1
  fi

  if [[ -d "${VBOX_HOME}" ]]; then
    echo "👍 VBOX_HOME '${VBOX_HOME}' Folder exists"
  else
    echo "💀 Folder '{${VBOX_HOME}' does not exist"
    exit 1
  fi

  echo ""
}



main(){
  print_help
  print_env
  check_virtualbox
  download_vbox_sdk
  update_virtualbox_imports
}

#
# Execute the install script
#
main
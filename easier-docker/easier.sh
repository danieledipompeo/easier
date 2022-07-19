#!/bin/bash

get_config_param() {
  grep -v '^#' config.ini | grep -Pzo "\-${1}\n\K[^\n]+"
}

CASE_STUDY=$(get_config_param m | cut -d'/' -f2)
ALGORITHM=$(get_config_param algo)
OUT_DIR=$(get_config_param outF)

wget -O config.ini $1
java -Xmx12g -jar easier.jar @./config.ini 2>&1 | tee "${OUT_DIR}/easier__${CASE_STUDY}__${ALGORITHM}.log"

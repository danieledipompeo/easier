#!/bin/bash

get_config_param() {
  grep -v '^#' config.ini | grep -Pzo "\-${1}\n\K[^\n]+"
}

wget -O config.ini $1

CASE_STUDY=$(get_config_param m | cut -d'/' -f2)
ALGORITHM=$(get_config_param algo)
OUT_DIR=$(get_config_param outF)

CASE_STUDY_FOLDER=/opt/easier/easier-uml2lqnCaseStudy/

mkdir -p ${CASE_STUDY_FOLDER}`dirname $(get_config_param m)`
mkdir -p ${CASE_STUDY_FOLDER}`dirname $(get_config_param initialModelPath)`

#download the model to be optmizated

#wget -O /opt/easier/easier-uml2lqnCaseStudy/$(get_config_param m) https://raw.githubusercontent.com/SEALABQualityGroup/uml2lqn-casestudies/support-workload/$(get_config_param m)
wget -O /opt/easier/easier-uml2lqnCaseStudy/$(get_config_param m) https://raw.githubusercontent.com/SEALABQualityGroup/uml2lqn-casestudies/${2}/$(get_config_param m)

# download the intial model path to compute the perfq
wget -O /opt/easier/easier-uml2lqnCaseStudy/$(get_config_param initialModelPath) https://raw.githubusercontent.com/SEALABQualityGroup/uml2lqn-casestudies/${2}/$(get_config_param initialModelPath)

mkdir -p $OUT_DIR
cp config.ini $OUT_DIR
JVM_PARAMS="-Xmx12g --add-exports java.xml/com.sun.org.apache.xerces.internal.dom=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${OUT_DIR}/easier-oom -XX:StartFlightRecording=filename=${OUT_DIR}/easier.jfr,path-to-gc-roots=true,settings=profile"

java -jar ${JVM_PARAMS} easier.jar @./config.ini 2>&1 | tee "${OUT_DIR}/easier__${CASE_STUDY}__${ALGORITHM}.log"

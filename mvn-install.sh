#!/bin/bash

# Exit on error
set -e

# Use Maven Wrapper (must be in project root)
MVN="./mvnw"

REPO="$1" 

# Ensure the wrapper exists
if [ ! -f "$MVN" ]; then
  echo "Error: Maven Wrapper not found. Run 'mvn -N io.takari:maven:wrapper -Dmaven=3.8.7' to generate it."
  exit 1
fi

$MVN install:install-file -Dfile=./easier-maven/org.eclipse.uml2.uml_5.5.0.v20181203-1331.jar -DgroupId=it.univaq.disim.sealab.uml -DartifactId=org.eclipse.uml2.uml -Dversion=5.5.0.v20181203-1331 -Dpackaging=jar ${REPO:+-Dmaven.repo.local="$REPO"}

$MVN install:install-file -Dfile=./easier-maven/org.eclipse.uml2.uml.resources_5.5.0.v20181203-1331.jar -DgroupId=it.univaq.disim.sealab.uml -DartifactId=org.eclipse.uml2.uml.resources -Dversion=5.5.0.v20181203-1331 -Dpackaging=jar ${REPO:+-Dmaven.repo.local="$REPO"}

$MVN install:install-file -Dfile=./easier-maven/org.eclipse.uml2.common_2.5.0.v20181203-1331.jar -DgroupId=it.univaq.disim.sealab.uml -DartifactId=org.eclipse.uml2.common -Dversion=2.5.0.v20181203-1331 -Dpackaging=jar ${REPO:+-Dmaven.repo.local="$REPO"}

$MVN install:install-file -Dfile=./easier-maven/org.eclipse.uml2.types_2.5.0.v20181203-1331.jar -DgroupId=it.univaq.disim.sealab.uml -DartifactId=org.eclipse.uml2.types -Dversion=2.5.0.v20181203-1331 -Dpackaging=jar ${REPO:+-Dmaven.repo.local="$REPO"}

$MVN install:install-file -Dfile=./easier-maven/org.eclipse.uml2.uml.profile.standard_1.5.0.v20181203-1331.jar -DgroupId=it.univaq.disim.sealab.uml.profiles -DartifactId=org.eclipse.uml2.uml.profile.standard -Dversion=1.5.0.v20181203-1331 -Dpackaging=jar ${REPO:+-Dmaven.repo.local="$REPO"}

$MVN install:install-file -Dfile=./easier-maven/com.masdes.dam.static.profile_0.13.1.201801221725.jar -DgroupId=es.unizar.profiles -DartifactId=com.masdes.dam.static.profile -Dversion=0.13.1.201801221725 -Dpackaging=jar ${REPO:+-Dmaven.repo.local="$REPO"}

$MVN install:install-file -Dfile=./easier-maven/org.eclipse.papyrus.marte.static.profile_1.2.3.202303241341.jar -DgroupId=org.eclipse.papyrus.marte -DartifactId=org.eclipse.papyrus.marte.static.profile -Dversion=1.2.3.202303241341 -Dpackaging=jar ${REPO:+-Dmaven.repo.local="$REPO"}

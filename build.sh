#!/usr/bin/env bash

JAVA_CMD=java
[ -n "$JDK_HOME" ] && JAVA_CMD=$JDK_HOME/bin/java
echo "Using Java command: $JAVA_CMD"

SBT_OPTIONS="-Xmx1G \
    -XX:MaxPermSize=250m \
    -XX:+UseCompressedOops \
    -Dsbt.log.noformat=true \
    -Dbuild.number=$BUILD_NUMBER \
    -Dbuild.vcs.number=$BUILD_VCS_NUMBER"

[ -d target ] && rm -rf target
mkdir target
cd target
mkdir -p artifacts/gibbons

if cd .. && $JAVA_CMD $SBT_OPTIONS -jar ./sbt-launch.jar assembly && cd target
then
    cp scala-*/*.jar ./artifacts/gibbons/gibbons.jar
else
    echo 'Failed to build exact-target-lambda'
    exit 1
fi

cp ../riff-raff.yaml ./artifacts

echo "##teamcity[publishArtifacts '$(pwd)/artifacts => .']"
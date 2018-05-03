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
cd $(dirname $0)/target
mkdir -p artifacts/gibbons

if cd .. && $JAVA_CMD $SBT_OPTIONS -jar ./sbt-launch.jar assembly
then
    cd target
    cp scala-*/*.jar ./artifacts/gibbons/gibbons.jar
    cp ../riff-raff.yaml ./artifacts
    echo "##teamcity[publishArtifacts '$(pwd)/artifacts => .']"
else
    echo 'Failed to build gibbons-lambdas'
    exit 1
fi
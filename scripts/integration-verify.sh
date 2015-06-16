#!/bin/bash
export JAVA_HOME=/usr/java/jdk1.7.0_latest
./gradlew build integrationTest --continue --refresh-dependencies --info

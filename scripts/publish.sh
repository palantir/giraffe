#!/bin/bash
set -e

export JAVA_HOME=/usr/java/jdk1.7.0_latest
./gradlew build publish --refresh-dependencies --info

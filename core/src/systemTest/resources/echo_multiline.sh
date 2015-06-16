#!/bin/sh
while read LINE; do
    if [ "$LINE" = "$1" ]; then
        exit 0
    else
        echo "$LINE"
    fi
done

#!/bin/sh
dd if=/dev/zero bs=$1 count=1 2>/dev/null | tr '\0' '0'

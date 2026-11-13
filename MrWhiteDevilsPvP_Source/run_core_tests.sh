#!/bin/sh
# Compiles + runs the pure-Java core tests (no Minecraft needed). Needs JDK 17+.
set -e
rm -rf .coreout && mkdir .coreout
SRC=$(find src/main -name '*.java' | grep -v '/fabric/')
if command -v javac >/dev/null 2>&1; then
  javac -d .coreout $SRC core-tests/CoreTests.java
else
  java -m jdk.compiler/com.sun.tools.javac.Main -d .coreout $SRC core-tests/CoreTests.java
fi
java -cp .coreout com.mrwhitedevil.pvp.CoreTests
rm -rf .coreout

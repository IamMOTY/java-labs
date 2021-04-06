#!/bin/bash
JAROOT=../../java-advanced-2021
ARTIFACTS=$JAROOT/artifacts
LIB=$JAROOT/lib
MODULES=$ARTIFACTS/info.kgeorgiy.java.advanced.base.jar:$ARTIFACTS/info.kgeorgiy.java.advanced.implementor.jar:$LIB/junit-4.11.jar
cd ../java-solutions || exit
javac --module-path=$MODULES info/kgeorgiy/ja/buduschev/implementor/Implementor.java module-info.java -d _build
cd _build || exit
printf "Manifest-Version: 1.0\nMain-Class: info.kgeorgiy.ja.buduschev.implementor.Implementor\nClass-Path: %s/artifacts/info.kgeorgiy.java.advanced.implementor.jar\n" $JAROOT > MANIFEST.MF
jar -c  --file=../implementor.jar --manifest=MANIFEST.MF info/kgeorgiy/ja/buduschev/implementor/* module-info.class
cd .. || exit
rm -rf _build
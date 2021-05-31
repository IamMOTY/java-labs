#!/usr/bin/env bash
root=../../../../../../
mkdir ${root}build
build=${root}build
javac -classpath ${root}lib/junit.jar -d ${build} *.java
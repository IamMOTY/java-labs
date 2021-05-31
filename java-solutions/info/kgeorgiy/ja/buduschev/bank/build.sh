#!/usr/bin/env bash
root=../../../../../../
rm -rf ${root}build
mkdir ${root}build
build=${root}build
lib=${root}lib
javac -cp "$lib/hamcrest-core-1.3.jar:$lib/jsoup-1.8.1.jar:$lib/junit.jar:$lib/quickcheck-0.6.jar" -d ${build} ./*.java
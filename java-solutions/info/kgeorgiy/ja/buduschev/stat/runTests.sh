#!/usr/bin/env bash
./build.sh && \
./cptxt.sh && \
cd ../../../../../../build && \
cp ../lib/junit.jar . && \
lib=../lib && \
java -cp "$lib/hamcrest-core-1.3.jar;$lib/jsoup-1.8.1.jar;$lib/junit.jar:$lib/quickcheck-0.6.jar;." info.kgeorgiy.ja.buduschev.stat.StTests
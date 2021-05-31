#!/usr/bin/env bash
./build.sh
cd ../../../../../../build
cp ../lib/junit.jar .
jar xf junit.jar
lib=../lib
java -cp "$lib/hamcrest-core-1.3.jar:$lib/jsoup-1.8.1.jar:$lib/junit.jar:$lib/quickcheck-0.6.jar:." org.junit.runner.JUnitCore info.kgeorgiy.ja.buduschev.bank.BankTests
#!/usr/bin/env bash
./build.sh
cd ../../../../../../build
cp ../lib/junit.jar .
jar xf junit.jar
java -cp . org.junit.runner.JUnitCore info.kgeorgiy.ja.buduschev.bank.BankTests
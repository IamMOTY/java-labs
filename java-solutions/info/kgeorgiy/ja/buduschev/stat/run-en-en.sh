#!/usr/bin/env bash
./build.sh && \
echo "build succeed" && \
./cptxt.sh && \
cd ../../../../../../build && \
java -cp . info.kgeorgiy.ja.buduschev.stat.Main en-US en-US ./info/kgeorgiy/ja/buduschev/stat/en.txt ../out/en-US-en-US.out.txt
#!/usr/bin/env bash
./build.sh && \
echo "build succeed" && \
./cptxt.sh && \
cd ../../../../../../build && \
java -cp . info.kgeorgiy.ja.buduschev.stat.Main en-US ru-RU ./info/kgeorgiy/ja/buduschev/stat/en.txt ../out/en-US-ru-RU.out.txt
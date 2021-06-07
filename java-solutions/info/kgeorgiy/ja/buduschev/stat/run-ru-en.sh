#!/usr/bin/env bash
./build.sh && \
echo "build succeed" && \
./cptxt.sh && \
cd ../../../../../../build && \
java -cp . info.kgeorgiy.ja.buduschev.stat.Main ru-RU en-US ./info/kgeorgiy/ja/buduschev/stat/ru.txt ../out/ru-RU-en-US.out.txt
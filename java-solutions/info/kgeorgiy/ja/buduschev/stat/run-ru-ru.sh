#!/usr/bin/env bash
./build.sh && \
echo "build succeed" && \
./cptxt.sh && \
cd ../../../../../../build && \
java -cp . info.kgeorgiy.ja.buduschev.stat.Main ru-RU ru-RU ./info/kgeorgiy/ja/buduschev/stat/ru.txt ../out/ru-RU-ru-RU.out.txt
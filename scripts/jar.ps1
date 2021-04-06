$JAROOT=../../java-advanced-2021
$ARTIFACTS=$JAROOT/artifacts
$LIB=$JAROOT/lib
$MODULES=$ARTIFACTS/info.kgeorgiy.java.advanced.base.jar:$ARTIFACTS/info.kgeorgiy.java.advanced.implementor.jar:$LIB/junit-4.11.jar
cd ../java-solutions
javac --module-path=$MODULES info/kgeorgiy/ja/buduschev/implementor/Implementor.java module-info.java -d _build
cd _build
"Manifest-Version: 1.0`r`nMain-Class: info.kgeorgiy.ja.buduschev.implementor.Implementor`r`nClass-Path: {0}/artifacts/info.kgeorgiy.java.advanced.implementor.jar`r`n" -f $JAROOT | Out-File -FilePath ./MANIFEST.MF
jar -c  --file=../implementor.jar --manifest=MANIFEST.MF info/kgeorgiy/ja/buduschev/implementor/* module-info.class
cd .. 
rm _build -r -fo

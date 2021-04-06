$JAROOT=../../java-advanced-2021
$JASROOT=../java-solutions
$MODULES=$JAROOT/modules
$ARTIFACTS=$JAROOT/artifacts
$LIB=$JAROOT/lib
rm javadoc -r -fo
javadoc \
    -private \
    -link https://docs.oracle.com/en/java/javase/11/docs/api/ \
    -d $JASROOT/javadoc \
    $JASROOT/info/kgeorgiy/ja/buduschev/implementor/Implementor.java \
    $MODULES/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/Impler.java \
    $MODULES/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/JarImpler.java \
    $MODULES/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/ImplerException.java
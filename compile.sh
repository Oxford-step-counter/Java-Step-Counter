rm -rf ./build/*
javac -d ./build ./src/*.java
cd build
jar cvf step-counter-module.jar *
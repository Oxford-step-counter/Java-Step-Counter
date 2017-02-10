rm -rf ./build/*
javac -d ./build ./src/*.java
jar cvf step-counter-module.jar ./build/*
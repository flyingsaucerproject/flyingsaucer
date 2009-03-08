#!/bin/bash
echo $basename
java -cp ../lib/dev/bsh-core-2.0b4.jar:../lib/dev/xilize-engine.jar com.centeredwork.xilize.Main -do ../www

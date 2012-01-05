java -cp lib\dev\javasrc.jar;lib\dev\antlrall.jar -Drecurse=yes -Doutdir=doc\javasrc javasrc.app.Pass1 src\java
java -cp lib\dev\javasrc.jar;lib\dev\antlrall.jar -Doutdir=doc\javasrc javasrc.app.Pass2

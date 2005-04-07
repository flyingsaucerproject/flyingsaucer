@echo off

REM important to set the logging off so that the only output is the boxing itself.
REM one easy way to do this is to set the .level on the ConsoleHandler itself
REM could also redirect to a non-console handler, etc.
java -Dxr.util-logging.java.util.logging.ConsoleHandler.level=OFF org.xhtmlrenderer.tool.Boxer %1

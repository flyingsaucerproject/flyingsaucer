@echo off
cd %xr_home%

set _cp=.
for %%i in ( lib\*.jar ) DO call add-temp-cp %%i


java -cp build\classes;build\core-renderer.jar;%_cp%; eeze.Eeze %1
set _cp=

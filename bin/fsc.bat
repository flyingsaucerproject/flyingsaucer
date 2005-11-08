@echo off
cd %xr_home%

set _cp=.
for %%i in ( lib\*.jar ) DO call add-temp-cp %%i
for %%i in ( lib\dev\jdic_win_30092005\*.jar ) DO call add-temp-cp %%i


java -Djava.library.path=lib\dev\jdic_win_30092005 -cp build\classes;build\core-renderer.jar;%_cp%; compare.FSC %1
set _cp=

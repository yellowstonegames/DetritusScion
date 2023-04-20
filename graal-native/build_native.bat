"C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvarsall.bat" x86_amd64 && ^
C:\d\jvm\graal11\bin\native-image.cmd ^
-H:+ReportExceptionStackTraces ^
--report-unsupported-elements-at-runtime ^
--no-fallback ^
-H:IncludeResources=".*.*" ^
-jar DetritusScion.jar

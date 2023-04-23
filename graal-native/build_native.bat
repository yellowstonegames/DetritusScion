C:\d\jvm\graal11\bin\native-image.cmd ^
-H:+ReportExceptionStackTraces ^
--report-unsupported-elements-at-runtime ^
--no-fallback ^
--add-modules jdk.unsupported ^
-H:IncludeResources="((images|music|sfx|shaders)/.*)|(Iosevka.*)|(.*\.dll)" ^
-H:+ForceNoROSectionRelocations ^
-H:-SpawnIsolates ^
-jar ../lwjgl3/build/lib/DetritusScion.jar

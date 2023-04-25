C:\d\jvm\graal11\bin\native-image.cmd ^
-H:+ReportExceptionStackTraces ^
--report-unsupported-elements-at-runtime ^
--no-fallback ^
--add-modules jdk.unsupported ^
-H:+ForceNoROSectionRelocations ^
-H:-SpawnIsolates ^
-H:IncludeResources="((images|music|sfx|shaders)/.*)|(Iosevka.*)|(.*\.dll)" ^
-jar ../lwjgl3/build/lib/DetritusScion.jar

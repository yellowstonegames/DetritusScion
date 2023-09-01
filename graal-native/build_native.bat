C:\d\jvm\graal17\bin\native-image.cmd ^
-H:IncludeResources="((fonts|images|music|sfx|shaders)/.*)|(.*\.dll)|(com/badlogic/gdx/utils/.*\.(png|fnt))" ^
-march=compatibility ^
-jar ../lwjgl3/build/lib/DetritusScion.jar
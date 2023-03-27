package com.github.yellowstonegames.files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.ds.interop.JsonSupport;

/**
 * Interaction point for reading and writing files.
 */
public class FileManager {

    private static FileManager instance;

    private Json json;

    public static FileManager instance() {
        if (instance != null) {
            return instance;
        }

        instance = new FileManager();
        return instance;
    }

    private FileManager() {
        json = new Json();
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);
        json.setQuoteLongValues(true);
        json.setSortFields(true);
        json.setUsePrototypes(false);
        JsonSupport.setNumeralBase(Base.BASE10);
        JsonSupport.registerAll(json);
    }

    /**
     * Reads the file in and returns it as a single string, with \\n at line breaks.
     *
     * @param fileName
     * @param path
     * @return
     */
    public String readFile(String fileName, String path) {
        String localPath = "";
        if (path != null && !path.isEmpty()) {
            localPath = path + "/";
        }
        localPath += fileName;

        Path foundPath = Paths.get(localPath);

        try {
            return Files.readAllLines(foundPath).stream().collect(Collectors.joining("\n"));
        } catch (IOException ex) {
            System.out.println("Could not read path: " + localPath + " with Exception: " + ex.getLocalizedMessage());
            return "";
        }
    }

    /**
     * Just an alternative API in case you want to use libGDX fully.
     * @param fileName why are these...
     * @param path     ...in reverse order?
     * @return the contents of the file by the given name in the given path
     */
    public String readFileGdx(String fileName, String path) {
        String localPath = path != null && !path.isEmpty() ? path + '/' + fileName : fileName;
        try {
            return Gdx.files.local(localPath).readString("UTF8");
        } catch (GdxRuntimeException ex) {
            System.out.println("Could not read path: " + localPath + " with Exception: " + ex.getLocalizedMessage());
            return "";
        }
    }

    /**
     * Writes the string directly into the given file.
     *
     * IOExceptions are swallowed, use the return value to handle success and failure of saving.
     *
     * @param fileName
     * @param path
     * @param contents
     *
     * @return true if the file write operation was a success, false if it was not
     */
    public boolean writeFile(String fileName, String path, String contents) {
        String localPath = "";
        if (path != null && !path.isEmpty()) {
            localPath = path + "/";
        }
        localPath += fileName;

        Path foundPath = Paths.get(localPath);
        try {
            Files.createDirectories(Paths.get(path));
            Files.write(foundPath, Collections.singletonList(contents), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.out.println("Could not write path: " + localPath + "\nException: " + ex.getLocalizedMessage());
            return false;
        }

        return true;
    }
    public boolean writeFileGdx(String fileName, String path, String contents) {
        String localPath = path != null && !path.isEmpty() ? path + '/' + fileName : fileName;
        try {
            Gdx.files.local(localPath).writeString(contents, false, "UTF8");
            return true;
        } catch (GdxRuntimeException ex) {
            System.out.println("Could not write path: " + localPath + "\nException: " + ex.getLocalizedMessage());
            return false;
        }
    }

    /**
     * A Json object with configurations set.
     *
     * @return
     */
    public Json json() {
        return json;
    }

}

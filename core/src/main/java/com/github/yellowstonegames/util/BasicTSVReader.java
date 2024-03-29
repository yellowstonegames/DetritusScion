package com.github.yellowstonegames.util;

import com.badlogic.gdx.files.FileHandle;
import com.github.tommyettinger.digital.TextTools;
import com.github.tommyettinger.ds.ObjectFloatOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectMap;

public class BasicTSVReader {
    public static ObjectList<ObjectFloatOrderedMap<String>> appendTo(ObjectList<ObjectFloatOrderedMap<String>> existing, FileHandle file) {
        String text = file.readString("UTF8");
        String[] lines = text.split("\r?\n"), header = TextTools.split(lines[0], "\t");
        for (int i = 1; i < lines.length; i++) {
            int start = 0, next = lines[i].indexOf('\t');
            if(next == -1) next = lines[i].length();
            ObjectFloatOrderedMap<String> current = new ObjectFloatOrderedMap<>(header.length);
            existing.add(current);
            for (int t = 0; t < header.length; t++) {
                if(start == next) continue;
                float f = Float.parseFloat(lines[i].substring(start, next));
                current.put(header[t], f);
                start = next + 1;
                if(start > lines[i].length()) break;
                next = lines[i].indexOf('\t', start);
                if(next == -1) next = lines[i].length();
            }
        }
        return existing;
    }

    /**
     * Takes a name in the first column, and can put into existing named maps if two names overlap.
     * @param existing
     * @param file
     * @return
     */
    public static ObjectObjectMap<String, ObjectFloatOrderedMap<String>> putInto(
            ObjectObjectMap<String, ObjectFloatOrderedMap<String>> existing, FileHandle file) {
        String text = file.readString("UTF8");
        String[] lines = text.split("\r?\n"), header = TextTools.split(lines[0], "\t");
        for (int i = 1; i < lines.length; i++) {
            int start = 0, next = lines[i].indexOf('\t');
            if(next == -1) next = lines[i].length();
            ObjectFloatOrderedMap<String> current;
            String name = lines[i].substring(start, next);
            if((current = existing.get(name)) == null) {
                current = new ObjectFloatOrderedMap<>(header.length);
                existing.put(name, current);
            }
            for (int t = 1; t < header.length; t++) {
                start = next + 1;
                if(start > lines[i].length()) break;
                next = lines[i].indexOf('\t', start);
                if(next == -1) next = lines[i].length();

                if(start == next) continue;
                float f = Float.parseFloat(lines[i].substring(start, next));
                current.put(header[t], f);
            }
        }
        return existing;
    }
}

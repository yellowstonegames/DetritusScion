package com.github.yellowstonegames.data;

import com.badlogic.gdx.files.FileHandle;
import com.github.tommyettinger.digital.TextTools;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import com.github.tommyettinger.textra.Font;

public class ItemArchetypes {
    public static final ObjectObjectOrderedMap<String, Item> ITEM_MAP = new ObjectObjectOrderedMap<>(64);
    public static final ObjectList<Item> ITEMS = new ObjectList<>(64);

    public static void load(FileHandle file, Font font) {
        String text = file.readString("UTF8");
        String[] lines = text.split("\r?\n"), header = TextTools.split(lines[0], "\t");
        for (int i = 1; i < lines.length; i++) {
            int start = 0, next = lines[i].indexOf('\t');
            if(next == -1) next = lines[i].length();
            Item current;
            String name = lines[i].substring(start, next);
            if((current = ITEM_MAP.get(name)) == null) {
                current = new Item();
                ITEM_MAP.put(name, current);
                ITEMS.add(current);
            }
            start = next + 1;
            if(start > lines[i].length()) break;
            next = lines[i].indexOf('\t', start);
            if(next == -1) next = lines[i].length();

            String markup = lines[i].substring(start, next);
            current.glyph = font.markupGlyph(markup);
            current.actor.glyph = current.glyph;
            current.actor.font = font;

            for (int t = 2; t < header.length; t++) {
                start = next + 1;
                if(start > lines[i].length()) break;
                next = lines[i].indexOf('\t', start);
                if(next == -1) next = lines[i].length();

                if(start == next) continue;
                float f = Float.parseFloat(lines[i].substring(start, next));
                current.baseStats.put(header[t], f);
                current.stats.put(header[t], f);
            }
        }

    }
}

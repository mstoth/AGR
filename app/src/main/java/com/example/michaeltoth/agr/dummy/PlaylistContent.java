package com.example.michaeltoth.agr.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class PlaylistContent {

    /**
     * An array of sample (playlist) items.
     */
    public static final List<SongItem> ITEMS = new ArrayList<SongItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, SongItem> ITEM_MAP = new HashMap<String, SongItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createSongItem(i));
        }
    }

    private static void addItem(SongItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static SongItem createSongItem(int position) {
        return new SongItem(String.valueOf(position), "Song " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * An item representing a song.
     */
    public static class SongItem {
        public final String id;
        public final String name;
        public final String details;

        public SongItem(String id, String name, String details) {
            this.id = id;
            this.name = name;
            this.details = details;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}

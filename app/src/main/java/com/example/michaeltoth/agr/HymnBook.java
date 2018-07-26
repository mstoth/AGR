package com.example.michaeltoth.agr;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class HymnBook {
    private static HymnBook ourInstance;
    private List<Hymn> mHymns;

    public static HymnBook get(Context context) {
        if (ourInstance == null) {
            ourInstance = new HymnBook(context);
        }
        return ourInstance;
    }

    private HymnBook(Context context) {
        mHymns = new ArrayList<>();
        for (int i=0; i<100; i++ ) {
            Hymn hymn = new Hymn();
            hymn.setTitle("Hymn #" + i);
            mHymns.add(hymn);
        }
    }

    public List<Hymn> getHymns() {
        return mHymns;
    }

}

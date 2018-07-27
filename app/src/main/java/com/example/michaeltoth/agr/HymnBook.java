package com.example.michaeltoth.agr;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class HymnBook {
    private static HymnBook ourInstance;
    private List<Hymn> mHymns;
    private List<Hymn> mPerfs;
    private List<Hymn> mRecs;

    public static HymnBook get(Context context) {
        if (ourInstance == null) {
            ourInstance = new HymnBook(context);
        }
        return ourInstance;
    }

    private HymnBook(Context context) {
        mHymns = new ArrayList<>();
        mPerfs = new ArrayList<>();
        mRecs = new ArrayList<>();

        for (int i=0; i<100; i++ ) {
            Hymn hymn = new Hymn();
            hymn.setTitle("Hymn #" + i);
            mHymns.add(hymn);
            Hymn perf = new Hymn();
            hymn.setTitle("Performance #" + i);
            mPerfs.add(hymn);
            Hymn rec = new Hymn();
            rec.setTitle("Selection #" + i);
            mPerfs.add(rec);
        }
    }

    public List<Hymn> getHymns() {
        return mHymns;
    }
    public List<Hymn> getmPerfs() { return mPerfs; }
    public List<Hymn> getRecs() { return mRecs; }


}

package com.example.michaeltoth.agr;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class HymnBook {
    private static HymnBook ourInstance;
    private List<Hymn> mHymns;
    private List<Hymn> mPerfs;
    private List<Hymn> mRecs;

    private ArrayList<String> hymnArrayList;
    private ArrayList<String> perfArrayList;

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
//            Hymn hymn = new Hymn();
//            hymn.setTitle("Hymn #" + i);
//            mHymns.add(hymn);
//            Hymn perf = new Hymn();
//            perf.setTitle("Performance #" + i);
//            mPerfs.add(perf);
            Hymn rec = new Hymn();
            rec.setTitle("Selection #" + i);
            mRecs.add(rec);
        }
    }

    public void setHymns(JSONArray hymnArray) {
        if (hymnArray != null) {
            mHymns.clear();
            hymnArrayList = new ArrayList<String>();
            for (int i = 0; i < hymnArray.length(); i++) {
                try {
                    mHymns.add(new Hymn(hymnArray.getString(i)));
                    hymnArrayList.add(hymnArray.getString(i));
                } catch(JSONException e) {
                    Log.e("ERROR","JSON ERROR",e);
                }
            }
        }

    }
    public void setPerfs(JSONArray hymnArray) {
        if (hymnArray != null) {
            mPerfs.clear();
            perfArrayList = new ArrayList<String>();
            for (int i = 0; i < hymnArray.length(); i++) {
                try {
                    mPerfs.add(new Hymn(hymnArray.getString(i)));
                    perfArrayList.add(hymnArray.getString(i));
                } catch(JSONException e) {
                    Log.e("ERROR","JSON ERROR",e);
                }
            }
        }

    }
    public List<Hymn> getHymns() {
        return mHymns;
    }
    public List<Hymn> getmPerfs() {
        return mPerfs;
    }
    public List<Hymn> getRecs() {
        return mRecs;
    }

    public String[] getHymnArray() {
        String[] hArray = new String[mHymns.size()];
        int i;
        for (i=0;i<mHymns.size();i++) {
            hArray[i]=hymnArrayList.get(i);
        }
        return hArray;
    }

    public String[] getPerfArray() {
        String[] hArray = new String[mPerfs.size()];
        int i;
        for (i=0;i<mPerfs.size();i++) {
            hArray[i]=perfArrayList.get(i);
        }
        return hArray;
    }


}

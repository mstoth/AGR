package com.example.michaeltoth.agr;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HymnBook {
    private static HymnBook ourInstance;
    private List<Hymn> mHymns;
    private List<Hymn> mPerfs;
    private List<Hymn> mRecs;

    private ArrayList<String> mRecordings;
    private ArrayList<String> mPlaylists;
    private ArrayList<String> hymnArrayList;
    private ArrayList<String> perfArrayList;
    private ArrayList<String> recArrayList;

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
        mPlaylists = new ArrayList<String>();
        recArrayList = new ArrayList<String>();
        mRecordings = new ArrayList<>();
        hymnArrayList = new ArrayList<String>();
        perfArrayList = new ArrayList<String>();

        for (int i=0; i<100; i++ ) {
            Hymn hymn = new Hymn();
            hymn.setTitle("Hymn #" + i);
            mHymns.add(hymn);
            hymnArrayList.add(" ");
            Hymn perf = new Hymn();
            perf.setTitle("Performance #" + i);
            mPerfs.add(perf);
            perfArrayList.add(" ");
//            Hymn rec = new Hymn();
//            rec.setTitle("Selection " + (i+1));
//            mRecs.add(rec);
//            recArrayList.add("Selection " + (i+1));
//            mRecordings.add("Selection " + String.format("%02d",i+1));
        }
    }

    public void setHymns(JSONArray hymnArray) {
        if (hymnArray != null) {
            mHymns.clear();
            hymnArrayList = new ArrayList<String>();
            for (int i = 0; i < hymnArray.length(); i++) {
                String s;
                s = "";
                try {
                    s = (String) hymnArray.get(i);
                } catch (JSONException e) {
                    Log.e("ERROR","JSON ERROR",e);
                }
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

    public void setPlaylists(JSONArray playListArray) {
        if (playListArray != null) {
            mPlaylists.clear();
            for (int i=0; i<playListArray.length(); i++) {
                String s;
                s = "";
                try {
                    s = (String) playListArray.get(i);
                } catch (JSONException e) {
                    Log.e("ERROR","JSONERROR ",e);
                }
                if (s.contains(".ZIP") || s.contains(".zip")) {
                    try {
                        mPlaylists.add((String) playListArray.get(i));
                    } catch (JSONException e) {
                        Log.e("ERROR","JSON ERROR ",e);
                    }
                }
            }
        }
    }


    public void setRecordings(JSONArray recArray) {
        if (recArray != null) {
            mRecordings.clear();
            mRecs.clear();
            recArrayList = new ArrayList<String>();
            for (int i = 0; i < recArray.length(); i++) {
                String s;
                s = "";
                try {
                    s = (String) recArray.get(i);
                } catch (JSONException e) {
                    Log.e("ERROR","JSON ERROR",e);
                }
                if (s.contains(".MID") || s.contains(".mid")) {
                    try {
                        mRecs.add(new Hymn(s));
                        mRecordings.add(s);
                        recArrayList.add(s);
                    } catch(Exception e) {
                        Log.e("ERROR","ERROR",e);
                    }
                }
            }
            Collections.sort(mRecordings);

        }

    }

    public void addRecording(String hymnTitle) {
        mRecordings.add(hymnTitle);
        recArrayList.add(hymnTitle);
        Collections.sort(mRecordings);
    }

    public void removeRecording(String hymnTitle) {
        int idx;
        idx = mRecordings.indexOf(hymnTitle);
        if (idx>=0) {
            mRecordings.remove(idx);
        }

        idx = recArrayList.indexOf(hymnTitle);
        if (idx >=0) {
            recArrayList.remove(idx);
        }

        idx = mRecs.indexOf(hymnTitle);
        if (idx >= 0) {
            mRecs.remove(hymnTitle);
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

    public ArrayList<String> getRecArrayList() {
        return recArrayList;
    }

    public String[] getPlaylistArray() {
        String[] hArray = new String[mPlaylists.size()];
        for (int i=0; i<mPlaylists.size(); i++) {
            hArray[i]=mPlaylists.get(i);
        }
        return hArray;
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

    public String[] getRecArray() {
        String[] hArray = new String[mRecs.size()];
        int i;
        for (i=0;i<mRecs.size();i++) {
            hArray[i]=recArrayList.get(i);
        }
        return hArray;
    }

    public String[] getRecordingArray() {
         String[] hArray = new String[mRecordings.size()];
        int i;
        for (i=0;i<mRecordings.size();i++) {
            hArray[i]=mRecordings.get(i);
        }
        return hArray;
    }


}

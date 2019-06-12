package com.example.michaeltoth.agr;

public class Playlist {
    private String mTitle;
    public String getTitle() { return mTitle; }
    public void setTitle(String title) { this.mTitle = title; }
    public Playlist() {

    }
    public Playlist(String title) {
        mTitle = title;
    }
}

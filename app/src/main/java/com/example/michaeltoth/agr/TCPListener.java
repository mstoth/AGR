package com.example.michaeltoth.agr;


import org.json.JSONObject;

public interface TCPListener {
    public void onTCPMessageRecieved(JSONObject message);
    public void onTCPConnectionStatusChanged(boolean isConnectedNow);
}
package com.example.michaeltoth.agr;

import android.support.v4.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

public class PlayerListFragment extends Fragment implements TCPListener {


    @Override
    public void onTCPMessageRecieved(JSONObject message) {
        final JSONObject theMessage = message;
        try {
            String messageSubTypeString;
            JSONObject obj = theMessage;
            final String messageTypeString = obj.getString("mtype");
            if (messageTypeString.equals("CPPP")) {
                messageSubTypeString = obj.getString("mstype");
            } else {
                messageSubTypeString = "";
            }
            if (messageTypeString.equals("CPPP")) {
                // handle message sub-type
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onTCPConnectionStatusChanged(boolean isConnectedNow) {
        // Nothing to do
    }

}

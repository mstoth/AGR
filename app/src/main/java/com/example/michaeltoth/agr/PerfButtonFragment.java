package com.example.michaeltoth.agr;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class PerfButtonFragment extends Fragment implements TCPListener{
    private ImageButton mPerfVolUpButton;
    private ImageButton mPerfVolDownButton;
    private ImageButton mPerfPlayButton;
    private TextView mPerfVolText;
    private int currentVolume, volMax, volMin;
    private TCPCommunicator tcpClient;
    private Handler UIHandler = new Handler();
    private boolean playing, remoteActive;
    private int currentSong;
    private TextView mPerfPlayText;
    private View myView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_perf_buttons,container,false);
        mPerfVolUpButton = myView.findViewById(R.id.perf_vol_up);
        mPerfVolDownButton = myView.findViewById(R.id.perf_vol_down);
        mPerfVolText = myView.findViewById(R.id.perf_vol_text);
        mPerfPlayText = myView.findViewById(R.id.perf_play_text);
        tcpClient = TCPCommunicator.getInstance();

        // PLAY BUTTON
        mPerfPlayButton = (ImageButton) myView.findViewById(R.id.perf_play_button);
        mPerfPlayButton.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (remoteActive) {
                    if (playing) {
                        playing = false;
                        mPerfPlayText.setText("Play");
                        mPerfPlayButton.setBackgroundResource(R.drawable.playt);
                        mPerfVolText.setEnabled(true);
                        mPerfVolDownButton.setEnabled(true);
                        mPerfVolUpButton.setEnabled(true);
                        tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"stop\"}",UIHandler,getContext());
                    } else {
                        playing = true;
                        mPerfVolText.setEnabled(false);
                        mPerfVolDownButton.setEnabled(false);
                        mPerfVolUpButton.setEnabled(false);

                        mPerfPlayText.setText("Stop");
                        mPerfPlayButton.setBackgroundResource(R.drawable.stopt);
                        tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"play\"}", UIHandler, getContext());
                    }

                }  else {
                    Toast.makeText(getContext(),"Remote is not Active.",Toast.LENGTH_SHORT).show();
                }
            }
        });


        mPerfVolUpButton.setOnClickListener(new ImageButton.OnClickListener(){
            @Override
            public void onClick(View view) {
                currentVolume = currentVolume + 1;
                if (currentVolume > volMax) {
                    currentVolume = volMax;
                }
                String msg = "{\"mtype\":\"CPPP\",\"mstype\":\"preludeplayer_volume_limit\",\"value\":" + currentVolume + "}";
                tcpClient.writeStringToSocket(msg,UIHandler,getContext());
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"preludeplayer_volume_limit\"}",UIHandler,getContext());
            }
        });

        mPerfVolDownButton.setOnClickListener(new ImageButton.OnClickListener(){
            @Override
            public void onClick(View view) {
                currentVolume = currentVolume - 1;
                if (currentVolume < volMin) {
                    currentVolume = volMin;
                }
                String msg = "{\"mtype\":\"CPPP\",\"mstype\":\"preludeplayer_volume_limit\",\"value\":" + currentVolume + "}";
                tcpClient.writeStringToSocket(msg,UIHandler,getContext());
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"preludeplayer_volume_limit\"}",UIHandler,getContext());

            }
        });

        tcpClient.addListener(this);

        TCPCommunicator.TCPWriterErrors e;
        e=tcpClient.writeStringToSocket("{\"mtype\":\"GIRQ\"}",UIHandler,getContext());
        if (e== TCPCommunicator.TCPWriterErrors.otherProblem) {
            // no connection
            Toast.makeText(getContext(),"No Connection to Organ...",Toast.LENGTH_SHORT).show();
            tcpClient.init("192.168.1.4",10002);
            TCPCommunicator.addListener(this);
        }

        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"seqeng_remote_active\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"preludeplayer_volume_limit\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"seqeng_mode\",\"value\":2}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"stop\"}",UIHandler,getContext());

        return myView;

    }


    @Override
    public void onTCPMessageRecieved(JSONObject message) {
        final JSONObject theMessage=message;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
        try {
            String messageSubTypeString;
            JSONObject obj = theMessage;
            final String messageTypeString=obj.getString("mtype");
            if (messageTypeString.equals("CPPP")) {
                messageSubTypeString = obj.getString("mstype");
            } else {
                messageSubTypeString = "";
            }
            if (messageTypeString.equals("CPPP")) {

                if (messageSubTypeString.equals("seqeng_remote_active")) {
                    remoteActive = obj.getBoolean("value");
                    if (!remoteActive) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(),"Remote is not active.",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                if (messageSubTypeString.equals("preludeplayer_song_current")) {
                    final int cs = obj.getInt("value");
                    currentSong = cs;
                }
                if (messageSubTypeString.equals("preludeplayer_volume_limit")) {
                    volMax = obj.getInt("range_max");
                    volMin = obj.getInt("range_min");
                    currentVolume = obj.getInt("value");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPerfVolText.setText("Volume: " + currentVolume);
                        }
                    });
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onTCPConnectionStatusChanged(boolean isConnectedNow) {
        // Log.d("TIMER","isConnectedNow is " + isConnectedNow);

    }
}

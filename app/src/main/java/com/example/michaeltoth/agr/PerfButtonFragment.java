package com.example.michaeltoth.agr;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perf_buttons,container,false);
        mPerfVolUpButton = view.findViewById(R.id.perf_vol_up);
        mPerfVolDownButton = view.findViewById(R.id.perf_vol_down);
        mPerfVolText = view.findViewById(R.id.perf_vol_text);
        mPerfPlayText = view.findViewById(R.id.perf_play_text);

        // PLAY BUTTON
        mPerfPlayButton = (ImageButton) view.findViewById(R.id.perf_play_button);
        mPerfPlayButton.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (remoteActive) {
                    if (playing) {
                        playing = false;
                        mPerfPlayText.setText("Play");
                        mPerfPlayButton.setBackgroundResource(R.drawable.playt2);
                        tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"stop\"}",UIHandler,getContext());
                    } else {
                        playing = true;
                        mPerfPlayText.setText("Stop");
                        mPerfPlayButton.setBackgroundResource(R.drawable.stopt);
                        tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"play\"}", UIHandler, getContext());
                    }

                }  else {
                    Toast.makeText(getContext(),"Remote is not Active.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        // VOLUME BUTTON
//         mPerfVolUpButton.setOnLongClickListener(new ImageButton.OnLongClickListener() {
//
//            @Override
//            public boolean onLongClick(View view) {
//                Toast.makeText(getContext(),"Long Click on Up Button",Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        });


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

        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"seqeng_remote_active\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"preludeplayer_volume_limit\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"seqeng_mode\",\"value\":2}",UIHandler,getContext());



        return view;



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

    }
}

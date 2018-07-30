package com.example.michaeltoth.agr;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HymnButtonFragment extends Fragment implements TCPListener {
    private ImageButton mHymnVolUpButton;
    private ImageButton mHymnVolDownButton;
    private TextView mHymnVolText;
    private ImageButton mPlayButton;
    private TCPCommunicator tcpClient;
    private Handler UIHandler = new Handler();
    private boolean remoteActive;
    int currentSong;
    int currentVolume;
    int currentPitch;
    boolean playing;
    boolean paused;
    int volMax;
    int volMin;
    int versesMin;
    int versesMax;
    int tempoMin;
    int tempoMax;
    int pitchMin;
    int pitchMax;

    TextView volTextView;
    View myView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_hymn_buttons,container,false);
        mHymnVolUpButton = myView.findViewById(R.id.hymn_vol_up);
        mHymnVolDownButton = myView.findViewById(R.id.hymn_vol_down);
        mHymnVolText = myView.findViewById(R.id.hymn_vol_text);
        tcpClient = TCPCommunicator.getInstance();
        remoteActive = false;


        mPlayButton = myView.findViewById(R.id.hymn_play_button);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playing) {
                    playing = false;
                    mPlayButton.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                    tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"stop\"}",UIHandler,getContext());
                } else {
                    if (paused) {
                        tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"unpause\"}",UIHandler,getContext());
                    } else {
                        playing = true;
                        mPlayButton.setBackgroundResource(R.drawable.ic_stop_black_24dp);
                        tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"play\"}", UIHandler, getContext());
                    }
                }
            }
        });


        tcpClient.addListener(this);
        sendHymnStartup();
        return myView;
    }

    void sendHymnStartup() {
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"seqeng_remote_active\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_song_current\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"seqeng_mode\",\"value\":1}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"stop\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"seqeng_status\",\"value\":1}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_volume_limit\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_tempo_adjust\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_verses_to_play\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_pitch_adjust\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"organ_lds_is\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_intro_play\"}",UIHandler,getContext());
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
                if (messageSubTypeString.equals("hymnplayer_song_current")) {
                    final int cs = obj.getInt("value");
                    currentSong = cs;
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            hymnsWheelView.setCurrentItem(currentSong);
//
//                        }
//                    });
                }
                if (messageSubTypeString.equals("hymnplayer_volume_limit")) {
                    final int vol = obj.getInt("value");
                    final int vMax = obj.getInt("range_max");
                    final int vMin = obj.getInt("range_min");
                    currentVolume = vol;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            volMax = vMax;
                            volMin = vMin;
                            volTextView = (TextView) myView.findViewById(R.id.hymn_vol_text);
                            volTextView.setText("Volume: " + Integer.toString(vol));
                        }
                    });
                }
                if (messageSubTypeString.equals("hymnplayer_intro_play")) {
                    final boolean intro = obj.getBoolean("value");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Switch sw = (Switch) myView.findViewById(R.id.include_intro_switch);
                            if (intro) {
                                sw.setChecked(true);
                            } else {
                                sw.setChecked(false);
                            }
                        }
                    });
                }
                if (messageSubTypeString.equals("hymnplayer_tempo_adjust")) {
                    final int tempo = obj.getInt("value");
                    final int tMin = obj.getInt("range_min");
                    final int tMax = obj.getInt("range_max");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tempoMin = tMin;
                            tempoMax = tMax;
                            TextView tempoTextView = (TextView) myView.findViewById(R.id.hymn_tempo_text);
                            tempoTextView.setText("Tempo: " + Integer.toString(tempo));
                        }
                    });
                }
                if (messageSubTypeString.equals("hymnplayer_verses_to_play")) {
                    final int verses = obj.getInt("value");
                    final int vMin = obj.getInt("range_min");
                    final int vMax = obj.getInt("range_max");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            versesMin = vMin;
                            versesMax = vMax;
                            TextView versesTextView = (TextView) myView.findViewById(R.id.hymn_verses_text);
                            versesTextView.setText("Verses: " + Integer.toString(verses));
                        }
                    });
                }
                if (messageSubTypeString.equals("organ_lds_is")) {
                    final boolean lds = obj.getBoolean("value");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RadioGroup langView = (RadioGroup) myView.findViewById(R.id.language_group);
                            if (lds) {
                                langView.setVisibility(View.VISIBLE);
                            } else {
                                langView.setVisibility(View.INVISIBLE);
                            }

                        }
                    });
                    if (lds) {
                        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"organ_language_current\"}",UIHandler,getContext());
                    }
                }
                if (messageSubTypeString.equals("organ_language_current")) {
                    final int current_language = obj.getInt("value");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RadioGroup radioGroup = myView.findViewById(R.id.language_group);

                            switch (current_language) {
                                case 0: {
                                    radioGroup.check(R.id.english_radio);
                                    break;
                                }
                                case 1: {
                                    radioGroup.check(R.id.french_radio);
                                    break;
                                }
                                case 2: {
                                    radioGroup.check(R.id.portuguese_radio);
                                    break;
                                }
                                case 3: {
                                    radioGroup.check(R.id.spanish_radio);
                                    break;
                                }
                            }
                        }
                    });
                }
                if (messageSubTypeString.equals("seqeng_status")) {
                    final int status = obj.getInt("value");
                    // final TextView hs = myView.findViewById(R.id.hymn_status);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (status) {
                                case 0: {
                                    // stopped
                                    mPlayButton.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                                    // hs.setText(R.string.play_status);
                                    playing = false;
                                    paused = false;
                                    break;
                                }
                                case 1: {
                                    // playing
                                    mPlayButton.setBackgroundResource(R.drawable.ic_stop_black_24dp);
                                    // hs.setText(R.string.stop_status);
                                    playing = true;
                                    paused = false;
                                    break;
                                }
                                case 2: {
                                    // seekdone
                                    playing = false;
                                    paused = false;
                                    break;
                                }
                                case 3: {
                                    // paused
                                    mPlayButton.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                                    // hs.setText(R.string.continue_status);
                                    playing = false;
                                    paused = true;
                                    break;
                                }
                                case 4: {
                                    // transition
                                    playing = false;
                                    paused = false;
                                    break;
                                }
                            }
                        }
                    });
                }
                if (messageSubTypeString.equals("hymnplayer_pitch_adjust")) {
                    final int pitch = obj.getInt("value");
                    final int pMin = obj.getInt("range_min");
                    final int pMax = obj.getInt("range_max");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentPitch = pitch;
                            pitchMin = pMin;
                            pitchMax = pMax;
                            TextView pitchTextView = (TextView) myView.findViewById(R.id.hymn_pitch_text);
                            pitchTextView.setText("Pitch: " + Integer.toString(pitch));
                        }
                    });
                }
//                if (messageSubTypeString.equals("hymnplayer_hymn_list")) {
//                    final ArrayList<String> listdata = new ArrayList<String>();
//                    listdata.clear();
//                    final JSONArray hymns = obj.getJSONArray("value");
//                    if (hymns != null) {
//                        for (int i=0;i<hymns.length();i++){
//                            listdata.add(hymns.getString(i));
//                        }
//                        getActivity().runOnUiThread(new Runnable() {
//                            public void run() {
//                                hymnBook.setHymns(listdata);
//                                final WheelView hymnsWheelView = (WheelView) findViewById(R.id.hymn_recycler_view);
//                                updateHymns(hymnsWheelView,hymnBook,0);
////                                HymnAdapter4 hymnAdapter;
////                                hymnAdapter = new HymnAdapter4(HymnPlayerActivity.this,hymnBook);
////                                // final WheelView hymnsWheelView = (WheelView) findViewById(R.id.hymn_recycler_view);
////                                hymnsWheelView.setViewAdapter(hymnAdapter);
//
////                                hymnsWheelView.refreshDrawableState();
////                                hymnsWheelView.notifyAll();
//// mHymnRecyclerView.setAdapter(mAdapter);
//                                // mAdapter.notifyDataSetChanged();
//                            }
//                        });
//                    }
//                }
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

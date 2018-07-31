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
import android.widget.CompoundButton;
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
    private ImageButton mHymnVolUpButton, mHymnVolDownButton;
    private ImageButton mHymnTempoUpButton, mHymnTempoDownButton;
    private ImageButton mHymnVersesUpButton, mHymnVersesDownButton;
    private ImageButton mHymnPitchUpButton, mHymnPitchDownButton;

    private Button englishButton, frenchButton, portugueseButton, spanishButton;

    private TextView mHymnVolText,mTempoText,mVersesText,mPlayButtonTextView,mPitchTextView;
    private ImageButton mPlayButton;
    private TCPCommunicator tcpClient;
    private Handler UIHandler = new Handler();
    private boolean remoteActive;
    int currentSong, currentVolume, currentPitch, currentTempo, currentVerses;
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
        mHymnVolText = myView.findViewById(R.id.hymn_vol_text);
        mTempoText = myView.findViewById(R.id.hymn_tempo_text);
        mVersesText = myView.findViewById(R.id.hymn_verses_text);
        mPitchTextView = myView.findViewById(R.id.hymn_pitch_text);
        tcpClient = TCPCommunicator.getInstance();
        remoteActive = false;
        mPlayButtonTextView = myView.findViewById(R.id.play_button_text_view);
        englishButton = (Button) myView.findViewById(R.id.english_radio);
        frenchButton = (Button) myView.findViewById(R.id.french_radio);
        portugueseButton = (Button) myView.findViewById(R.id.portuguese_radio);
        spanishButton = (Button) myView.findViewById(R.id.spanish_radio);

        // BUTTONS

        // VOLUME BUTTON
        // UP
        mHymnVolUpButton = myView.findViewById(R.id.hymn_vol_up);
        mHymnVolUpButton.setOnClickListener(new ImageButton.OnClickListener(){
            @Override
            public void onClick(View view) {
                currentVolume = currentVolume + 1;
                if (currentVolume > volMax) {
                    currentVolume = volMax;
                }
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_volume_limit\",\"value\":" + Integer.toString(currentVolume) + "}",UIHandler,getContext());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHymnVolText.setText("Volume: " + currentVolume);
                    }
                });
            }
        });

        // DOWN
        mHymnVolDownButton = myView.findViewById(R.id.hymn_vol_down);
        mHymnVolDownButton.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View view) {
                currentVolume = currentVolume - 1;
                if (currentVolume < volMin) {
                    currentVolume = volMin;
                }
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_volume_limit\",\"value\":" + Integer.toString(currentVolume) + "}",UIHandler,getContext());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHymnVolText.setText("Volume: " + currentVolume);
                    }
                });
            }
        });

        // TEMPO BUTTON
        // UP
        mHymnTempoUpButton = myView.findViewById(R.id.hymn_tempo_up);
        mHymnTempoUpButton.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View view) {
                currentTempo = currentTempo + 1;
                if (currentTempo > tempoMax) {
                    currentTempo = tempoMax;
                }
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_tempo_adjust\",\"value\":" + Integer.toString(currentTempo) + "}",UIHandler,getContext());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTempoText.setText("Tempo: " + currentTempo);
                    }
                });
            }
        });
        //DOWN
        mHymnTempoDownButton = myView.findViewById(R.id.hymn_tempo_down);
        mHymnTempoDownButton.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View view) {
                currentTempo = currentTempo - 1;
                if (currentTempo < tempoMin) {
                    currentTempo = tempoMin;
                }
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_tempo_adjust\",\"value\":" + Integer.toString(currentTempo) + "}",UIHandler,getContext());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTempoText.setText("Tempo: " + currentTempo);
                    }
                });
            }
        });

        // VERSES BUTTON
        // UP
        mHymnVersesUpButton = myView.findViewById(R.id.hymn_verses_up);
        mHymnVersesUpButton.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View view) {
                currentVerses = currentVerses + 1;
                if (currentVerses > versesMax) {
                    currentVerses = versesMax;
                }
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_verses_to_play\",\"value\":" + Integer.toString(currentVerses) + "}",UIHandler,getContext());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVersesText.setText("Verses: " + currentVerses);
                    }
                });
            }
        });
        // DOWN
        mHymnVersesDownButton = myView.findViewById(R.id.hymn_verses_down);
        mHymnVersesDownButton.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View view) {
                currentVerses = currentVerses - 1;
                if (currentVerses < versesMin) {
                    currentVerses = versesMin;
                }
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_verses_to_play\",\"value\":" + Integer.toString(currentVerses) + "}",UIHandler,getContext());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVersesText.setText("Verses: " + currentVerses);
                    }
                });
            }
        });

        // PITCH BUTTON
        // UP
        mHymnPitchUpButton = myView.findViewById(R.id.hymn_pitch_up);
        mHymnPitchUpButton.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View view) {
                currentPitch = currentPitch + 1;
                if (currentPitch > pitchMax) {
                    currentPitch = pitchMax;
                }
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_pitch_adjust\",\"value\":" + Integer.toString(currentPitch) + "}",UIHandler,getContext());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPitchTextView.setText("Pitch: " + currentPitch);
                    }
                });
            }
        });
        // DOWN
        mHymnPitchDownButton = myView.findViewById(R.id.hymn_pitch_down);
        mHymnPitchDownButton.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View view) {
                currentPitch = currentPitch - 1;
                if (currentPitch > pitchMax) {
                    currentPitch = pitchMax;
                }
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_pitch_adjust\",\"value\":" + Integer.toString(currentPitch) + "}",UIHandler,getContext());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPitchTextView.setText("Pitch: " + currentPitch);
                    }
                });
            }
        });

        Switch introSwitch = (Switch) myView.findViewById(R.id.include_intro_switch);
        introSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_intro_play\",\"value\":true}",UIHandler,getContext());
                } else {
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_intro_play\",\"value\":false}",UIHandler,getContext());
                }
            }
        });

        // LANGUAGE BUTTONS
        englishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"organ_language_current\",\"value\":0}",UIHandler,getContext());
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"organ_language_current\",\"write\":true}",UIHandler,getContext());
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_hymn_list\"}",UIHandler,getContext());
            }
        });

        frenchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"organ_language_current\",\"value\":1}",UIHandler,getContext());
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"organ_language_current\",\"write\":true}",UIHandler,getContext());
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_hymn_list\"}",UIHandler,getContext());
            }
        });

        portugueseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"organ_language_current\",\"value\":2}",UIHandler,getContext());
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"organ_language_current\",\"write\":true}",UIHandler,getContext());
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_hymn_list\"}",UIHandler,getContext());
            }
        });

        spanishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"organ_language_current\",\"value\":3}",UIHandler,getContext());
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"organ_language_current\",\"write\":true}",UIHandler,getContext());
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_hymn_list\"}",UIHandler,getContext());
            }
        });

        RadioGroup radioGroup = myView.findViewById(R.id.language_group);
        radioGroup.check(R.id.english_radio);


        // PLAY BUTTON
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
                                    mPlayButtonTextView.setText("Play");
                                    break;
                                }
                                case 1: {
                                    // playing
                                    mPlayButton.setBackgroundResource(R.drawable.ic_stop_black_24dp);
                                    // hs.setText(R.string.stop_status);
                                    playing = true;
                                    paused = false;
                                    mPlayButtonTextView.setText("Stop");
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
                                    mPlayButtonTextView.setText("Continue");
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

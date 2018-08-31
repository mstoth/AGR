package com.example.michaeltoth.agr;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
    private Handler repeatUpdateHandler = new Handler();
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;

    TextView volTextView;
    View myView;

    static final int REP_DELAY=50;

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
        // LONG CLICK
        mHymnTempoUpButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mAutoIncrement = true;
                repeatUpdateHandler.post( new RptTempoUpdater() );
                return false;
            }
        });
        mHymnTempoUpButton.setOnTouchListener( new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
                        && mAutoIncrement ){
                    mAutoIncrement = false;
                }
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_tempo_adjust\",\"value\":" + Integer.toString(currentTempo) + "}",UIHandler,getContext());
                return false;
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTempoText.setText("Tempo: " + currentTempo);
                    }
                });
            }
        });
        // LONG CLICK
        mHymnTempoDownButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mAutoDecrement = true;
                repeatUpdateHandler.post( new RptTempoUpdater() );
                return false;
            }
        });
        mHymnTempoDownButton.setOnTouchListener( new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
                        && mAutoDecrement ){
                    mAutoDecrement = false;
                }
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_tempo_adjust\",\"value\":" + Integer.toString(currentTempo) + "}",UIHandler,getContext());
                return false;
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPitchTextView.setText("Pitch: " + currentPitch);
                        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_pitch_adjust\",\"value\":" + Integer.toString(currentPitch) + "}",UIHandler,getContext());
                    }
                });
            }
        });
        // LONG CLICK
        mHymnPitchUpButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mAutoIncrement = true;
                repeatUpdateHandler.post( new RptPitchUpdater() );
                return false;
            }
        });
        mHymnPitchUpButton.setOnTouchListener( new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
                        && mAutoIncrement ){
                    mAutoIncrement = false;
                }
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_pitch_adjust\",\"value\":" + Integer.toString(currentPitch) + "}",UIHandler,getContext());
                return false;
            }
        });

        // DOWN
        mHymnPitchDownButton = myView.findViewById(R.id.hymn_pitch_down);
        mHymnPitchDownButton.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View view) {
                currentPitch = currentPitch - 1;
                if (currentPitch < pitchMin) {
                    currentPitch = pitchMin;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPitchTextView.setText("Pitch: " + currentPitch);
                        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_pitch_adjust\",\"value\":" + Integer.toString(currentPitch) + "}",UIHandler,getContext());

                    }
                });
            }
        });
        // LONG CLICK
        mHymnPitchDownButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mAutoDecrement = true;
                repeatUpdateHandler.post( new RptPitchUpdater() );
                return false;
            }
        });
        mHymnPitchDownButton.setOnTouchListener( new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
                        && mAutoDecrement ){
                    mAutoDecrement = false;
                }
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_pitch_adjust\",\"value\":" + Integer.toString(currentPitch) + "}",UIHandler,getContext());
                return false;
            }
        });

        // INTRODUCTION SWITCH
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
                if (remoteActive) {
                    if (playing) {
                        playing = false;
                        mPlayButton.setBackgroundResource(R.drawable.playt);
                        tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"stop\"}",UIHandler,getContext());
                    } else {
                        if (paused) {
                            tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"unpause\"}",UIHandler,getContext());
                        } else {
                            playing = true;
                            mPlayButton.setBackgroundResource(R.drawable.stopt);
                            tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"play\"}", UIHandler, getContext());
                        }
                    }

                } else {
                    Toast.makeText(getContext(),"Remote is not active.",Toast.LENGTH_SHORT).show();
                }
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

    // UPDATERS
    // TEMPO
    class RptTempoUpdater implements Runnable {
        public void run() {
            if( mAutoIncrement ){
                incrementTempo();
                repeatUpdateHandler.postDelayed( new RptTempoUpdater(), REP_DELAY );
            } else if( mAutoDecrement ){
                decrementTempo();
                repeatUpdateHandler.postDelayed( new RptTempoUpdater(), REP_DELAY );
            }
        }
    }

    public void incrementTempo() {
        currentTempo = currentTempo + 1;
        if (currentTempo > tempoMax) {
            currentTempo = tempoMax;
        }
        mTempoText.setText("Tempo: " + currentTempo);
    }

    public void decrementTempo() {
        currentTempo = currentTempo - 1;
        if (currentTempo < tempoMin) {
            currentTempo = tempoMin;
        }
        mTempoText.setText("Tempo: " + currentTempo);
    }

    // PITCH
    class RptPitchUpdater implements Runnable {
        public void run() {
            if( mAutoIncrement ){
                incrementPitch();
                repeatUpdateHandler.postDelayed( new RptPitchUpdater(), REP_DELAY );
            } else if( mAutoDecrement ){
                decrementPitch();
                repeatUpdateHandler.postDelayed( new RptPitchUpdater(), REP_DELAY );
            }
        }
    }

    public void incrementPitch() {
        currentPitch = currentPitch + 1;
        if (currentPitch > pitchMax) {
            currentPitch = pitchMax;
        }
        mPitchTextView.setText("Pitch: " + currentPitch);
    }

    public void decrementPitch() {
        currentPitch = currentPitch - 1;
        if (currentPitch < pitchMin) {
            currentPitch = pitchMin;
        }
        mPitchTextView.setText("Pitch: " + currentPitch);
    }

    @Override
    public void onTCPMessageRecieved(JSONObject message) {
        final JSONObject theMessage=message;
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
                            currentVerses = verses;
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
                            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_hymn_list\"}",UIHandler,getContext());

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
                                    mPlayButton.setBackgroundResource(R.drawable.playt);
                                    // hs.setText(R.string.play_status);
                                    playing = false;
                                    paused = false;
                                    mPlayButtonTextView.setText("Play");
                                    break;
                                }
                                case 1: {
                                    // playing
                                    mPlayButton.setBackgroundResource(R.drawable.stopt);
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
                                    mPlayButton.setBackgroundResource(R.drawable.playt);
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
        // Log.d("TIMER","isConnectedNow is " + isConnectedNow);
    }
}

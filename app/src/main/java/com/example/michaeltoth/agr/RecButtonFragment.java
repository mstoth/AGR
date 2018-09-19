package com.example.michaeltoth.agr;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.michaeltoth.agr.widget.WheelView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RecButtonFragment extends Fragment implements TCPListener, IRecButtonFragment {
    private boolean remoteActive;
    private boolean selectionExists;
    private View myView;
    private Button playButton;
    private Handler UIHandler = new Handler();
    private HymnBook hymnBook;
    private boolean scrolling = false;
    private TCPCommunicator tcpClient = TCPCommunicator.getInstance();
    private Button recordButton, stopButton, deleteButton;
    private TextView counterTextView, statusTextView;
    private int currentSelection;
    private ArrayList<String> mediaDirList;
    private ArrayList<String> midiFiles;
    private ArrayList<String> titles;
    WheelView hymnsWheelView;
    private IMainActivity mIMainActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIMainActivity = (IMainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rec_buttons,container,false);
        remoteActive = false;
        myView = view;

        recordButton = (Button) myView.findViewById(R.id.rec_record_button);
        playButton = (Button) myView.findViewById(R.id.rec_play_button);
        stopButton = (Button) myView.findViewById(R.id.rec_stop_button);
        deleteButton = (Button) myView.findViewById(R.id.rec_delete_button);

        statusTextView = (TextView) myView.findViewById(R.id.status_text_view);
        counterTextView = (TextView) myView.findViewById(R.id.counter_text_view);
        // ON CLICK METHODS
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (remoteActive) {
                    tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"record\"}", UIHandler,getContext());
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_current\",\"value\":\"/work\"}", UIHandler,getContext());
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());

                } else {
                    Toast.makeText(getActivity(),"Remote is not active",Toast.LENGTH_SHORT).show();
                }

            }
        });


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (remoteActive) {
                    if (playButton.getText().equals("Pause")) {
                        playButton.setText("Continue");
                        playButton.setEnabled(true);
                        tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"pause\"}", UIHandler,getContext());
                    } else {
                        if (playButton.getText().equals("Continue")) {
                            playButton.setText("Pause");
                            tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"unpause\"}", UIHandler,getContext());
                        } else {
                            tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"play\"}", UIHandler, getContext());
                            playButton.setText("Pause");
                        }
                    }
                } else {
                    Toast.makeText(getActivity(),"Remote is not active",Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (remoteActive) {
                    tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"stop\"}", UIHandler,getContext());
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_current\",\"value\":\"/work\"}", UIHandler,getContext());
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());
                } else {
                    Toast.makeText(getActivity(),"Remote is not active",Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (remoteActive) {
                    tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"delete\"}", UIHandler,getContext());
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_current\",\"value\":\"/work\"}", UIHandler,getContext());
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());
                } else {
                    Toast.makeText(getActivity(),"Remote is not active",Toast.LENGTH_SHORT).show();
                }
            }
        });

        tcpClient = TCPCommunicator.getInstance();

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
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"seqeng_mode\",\"value\":3}", UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_number\"}",UIHandler,getContext());

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(outState);
    }

    @Override
    public void onTCPMessageRecieved(JSONObject message) {
        final JSONObject theMessage=message;
        try {
            JSONObject obj = theMessage;
            final String messageTypeString=obj.getString("mtype");
            if (messageTypeString.equals("CPPP")) {
                final String messageSubTypeString = obj.getString("mstype");
                if (messageSubTypeString.equals("sequencer_song_number")) {
                    currentSelection = obj.getInt("value");

                }
                if (messageSubTypeString.equals("seqeng_remote_active")) {
                    remoteActive = obj.getBoolean("value");
                    if (!remoteActive) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(),"Remote is not active.",Toast.LENGTH_SHORT).show();
                                playButton = (Button) myView.findViewById(R.id.rec_play_button);
                                //playButton.setEnabled(false);
                            }
                        });
                    }
                }

//                if (messageSubTypeString.equals("media_dir_list")) {
//                    int i;
//                    mediaDirList.clear();
//                    final ArrayList<String> mMidiFiles;
//                    mMidiFiles = new ArrayList<String>();
//                    mMidiFiles.clear();
//                    midiFiles.clear();
//                    for (i=0;i<obj.getJSONArray("value").length();i++) {
//                        String s = obj.getJSONArray("value").getString(i);
//                        mediaDirList.add(s);
//                        if (s.contains(".MID")) {
//                            mMidiFiles.add(s);
//                            midiFiles.add(s);
//                        }
//                    }
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            WheelView wv = hymnsWheelView;
//                            HymnAdapter4 adapter = new HymnAdapter4(RecorderActivity.this,hymnBook);
//                            hymnsWheelView.setViewAdapter(adapter);
//                            String[] r = hymnBook.getRecordingArray();
//                            updateHymns(hymnsWheelView,hymnBook,0);
//                            wv.invalidate();
//                        }
//                    });
//                }
                if (messageSubTypeString.equals("sequencer_measure")) {
                    final String msg = "Counter: " + obj.getInt("value");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            counterTextView.setText(msg);
                        }
                    });
                }
                if (messageSubTypeString.equals("seqeng_status")) {
                    final int status = obj.getInt("value");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (status) {
                                case 0: {
                                    // STOPPED
                                    statusTextView.setText("Stopped");
                                    playButton.setText("Play");
                                    playButton.setEnabled(true);
                                    recordButton.setEnabled(true);
                                    deleteButton.setEnabled(true);
                                    stopButton.setEnabled(false);
                                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_current\",\"value\":\"/work\"}", UIHandler,getContext());

                                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());
                                    break;
                                }
                                case 1: {
                                    statusTextView.setText("Playing");
                                    playButton.setEnabled(true);
                                    playButton.setText("Pause");
                                    recordButton.setEnabled(false);
                                    deleteButton.setEnabled(false);
                                    stopButton.setEnabled(true);

                                    break;
                                }
                                case 2: {
                                    statusTextView.setText("Seek Done");
                                    break;
                                }
                                case 3: {
                                    statusTextView.setText("Paused");                                    playButton.setEnabled(false);
                                    recordButton.setEnabled(false);
                                    deleteButton.setEnabled(false);
                                    stopButton.setEnabled(true);
                                    playButton.setEnabled(true);

                                    break;
                                }
                                case 4: {
                                    statusTextView.setText("Transition");
                                    break;
                                }
                                default: {
                                    statusTextView.setText("Unknown");
                                    break;
                                }
                            }
                        }
                    });

                }
            }
            if (messageTypeString.equals("SSTA")) {
                final String msg = obj.getString("mstype");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_current\",\"value\":\"/work\"}", UIHandler,getContext());
                        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());
                    }
                });
            }
            if (messageTypeString.equals("GERR")) {
                int errorNumber = obj.getInt("error_number");
                if (errorNumber==0) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),"ERROR: NO FILE",Toast.LENGTH_SHORT).show();
                            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_current\",\"value\":\"/work\"}", UIHandler,getContext());
                            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());
                        }
                    });

                }
                if (errorNumber==1) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),"ERROR: FILE EXISTS",Toast.LENGTH_SHORT).show();
                            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_current\",\"value\":\"/work\"}", UIHandler,getContext());
                            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());
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

    @Override
    public void selectedRecordingExists(Boolean exists) {

        selectionExists = exists;
        if (recordButton==null) {
            return;
        }
        if (selectionExists) {
            recordButton.setEnabled(false);
            stopButton.setEnabled(false);
            deleteButton.setEnabled(true);
            playButton.setEnabled(true);
        } else {
            recordButton.setEnabled(true);
            stopButton.setEnabled(false);
            playButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }
}

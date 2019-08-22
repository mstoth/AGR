package com.example.michaeltoth.agr;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.michaeltoth.agr.widget.WheelView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class PlayerButtonFragment extends Fragment implements TCPListener,
        IPlayerButtonFragment {
    private boolean remoteActive;
    private boolean selectionChanged;
    private View myView;
    private Button playButton;
    private Handler UIHandler = new Handler();
    private HymnBook hymnBook;
    private boolean scrolling = false;
    private TCPCommunicator tcpClient = TCPCommunicator.getInstance();
    private Button stopButton;
    private ImageButton volUpButton, volDownButton;

    private CheckBox playPlusCheckBox;
    private CheckBox loopCheckBox;
    private TextView statusTextView;
    private TextView counterTextView;
    private EditText volumeText;
    private String currentSelection;
    private int currentSelectionNumber;
    WheelView hymnsWheelView;
    private String selectedName = "";
    private MainActivity mListener;
    static final int RENAME_DIALOG_ID = 0;
    private SharedViewModel model;
    private RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<String> myDataset = new ArrayList<String>();
    private List<String> tempoList = new ArrayList<String>();
    private int volLimit;
    private TextView tempoAdjustText;
    private String tempoAdjust;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;

        if (context instanceof Activity){
            a=(Activity) context;
        } else {
            return;
        }
        try {
            mListener = (MainActivity) a;
        } catch (ClassCastException e) {
            throw new ClassCastException(a.toString() + " must implement OnFragmentInteractionListener");
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_buttons,container,false);
        remoteActive = false;
        myView = view;
        selectionChanged = false;
        LayoutInflater inflater2 = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View renamelayout = inflater2.inflate(R.layout.content_rename,(ViewGroup) myView.findViewById(R.id.rename_content));


        // ON CLICK METHODS
        stopButton = view.findViewById(R.id.playerStopButton);
        playButton = view.findViewById(R.id.playerStartButton);
        statusTextView = view.findViewById(R.id.status_text_view);
        counterTextView = view.findViewById(R.id.counter_text_view);
        volDownButton = view.findViewById(R.id.volumeDownButton);
        volUpButton = view.findViewById(R.id.volumeUpButton);
        volumeText = view.findViewById(R.id.volumeLimitText);
        playPlusCheckBox = view.findViewById(R.id.playPlusCheckBox);
        loopCheckBox = view.findViewById(R.id.loopCheckBox);
        tempoAdjustText = view.findViewById(R.id.player_tempo_text);

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"stop\"}",UIHandler,getContext());
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playButton.getText().equals("Play")) {
                    tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"play\"}",UIHandler,getContext());
                }
                if (playButton.getText().equals("Pause")) {
                    tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"pause\"}",UIHandler,getContext());
                }
                if (playButton.getText().equals("Continue")) {
                    tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"unpause\"}",UIHandler,getContext());
                }
            }
        });

        loopCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loopCheckBox.isChecked()) {
                    playPlusCheckBox.setChecked(true);
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_playlist_plus\",\"value\":true}", UIHandler, getContext());
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_playlist_loop\",\"value\":true}", UIHandler, getContext());
                } else {
                    playPlusCheckBox.setChecked(false);
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_playlist_plus\",\"value\":false}", UIHandler, getContext());
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_playlist_loop\",\"value\":false}", UIHandler, getContext());
                }
            }
        });

        volUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (volLimit == 10) {
                    return;
                } else {
                    volLimit = volLimit + 1;
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"playlist_volume_limit\",\"value\":" + volLimit +  "}", UIHandler, getContext());
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"playlist_volume_limit\"}", UIHandler, getContext());
                }
            }
        });

        volDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (volLimit == 1 )  {
                    return;
                }  else {
                    volLimit = volLimit - 1;
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"playlist_volume_limit\",\"value\":" + volLimit +  "}", UIHandler, getContext());
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"playlist_volume_limit\"}", UIHandler, getContext());
                }
            }
        });
        playPlusCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playPlusCheckBox.isChecked()) {
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_playlist_plus\",\"value\":true}", UIHandler, getContext());
                } else {
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_playlist_plus\",\"value\":false}", UIHandler, getContext());
                }
            }
        });



        tcpClient = TCPCommunicator.getInstance();
        tcpClient.addListener(this);

        TCPCommunicator.TCPWriterErrors e;
        e=tcpClient.writeStringToSocket("{\"mtype\":\"GIRQ\"}",UIHandler,getContext());
        if (e == TCPCommunicator.TCPWriterErrors.otherProblem) {
            // no connection
            Toast.makeText(getContext(),"No Connection to Organ...",Toast.LENGTH_SHORT).show();
            tcpClient.init("192.168.1.4",10002);
            TCPCommunicator.addListener(this);
        }

        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"seqeng_remote_active\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"seqeng_mode\",\"value\":4}", UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"playlist_volume_limit\"}", UIHandler, getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"playlist_volume_limit\"}", UIHandler, getContext());

        if (playPlusCheckBox.isChecked()) {
            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_playlist_plus\",\"value\":true}", UIHandler, getContext());
        } else {
            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_playlist_plus\",\"value\":false}", UIHandler, getContext());
        }
        if (loopCheckBox.isChecked()) {
            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_playlist_loop\",\"value\":true}", UIHandler, getContext());
        } else {
            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_playlist_loop\",\"value\":false}", UIHandler, getContext());
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.player_recycler_view);
        recyclerView.setHasFixedSize(true);

//      use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        String[] simpleArray = new String[ myDataset.size() ];
        myDataset.toArray( simpleArray );
        mAdapter = new MyAdapter(simpleArray);
        recyclerView.setAdapter(mAdapter);
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

                if (messageSubTypeString.equals("media_dir_list")) {
                    //model.setSelectedName(selectedName);
                }
                if (messageSubTypeString.equals("sequencer_playlist_name")) {
                    //model.setSelectedName(selectedName);
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_playlist_get\"}" ,
                            UIHandler,getContext());

                }

                if (messageSubTypeString.equals("sequencer_playlist_get")) {
                    JSONObject playlist = obj.getJSONObject("value");
                    JSONArray files = playlist.getJSONArray("playlist");
                    myDataset.clear();
                    tempoList.clear();
                    for (int i = 0; i < files.length(); i++) {
                        JSONObject file = files.getJSONObject(i);
                        String fileName = file.getString("file");
                        String fileNameWithoutSuffix = "";
                        if (fileName.contains(".mid")) {
                            fileNameWithoutSuffix = fileName.replace(".mid","");
                        }
                        if (fileName.contains(".MID")) {
                            fileNameWithoutSuffix = fileName.replace(".MID","");
                        }
                        myDataset.add(fileNameWithoutSuffix);
                        tempoAdjust = file.getString("tempo_adjust");
                        if (tempoAdjust != null) {
                            tempoList.add(tempoAdjust);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tempoAdjustText.setText("Tempo Adjust: " + tempoAdjust);
                                }
                            });

                        } else {
                            tempoList.add("0");
                        }
                    }
                    if (myDataset.isEmpty()) {
                        return;
                    }
                    currentSelection = myDataset.get(0);
                    currentSelectionNumber = 0;
                     mAdapter.setSelectedSong(currentSelection + ".MID");
                     mAdapter.setSelectedSongNumber(0);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String[] simpleArray = new String[ myDataset.size() ];
                            myDataset.toArray( simpleArray );
                            mAdapter = new MyAdapter(simpleArray);
                            ((MyAdapter) mAdapter).setSelectedSong(currentSelection + ".MID");
                            mAdapter.setSelectedSongNumber(currentSelectionNumber);
                            String s;
                            if (simpleArray.length>0) {
                                s = simpleArray[0];
                                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\",\"value\":\"" + s + ".MID\"}",
                                        UIHandler, getContext());
                            }
                            recyclerView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();

                        }
                    });

                }
                if (messageSubTypeString.equals("seqeng_remote_active")) {
                    remoteActive = obj.getBoolean("value");
                    if (!remoteActive) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(),"Remote is not active.",Toast.LENGTH_SHORT).show();
                                playButton = (Button) myView.findViewById(R.id.playerStartButton);
                                //playButton.setEnabled(false);
                            }
                        });
                    }
                }

                if (messageSubTypeString.equals("sequencer_song_name")) {
                    currentSelection = obj.getString("value");
                    mAdapter.setSelectedSong(currentSelection);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });

                }

                if (messageSubTypeString.equals("sequencer_song_number")) {
                    currentSelectionNumber = obj.getInt("value") - 1;
                    mAdapter.setSelectedSongNumber(currentSelectionNumber);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });

                }

                if (messageSubTypeString.equals("playlist_volume_limit")) {
                    volLimit = obj.getInt("value");
                    final String msg = "Volume: " + volLimit;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            volumeText.setText(msg);
                        }
                    });

                }

                if (messageSubTypeString.equals("sequencer_measure")) {
                    final String msg = "Measure: " + obj.getInt("value");
                    if (obj.getInt("value") == 0) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (tempoList.isEmpty()) {
                                    return;
                                }
                                if (tempoList.get(currentSelectionNumber).equals("0")) {
                                    // clear tempo indicator
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            tempoAdjustText.setText("");
                                        }
                                    });

                                } else {
                                    tempoAdjustText.setText("Tempo Adj: " + tempoList.get(currentSelectionNumber));
                                }
                            }
                        });
                    }
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
                                    stopButton.setEnabled(false);
                                    break;
                                }
                                case 1: { // playing
                                    playButton.setEnabled(true);
                                    playButton.setText("Pause");
                                    stopButton.setEnabled(true);
                                    break;
                                }
                                case 2: {
                                    statusTextView.setText("Seek Done");
                                    break;
                                }
                                case 3: {
                                    statusTextView.setText("Paused");
                                    playButton.setText("Continue");

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
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
//                        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_current\",\"value\":\"/work\"}", UIHandler,getContext());
//                        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());
//                    }
//                });
            }
            if (messageTypeString.equals("GACK")) {
//                if (refreshWheel) {
//                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\"}",
//                            UIHandler, getContext());
//                    refreshWheel = false;
//                }
//                if (selectedName.equals("")) {
//                    return;
//                }
//                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}",
//                        UIHandler, getContext());
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
    }


    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String name);
    }

    public void updateName(String name, HymnBook hBook, WheelView wv) {
        selectedName = name;
        hymnBook = hBook;
        String[] mfiles = hymnBook.getRecordingArray();

//        if (midiFiles != null) {
//            midiFiles.clear();
//            String[] mfiles = hymnBook.getRecordingArray();
//            for (String s : mfiles) {
//                midiFiles.add(s);
//            }
//        }
        hymnsWheelView = wv;

        if (selectedName.length()>0) {
            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\",\"value\":\"" + selectedName + "\"}", UIHandler, getContext());
        }
    }


}



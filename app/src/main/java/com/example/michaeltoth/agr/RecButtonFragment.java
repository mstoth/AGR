package com.example.michaeltoth.agr;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.michaeltoth.agr.widget.WheelView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class RecButtonFragment extends Fragment implements TCPListener,
        IRecButtonFragment, EditNameDialogFragment.EditNameDialogListener {
    private boolean remoteActive;
    private boolean selectionExists;
    private View myView;
    private Button playButton;
    private Handler UIHandler = new Handler();
    private HymnBook hymnBook;
    private boolean scrolling = false;
    private TCPCommunicator tcpClient = TCPCommunicator.getInstance();
    private Button recordButton, stopButton, deleteButton, renameButton;
    private TextView counterTextView, statusTextView;
    private String currentSelection;
    private ArrayList<String> mediaDirList;
    private ArrayList<String> midiFiles;
    private ArrayList<String> titles;
    WheelView hymnsWheelView;
    private IMainActivity mIMainActivity;
    private String selectedName = "";
    private MainActivity mListener;
    private boolean songNamesMatch;
    private boolean waitForSequencerToStop;
    private String songNameToMatch;
    private boolean recordWhenSongNamesMatch,refreshWheel;
    static final int RENAME_DIALOG_ID = 0;
    private boolean sequencerStopped = true;
    private SharedViewModel model;

//    NameChanged mCallBack;
//    public interface NameChanged {
//        public void sendName(String name);
//    }

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

    private View getRenammeView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.content_rename,(ViewGroup) myView.findViewById(R.id.rename_content));
        return view;
    }

    public void onFinishEditDialog(String s) {
        Log.d("DEBUG","In onFinishEditDialog");

        if (s.contains(".mid") || s.contains(".MID")) {
            // don't add .mid
        } else {
            s=s+".MID";
        }
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_rename\",\"value\":\"" +
                        selectedName + "\",\"value2\":\"" + s + "\"}",
                UIHandler, getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\",\"value\":\"" + s + "\"}",
                UIHandler, getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());

        hymnsWheelView.invalidateWheel(true);
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rec_buttons,container,false);
        remoteActive = false;
        myView = view;
        refreshWheel = true;
        waitForSequencerToStop = false;
        recordButton = (Button) myView.findViewById(R.id.rec_record_button);
        playButton = (Button) myView.findViewById(R.id.rec_play_button);
        stopButton = (Button) myView.findViewById(R.id.rec_stop_button);
        deleteButton = (Button) myView.findViewById(R.id.rec_delete_button);
        renameButton = (Button) myView.findViewById(R.id.rec_rename_button);
        statusTextView = (TextView) myView.findViewById(R.id.status_text_view);
        counterTextView = (TextView) myView.findViewById(R.id.counter_text_view);

        LayoutInflater inflater2 = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View renamelayout = inflater2.inflate(R.layout.content_rename,(ViewGroup) myView.findViewById(R.id.rename_content));


        // ON CLICK METHODS
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = 0;
                boolean found = false;
                if (remoteActive) {
                    if (hymnBook == null) {
                        hymnBook = HymnBook.get(getContext());
                    }
                    String fileNames[] = hymnBook.getRecordingArray();
                    String tFileName = "";
                    for (int j=1; j<100; j++) {
                        tFileName = "SONG" + String.format("%02d",j) + ".MID";
                        found = false;
                        for (String fn : fileNames) {
                            if (fn.equals(tFileName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            break;
                        }
                    }
                    hymnBook.addRecording(tFileName);
                    songNameToMatch = tFileName;
                    songNamesMatch = false;
                    boolean songNameSent = false;
                    recordWhenSongNamesMatch = true;
                    waitForSequencerToStop = true;
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\",\"value\":\"" + tFileName + "\"}",
                            UIHandler, getContext());
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\"}",
                            UIHandler, getContext());

//                    while (!songNamesMatch) {
//                        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\"}",
//                                    UIHandler, getContext());
//                    }
//                    tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"record\"}", UIHandler,getContext());
                    //tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_current\",\"value\":\"/WORK\"}", UIHandler,getContext());
                    //tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());
                } else {
                    Toast.makeText(getActivity(),"Remote is not active",Toast.LENGTH_SHORT).show();
                }

            }
        });

        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (remoteActive) {
                    int index = hymnsWheelView.getCurrentItem();
                    if (index >= hymnBook.getRecordingArray().length) {
                        Toast.makeText(getContext(), "No Recordings.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedName = hymnBook.getRecordingArray()[index];
                    LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View layout = inflator.inflate(R.layout.content_rename,(ViewGroup) myView.findViewById(R.id.rename_content));
//                    Intent myIntent = new Intent(getContext(), RenameActivity.class);
//                    myIntent.putExtra("NAME_TO_CHANGE", selectedName); //Optional parameters
//                    getContext().startActivity(myIntent);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    final View v = layout.findViewById(R.id.rename_content);
                    EditText et = v.findViewById(R.id.edit_text);
//                    TextView tv = v.findViewById(R.id.textView);
//                    tv.setText("selectedName");
                    builder.setView(v);

                    final String selectedNameWithoutSuffix;
                    selectedNameWithoutSuffix = selectedName.substring(0, selectedName.lastIndexOf('.'));
                    et.setText(selectedNameWithoutSuffix);

                    builder.setTitle("Rename " + selectedNameWithoutSuffix);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TextView nameTextView = (TextView) v.findViewById(R.id.edit_text);
                            String name = nameTextView.getText().toString();
                            if (name.contains(".mid") || name.contains(".MID")) {
                                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_rename\",\"value\":\"" + selectedName + "\",\"value2\":\"" + name + "\"}",
                                        UIHandler, getContext());
                            } else {
                                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_rename\",\"value\":\"" + selectedName + "\",\"value2\":\"" + name + ".MID\"}",
                                        UIHandler, getContext());
                            }
                            refreshWheel = true;
//                            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}",
//                                    UIHandler, getContext());
                            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\",\"value\":\"" + name + ".MID" + "\"}",
                                    UIHandler, getContext());
//                            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}",
//                                    UIHandler, getContext());
//                            if (midiFiles == null) {
//                                midiFiles = hymnBook.getRecArrayList();
//                            }
//                            int index = midiFiles.indexOf(name + ".MID");
//                            hymnsWheelView.setCurrentItem(index);
                            model.setOldName(selectedName);
                            model.setNewName(name+".MID");
                            selectedName = name + ".MID";
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

//                    FragmentManager fm = getActivity().getSupportFragmentManager();
//                    DialogRenameFragment editNameDialogFragment = DialogRenameFragment.newInstance("Rename Song");
//                    editNameDialogFragment.show(fm, "fragment_edit_name");
                    // showEditDialog(layout);
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });



        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (remoteActive) {
                    // model.setNewName(null);
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
                    stopButton.setEnabled(true);
                } else {
                    Toast.makeText(getActivity(),"Remote is not active",Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (remoteActive) {
                    recordButton.setEnabled(true);
                    sequencerStopped = false;
                    tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"stop\"}", UIHandler,getContext());
                    // tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_current\",\"value\":\"/WORK\"}", UIHandler,getContext());
                    // tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());
                    if ( songNameToMatch != null) {
                        model.setNewName(songNameToMatch);
                        songNameToMatch = null;
                    }
                } else {
                    Toast.makeText(getActivity(),"Remote is not active",Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (remoteActive) {
                    int item = hymnsWheelView.getCurrentItem();
//                    if (item >= hymnBook.getRecArray().length)  {
//                        Toast.makeText(getContext(),"No Recordings.",Toast.LENGTH_LONG).show();
//                        return;
//                    }

                    selectedName = hymnBook.getRecordingArray()[item];
                    String fname = selectedName; // .get(item);
//                    hymnBook.removeRecording(fname);
//                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_delete\",\"value\":\"" +
//                            fname + "\"}", UIHandler, getContext());
                    //tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"delete\"}", UIHandler,getContext());
//                    if (hymnBook.getRecArray().length>0) {
                        deleteAlertView(selectedName);
//                        if (midiFiles != null) {
//                            midiFiles.remove(selectedName);
//                        }
//                        selectedName = null;
//                        model.setNewName(null);
                        //tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_current\",\"value\":\"/work\"}", UIHandler,getContext());
                        //tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());

//                    }

                    //tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());

                    //hymnsWheelView.invalidateWheel(true);
                    //hymnsWheelView.setCurrentItem(0);
//                    if (midiFiles != null) {
//                        if (midiFiles.size() > 0) {
//                            String tFileName = midiFiles.get(0);
//                            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\",\"value\":\"" + tFileName + "\"}",
//                                    UIHandler, getContext());
//                            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\"}",
//                                    UIHandler, getContext());
//                        }
//                    }
////                    hymnsWheelView.scrollTo(0,0);
//                    recordButton.setEnabled(true);
//                    playButton.setEnabled(true);
                    // deleteButton.setEnabled(false);
                } else {
                    Toast.makeText(getActivity(),"Remote is not active",Toast.LENGTH_SHORT).show();
                }
                // tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());

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
        //tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_number\"}",UIHandler,getContext());

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(outState);
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

                if (messageSubTypeString.equals("sequencer_song_name")) {
                    String name;

                    name = obj.getString("value");
                    if (name.equals(songNameToMatch)) {
                        songNamesMatch = true;
                    } else {
                        songNamesMatch = false;
                    }
                    if (recordWhenSongNamesMatch) {
                        if (songNamesMatch) {
                            tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"record\"}", UIHandler,getContext());
                            recordWhenSongNamesMatch = false;
                            mListener.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    statusTextView.setText(R.string.recording);

                                }
                            });

                        }
                    }
                    model.setNewName(name);
                }

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
                                    //recordButton.setEnabled(true);
                                    deleteButton.setEnabled(true);
                                    stopButton.setEnabled(false);
                                    hymnsWheelView.setEnabled(true);
                                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_current\",\"value\":\"/work\"}", UIHandler,getContext());

                                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());
                                    break;
                                }
                                case 1: {
                                    if (songNamesMatch) {
                                        statusTextView.setText("Recording " + songNameToMatch);
                                    } else {
                                        statusTextView.setText("Playing");
                                    }
                                    playButton.setEnabled(true);
                                    playButton.setText("Pause");
                                    recordButton.setEnabled(false);
                                    deleteButton.setEnabled(false);
                                    stopButton.setEnabled(true);
                                    hymnsWheelView.setEnabled(false);

                                    break;
                                }
                                case 2: {
                                    statusTextView.setText("Seek Done");
                                    break;
                                }
                                case 3: {
                                    statusTextView.setText("Paused");
                                    playButton.setEnabled(false);
                                    recordButton.setEnabled(false);
                                    deleteButton.setEnabled(false);
                                    stopButton.setEnabled(true);
                                    playButton.setEnabled(true);
                                    hymnsWheelView.setEnabled(false);

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

        selectionExists = exists;
        if (recordButton==null) {
            return;
        }
        recordButton.setEnabled(true);
        stopButton.setEnabled(false);
        deleteButton.setEnabled(true);
        playButton.setEnabled(true);
    }

    private void deleteAlertView( final String name) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle( "Deleting " + name ).setMessage("Are you sure you want to delete " + name + "?")
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) { dialoginterface.cancel(); }})
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_delete\",\"value\":\"" +
                                name + "\"}", UIHandler, getContext());
                        currentSelection = null;
//                        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());
                        if (midiFiles != null) {
                            midiFiles.remove(selectedName);
                        }
                        hymnBook.removeRecording(selectedName);

                        if (mListener != null) {
                            mListener.onFragmentInteraction(name);
                        }

                        selectedName = null;
                        model.setNewName(null);
                        if (midiFiles != null) {
                            if (midiFiles.size() > 0) {
                                String tFileName = midiFiles.get(0);
                                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\",\"value\":\"" + tFileName + "\"}",
                                        UIHandler, getContext());
                                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\"}",
                                        UIHandler, getContext());
                            }
                        }
//                    hymnsWheelView.scrollTo(0,0);
                        recordButton.setEnabled(true);
                        playButton.setEnabled(true);

                        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());

                    }

                }).show();
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String name);
    }


    private void showEditDialog(View layout) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        String selectedNameWithoutSuffix;
        selectedNameWithoutSuffix = selectedName.substring(0, selectedName.lastIndexOf('.'));

        EditNameDialogFragment editNameDialogFragment = EditNameDialogFragment.newInstance("Rename " + selectedNameWithoutSuffix, selectedNameWithoutSuffix);
        //View v = editNameDialogFragment.getView();
        EditText et = layout.findViewById(R.id.edit_text);
        et.setText(selectedNameWithoutSuffix);
        editNameDialogFragment.setTargetFragment(this,0);
        editNameDialogFragment.show(fm, "fragment_edit_name");
    }

}

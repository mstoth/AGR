package com.example.michaeltoth.agr;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.michaeltoth.agr.widget.OnWheelChangedListener;
import com.example.michaeltoth.agr.widget.OnWheelScrollListener;
import com.example.michaeltoth.agr.widget.WheelView;
import com.example.michaeltoth.agr.widget.adapters.AbstractWheelTextAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RecListFragment extends Fragment implements TCPListener{
    private RecListFragment.HymnAdapter4 mAdapter;
    private TCPCommunicator tcpClient;
    private Handler UIHandler = new Handler();
    private HymnBook hymnBook;
    private boolean scrolling;
    WheelView hymnsWheelView;
    private ArrayList<String> midiFiles;
    private ArrayList<String> mediaDirList;
    private boolean remoteActive;
    private String currentSelection;
    private static final String tag = "REC_LIST";
    private IMainActivity iMainActivity;
    private MainActivity mListener;
    private SharedViewModel model;
    private String oldName, newName;

//    public void changeName(String text) {
//        int index = midiFiles.indexOf(text);
//        hymnsWheelView.setCurrentItem(index);
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hymn_list,container,false);

        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        model.getOldName().observe(this,(name)->{
            oldName = name;
            int index = midiFiles.indexOf(oldName);
            if (index >= 0) {
                midiFiles.remove(index);
                hymnBook.removeRecording(oldName);
            }
        });
        model.getNewName().observe(this,(name)->{

            newName = name;
            if (newName == null) {
                int index = hymnsWheelView.getCurrentItem();
                if (index >= 0 && index < midiFiles.size()) {
                    String s = midiFiles.get(index);
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\",\"value\":\"" + s + "\"}" ,
                            UIHandler,getContext());
                    currentSelection = s;

                } else {
                    index = 0;
                    hymnsWheelView.setCurrentItem(index);
                    String s = midiFiles.get(index);
                    currentSelection = s;
                }
                return;
            }
            currentSelection = newName;
            if (midiFiles == null) {
                midiFiles = hymnBook.getRecArrayList();
                if (midiFiles == null) {
                    return;
                }
            }
            int index = midiFiles.indexOf(newName);
            if (index >= 0) {
                hymnsWheelView.setCurrentItem(index);
            } else {
                if (newName != null) {
                    midiFiles.add(newName);
                    Collections.sort(midiFiles);
                    hymnBook.addRecording(newName);
                    index = midiFiles.indexOf(newName);
                    hymnsWheelView.setCurrentItem(index);
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_rename\",\"value\":\"" +
                                    oldName + "\",\"value2\":\"" + newName + "\"}",
                            UIHandler, getContext());

                }

            }
            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());

        });


        scrolling = false;
        remoteActive = false;
        hymnsWheelView = view.findViewById(R.id.hymn_recycler_view);
        hymnsWheelView.setVisibleItems(1);
        mAdapter = new RecListFragment.HymnAdapter4(getContext(),hymnBook);
        hymnsWheelView.setViewAdapter(mAdapter);

        hymnsWheelView.addChangingListener(new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (!scrolling) {
                    if ( currentSelection != null) {
                        if (currentSelection.length() > 0) {
                            int index = midiFiles.indexOf(currentSelection);
                            hymnsWheelView.setCurrentItem(index);
                            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\",\"value\":\"" + currentSelection + "\"}" ,
                                    UIHandler,getContext());

                        }
                    }

                }
            }
        });

        hymnsWheelView.addScrollingListener( new OnWheelScrollListener() {
            public void onScrollingStarted(WheelView wheel) {
                scrolling = true;
            }
            public void onScrollingFinished(WheelView wheel) {
                scrolling = false;
                int item = hymnsWheelView.getCurrentItem();
                if (midiFiles.size()>item) {
                    String fname = midiFiles.get(item);
                    currentSelection = fname;
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\",\"value\":\"" + fname + "\"}" ,
                            UIHandler,getContext());
                }


            }
        });

        mediaDirList = new ArrayList<String>();
        midiFiles = new ArrayList<String>();
        hymnBook = HymnBook.get(getContext());
        remoteActive = false;


        tcpClient = TCPCommunicator.getInstance();
        if (!tcpClient.isConnected) {
            tcpClient.setServerHost("192.168.1.4");
            tcpClient.setServerPort(10002);
            ConnectToServer();
        } else {
            tcpClient.addListener(this);
        }


        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"seqeng_remote_active\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"seqeng_mode\",\"value\":3}", UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_current\",\"value\":\"/WORK\"}", UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());

        hymnBook = HymnBook.get(getContext());
        updateUI();

        if (hasMidiFile(hymnsWheelView.getCurrentItem())) {
            mListener.selectedTitleExists(true);
        } else {
            mListener.selectedTitleExists(false);
        }
        int item = hymnsWheelView.getCurrentItem();
        String fname = "";
        if (midiFiles.size() == 0) {
            //fname = hymnBook.getRecordingArray()[0];
        } else {
            fname = midiFiles.get(item);
        }
        if (mListener != null) {
            mListener.onFragmentInteraction(fname,hymnBook, hymnsWheelView);
        }

        return view;
    }
    private void ConnectToServer() {
        //tcpClient = TCPCommunicator.getInstance();
        tcpClient.init("192.168.1.4",10002);
        TCPCommunicator.addListener(this);
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class RecHolder extends RecyclerView.ViewHolder {
        private TextView mTitleView;
        private Hymn mHymn;

        public RecHolder(LayoutInflater inflater,ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_rec,parent,false));
            mTitleView = itemView.findViewById(R.id.rec_title);
        }

        public void bind(Hymn hymn) {
            mHymn = hymn;
            mTitleView.setText(hymn.getTitle());
        }
    }

    private void updateUI() {
        // perhaps not needed
    }

    private class RecAdapter extends RecyclerView.Adapter<RecListFragment.RecHolder> {
        private List<Hymn> mHymns;

        public RecAdapter(List<Hymn> hymns) {
            mHymns = hymns;
        }

        @NonNull
        @Override
        public RecListFragment.RecHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new RecListFragment.RecHolder(layoutInflater,viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull RecListFragment.RecHolder hymnHolder, int i) {
            Hymn hymn = mHymns.get(i);
            hymnHolder.bind(hymn);
        }

        @Override
        public int getItemCount() {
            return mHymns.size();
        }
    }


    @Override
    public void onTCPMessageRecieved(JSONObject message) {
        final JSONObject theMessage=message;
        try {

            final String messageTypeString=theMessage.getString("mtype");

            // Log.d("DEBUG",messageTypeString);
//            if (messageTypeString.equals("GACK")) {
//                if (mListener != null) {
//                    int index = hymnsWheelView.getCurrentItem();
//                    if (midiFiles.size()>index) {
//                        String fname = midiFiles.get(index);
//                        mListener.onFragmentInteraction(fname, hymnBook, hymnsWheelView);
//                    }
//                }
//            }

            if (messageTypeString.equals("CPPP")) {
                final String messageSubTypeString=theMessage.getString("mstype");
                final boolean hasSel;
//                if (messageSubTypeString.equals("sequencer_song_number")) {
//                    currentSelection = theMessage.getInt("value");
//                    hasSel = hasMidiFile(currentSelection-1);
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            iMainActivity.selectedTitleExists(hasSel);
//                            hymnsWheelView.setCurrentItem(currentSelection-1);
//                        }
//                    });
//                }
                if (messageSubTypeString.equals("media_dir_list")) {
                    int i;
                    mediaDirList.clear();
                    JSONArray a = theMessage.getJSONArray("value");
                    final ArrayList<String> mMidiFiles;
                    mMidiFiles = new ArrayList<String>();
                    mMidiFiles.clear();
                    midiFiles.clear();
                    int alen = a.length();

                    for (i=0; i<alen; i++) {
                        String s = a.getString(i);
                        if (s.contains(".mid") || s.contains(".MID")) {
                            mediaDirList.add(s);
                            midiFiles.add(s);
                        }
                    }
                    Collections.sort(midiFiles);
//                    final ArrayList<String> mMidiFiles;
//                    mMidiFiles = new ArrayList<String>();
//                    mMidiFiles.clear();
//                    midiFiles.clear();
                    hymnBook.setRecordings(theMessage.getJSONArray("value"));
//                    for (i=0;i<hymnBook.getRecArray().length;i++) {
//                        String s = hymnBook.getRecArray()[i];
//                        mediaDirList.add(s);
//                        midiFiles.add(s);
//                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WheelView wv = hymnsWheelView;
                            HymnAdapter4 adapter = new HymnAdapter4(getContext(),hymnBook);
                            hymnsWheelView.setViewAdapter(adapter);
                            //String[] r = hymnBook.getRecordingArray();
                            if (newName != null && newName.length()>0) {
                                int index = midiFiles.indexOf(newName);
                                if (index >= 0) {
                                    updateHymns(hymnsWheelView, hymnBook, index);
                                }

                            } else {
                                updateHymns(hymnsWheelView, hymnBook, 0);
                            }
                            wv.invalidate();
                        }
                    });

                }

            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void updateHymns(final WheelView hymnWheelView, HymnBook hbook, int index) {
        final HymnBook hb = hbook;
        final int idx = index;
        final WheelView wv = hymnWheelView;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HymnAdapter4 adapter = new HymnAdapter4(getContext(),hymnBook);
                adapter.setTextSize(18);
                wv.setViewAdapter(adapter);
                int  index = midiFiles.indexOf(currentSelection);
                if (index < 0) {
                    index = hymnWheelView.getCurrentItem();
                }
                hymnWheelView.setCurrentItem(index);
                if (index < midiFiles.size()) {
                    String str = midiFiles.get(index);
                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_name\",\"value\":\"" + str + "\"}",
                            UIHandler, getContext());

                }

            }
        });
    }


    @Override
    public void onTCPConnectionStatusChanged(boolean isConnectedNow) {

    }

    private boolean hasMidiFile(int index) {
//        String title = hymnBook.getHymnArray()[index];
//        if (midiFiles==null) {
//            return false;
//        }
//        for (String name: midiFiles) {
//            if (name.equals(title)) {
//                return true;
//            }
//        }
        return true;
    }


    protected class HymnAdapter4 extends AbstractWheelTextAdapter {
        private HymnBook hymnBook = HymnBook.get(getContext());
        private String[] hymns = hymnBook.getRecArray();
        // private String midiFiles[] = new String[] {};

        public void setMidiFiles(String[] mf) {
            for (String f: mf) {
                midiFiles.add(f);
                Collections.sort(midiFiles);
            }
        }

        /**
         * Constructor
         */
        protected HymnAdapter4(Context context, HymnBook hymnbook) {
            super(context, R.layout.list_item_hymn, NO_RESOURCE);
            setItemTextResource(R.id.hymn_title);
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            View view = super.getItem(index, cachedView, parent);
            TextView txt = (TextView) view.findViewById(R.id.hymn_title);
            if (index < midiFiles.size()) {
                String fname = midiFiles.get(index);
                if (fname.contains(".mid")) {
                    fname = fname.replace(".mid","");
                }
                if (fname.contains(".MID")) {
                    fname = fname.replace(".MID","");
                }
                txt.setText(fname);
                txt.setTextColor(Color.BLACK);
            }
            return view;
        }

        @Override
        public int getItemsCount() {
//            int sz = midiFiles.size();
//            int fnameIndex = hymnsWheelView.getCurrentItem();
//            String fname;
//            if (midiFiles.size()>fnameIndex) {
//                fname = midiFiles.get(fnameIndex);
//            } else {
//                fname = "";
//            }
//            if (mListener != null) {
//                mListener.onFragmentInteraction(fname, hymnBook, hymnsWheelView);
//            }
            return midiFiles.size();
        }

        @Override
        protected CharSequence getItemText(int index) {
            if (midiFiles.size()==0) {
                return "";
            }
            if (midiFiles.size() > index) {
                return midiFiles.get(index);
            }
            return "";
        }
    }

    public void removeName(String name) {
        String selectedName;
        if (mListener != null) {
            if (midiFiles.size()>0) {
                if (name != null) {
                    int idx = midiFiles.indexOf(name);
                    if (idx >= 0) {
                        midiFiles.remove(idx);
                        hymnBook.removeRecording(name);
                        hymnsWheelView.invalidateWheel(true);
                    }
                }
                Collections.sort(midiFiles);
//                selectedName = midiFiles.get(0);
//                hymnsWheelView.setCurrentItem(0);
            } else {
                selectedName = "";
                Toast.makeText(getContext(),"There are no MIDI files.",Toast.LENGTH_LONG).show();
            }
//            int index = hymnsWheelView.getCurrentItem();
//            if (midiFiles.size() > index) {
//                index = index - 1;
//            }
//            if (index == midiFiles.size()) {
//                index = index - 1;
//            }
//            if (midiFiles.size() > 0) {
//                selectedName = midiFiles.get(index);
//            } else {
//                selectedName = "";
//            }
//            Log.d("DEBUG","Updating Name to " + selectedName);
            // mListener.onFragmentInteraction(selectedName,hymnBook,hymnsWheelView);
        }
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String fileName, HymnBook hymnBook, WheelView wv);
    }

}

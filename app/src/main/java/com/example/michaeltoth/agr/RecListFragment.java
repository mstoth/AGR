package com.example.michaeltoth.agr;

import android.content.Context;
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

import com.example.michaeltoth.agr.widget.OnWheelChangedListener;
import com.example.michaeltoth.agr.widget.OnWheelScrollListener;
import com.example.michaeltoth.agr.widget.WheelView;
import com.example.michaeltoth.agr.widget.adapters.AbstractWheelTextAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
    private int currentSelection;
    private static final String tag = "REC_LIST";
    private IMainActivity iMainActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        iMainActivity = (IMainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hymn_list,container,false);

        scrolling = false;
        remoteActive = false;
        hymnsWheelView = view.findViewById(R.id.hymn_recycler_view);
        hymnsWheelView.setVisibleItems(1);
        mAdapter = new RecListFragment.HymnAdapter4(getContext(),hymnBook);
        hymnsWheelView.setViewAdapter(mAdapter);

        hymnsWheelView.addChangingListener(new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (!scrolling) {
//                    tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_number\",\"value\":" + Integer.toString(hymnsWheelView.getCurrentItem()+1) + "}",
//                            UIHandler,getContext());

                }
            }
        });

        hymnsWheelView.addScrollingListener( new OnWheelScrollListener() {
            public void onScrollingStarted(WheelView wheel) {
                scrolling = true;
            }
            public void onScrollingFinished(WheelView wheel) {
                scrolling = false;
                if (hasMidiFile(hymnsWheelView.getCurrentItem())) {
                    iMainActivity.selectedTitleExists(true);
                } else {
                    iMainActivity.selectedTitleExists(false);
                }
                // Log.i("TAG",Integer.toString(hymnsWheelView.getCurrentItem()));
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_number\",\"value\":" + Integer.toString(hymnsWheelView.getCurrentItem()+1) + "}",
                        UIHandler,getContext());

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
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_current\",\"value\":\"/work\"}", UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"media_dir_list\"}", UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_number\"}", UIHandler,getContext());

        hymnBook = HymnBook.get(getContext());
        updateUI();

        if (hasMidiFile(hymnsWheelView.getCurrentItem())) {
            iMainActivity.selectedTitleExists(true);
        } else {
            iMainActivity.selectedTitleExists(false);
        }

//        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        int height=display.getHeight();
//        ViewGroup.LayoutParams p = view.getLayoutParams();
//        int h = p.height;
//        p.height  = (int)(height/3);
//        view.setLayoutParams(p);

        return view;
    }
    private void ConnectToServer() {
        //tcpClient = TCPCommunicator.getInstance();
        tcpClient.init("192.168.1.4",10002);
        TCPCommunicator.addListener(this);
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

            if (messageTypeString.equals("CPPP")) {
                final String messageSubTypeString=theMessage.getString("mstype");
                final boolean hasSel;
                if (messageSubTypeString.equals("sequencer_song_number")) {
                    currentSelection = theMessage.getInt("value");
                    hasSel = hasMidiFile(currentSelection-1);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iMainActivity.selectedTitleExists(hasSel);
                            hymnsWheelView.setCurrentItem(currentSelection-1);
                        }
                    });
                }
                if (messageSubTypeString.equals("media_dir_list")) {
                    int i;
                    mediaDirList.clear();
                    final ArrayList<String> mMidiFiles;
                    mMidiFiles = new ArrayList<String>();
                    mMidiFiles.clear();
                    midiFiles.clear();
                    for (i=0;i<theMessage.getJSONArray("value").length();i++) {
                        String s = theMessage.getJSONArray("value").getString(i);
                        mediaDirList.add(s);
                        if (s.contains(".MID")) {
                            mMidiFiles.add(s);
                            midiFiles.add(s);
                        }
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WheelView wv = hymnsWheelView;
                            HymnAdapter4 adapter = new HymnAdapter4(getContext(),hymnBook);
                            hymnsWheelView.setViewAdapter(adapter);
                            String[] r = hymnBook.getRecordingArray();
                            updateHymns(hymnsWheelView,hymnBook,0);
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

    private void updateHymns(WheelView hymnWheelView, HymnBook hbook, int index) {
        final HymnBook hb = hbook;
        final int idx = index;
        final WheelView wv = hymnWheelView;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HymnAdapter4 adapter = new HymnAdapter4(getContext(),hymnBook);
                adapter.setTextSize(18);
                wv.setViewAdapter(adapter);
            }
        });
    }


    @Override
    public void onTCPConnectionStatusChanged(boolean isConnectedNow) {

    }

    private boolean hasMidiFile(int index) {
        String title = String.format("SONG%02d.MID",index+1);
        if (midiFiles==null) {
            return false;
        }
        for (String name: midiFiles) {
            if (name.equals(title)) {
                return true;
            }
        }
        return false;
    }


    private class HymnAdapter4 extends AbstractWheelTextAdapter {
        private HymnBook hymnBook = HymnBook.get(getContext());
        private String[] hymns = hymnBook.getRecArray();
        private String midiFiles[] = new String[] {};

        public void setMidiFiles(String[] mf) {
            midiFiles = mf;
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
            txt.setText(String.format("Selection %02d",index+1));
            if (hasMidiFile(index)) {
                txt.setTextColor(Color.BLACK);
                //iMainActivity.selectedTitleExists(true);
            } else {
                txt.setTextColor(Color.RED);
                //iMainActivity.selectedTitleExists(false);
            }
            return view;
        }

        @Override
        public int getItemsCount() {
            int sz = hymns.length;
            return hymns.length;
        }

        @Override
        protected CharSequence getItemText(int index) {
            return hymns[index];
        }
    }


}

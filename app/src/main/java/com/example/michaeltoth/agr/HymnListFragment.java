package com.example.michaeltoth.agr;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
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
import com.example.michaeltoth.agr.widget.adapters.WheelViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.List;

import static android.support.v4.content.ContextCompat.getSystemService;


public class HymnListFragment extends Fragment implements TCPListener{
    private HymnAdapter4 mAdapter;
    private TCPCommunicator tcpClient;
    private Handler UIHandler = new Handler();
    private HymnBook hymnBook;
    private boolean scrolling;
    private boolean remoteActive;
    WheelView hymnsWheelView;
    private int currentSong;
    private View myView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hymn_list,container,false);

        scrolling = false;
        remoteActive = false;

        tcpClient = TCPCommunicator.getInstance();
        if (!tcpClient.isConnected) {
            tcpClient.setServerHost("192.168.1.4");
            tcpClient.setServerPort(10002);
            ConnectToServer();
        } else {
            tcpClient.addListener(this);
        }
        if (!tcpClient.getHymnsLoaded()) {
            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_hymn_list\"}",UIHandler,getContext());
        }
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_song_current\"}",UIHandler,getContext());
        hymnBook = HymnBook.get(getContext());
        myView = view;
        setup();
        return view;
    }

    private void ConnectToServer() {
        //tcpClient = TCPCommunicator.getInstance();
        tcpClient.init("192.168.1.4",10002);
        TCPCommunicator.addListener(this);
    }

    public void setup() {
        hymnBook = HymnBook.get(getContext());
        hymnsWheelView = myView.findViewById(R.id.hymn_recycler_view);
        hymnsWheelView.setVisibleItems(1);
        mAdapter = new HymnAdapter4(getContext(),hymnBook);
        hymnsWheelView.setViewAdapter(mAdapter);

        hymnsWheelView.addChangingListener(new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (!scrolling) {
                    // Log.d("TAG","oldValue is " + Integer.toString(oldValue));
                    // Log.d("TAG","new value is " + Integer.toString(newValue));
                }
            }
        });

        hymnsWheelView.addScrollingListener( new OnWheelScrollListener() {
            public void onScrollingStarted(WheelView wheel) {
                scrolling = true;
            }
            public void onScrollingFinished(WheelView wheel) {
                scrolling = false;
                // Log.i("TAG",Integer.toString(hymnsWheelView.getCurrentItem()));
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"hymnplayer_song_current\",\"value\":" + Integer.toString(hymnsWheelView.getCurrentItem()) + "}",
                        UIHandler,getContext());

            }
        });
    }
    @Override
    public void onTCPMessageRecieved(JSONObject message) {
        final JSONObject theMessage=message;
        try {

            final String messageTypeString=theMessage.getString("mtype");

            // Log.d("DEBUG",messageTypeString);

            if (messageTypeString.equals("CPPP")) {
                final String messageSubTypeString=theMessage.getString("mstype");
                if (messageSubTypeString.equals("hymnplayer_hymn_list")) {
                    final JSONArray hymns = theMessage.getJSONArray("value");
                    hymnBook.setHymns(hymns);
                    if (hymns.length()>0) {
                        tcpClient.setHymnsLoaded(true);
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setup();
                        }
                    });
                }
                if (messageSubTypeString.equals("hymnplayer_song_current")) {
                    currentSong = theMessage.getInt("value");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hymnsWheelView.setCurrentItem(currentSong);
                            hymnsWheelView.invalidateWheel(false);
                            setup();
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

    private class HymnHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleView;
        private Hymn mHymn;

        public HymnHolder(LayoutInflater inflater,ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_hymn,parent,false));
            itemView.setOnClickListener(this);
            mTitleView = (TextView) itemView.findViewById(R.id.hymn_title);
        }

        public void bind(Hymn hymn) {
            mHymn = hymn;
            mTitleView.setText(hymn.getTitle());
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(getActivity(),mHymn.getTitle() + " clicked!",Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        HymnBook hymnBook = HymnBook.get(getActivity());
        List<Hymn> hymns  = hymnBook.getHymns();
        mAdapter = new HymnAdapter4(getContext(),hymnBook);
        hymnsWheelView.setViewAdapter(mAdapter);
        hymnsWheelView.invalidateWheel(false);

    }


    private class HymnAdapter4 extends AbstractWheelTextAdapter {
        private HymnBook hymnBook = HymnBook.get(getContext());
        private String[] hymns = hymnBook.getHymnArray();

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
            return view;
        }

        @Override
        public int getItemsCount() {
            // Log.d("HYMN","Number of items = " + hymns.length);
            return hymns.length;
        }

        @Override
        protected CharSequence getItemText(int index) {
            return hymns[index];
        }
    }

}

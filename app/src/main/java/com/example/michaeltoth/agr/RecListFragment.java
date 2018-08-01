package com.example.michaeltoth.agr;

import android.content.Context;
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

import java.util.List;

public class RecListFragment extends Fragment implements TCPListener{
    private RecListFragment.HymnAdapter4 mAdapter;
    private TCPCommunicator tcpClient;
    private Handler UIHandler = new Handler();
    private HymnBook hymnBook;
    private boolean scrolling;
    WheelView hymnsWheelView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hymn_list,container,false);
        scrolling = false;

        hymnsWheelView = view.findViewById(R.id.hymn_recycler_view);
        hymnsWheelView.setVisibleItems(1);
        mAdapter = new RecListFragment.HymnAdapter4(getContext(),hymnBook);
        hymnsWheelView.setViewAdapter(mAdapter);

        hymnsWheelView.addChangingListener(new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (!scrolling) {
                    //updateHymns(wheel,hymnBook,0);
                    Log.d("TAG","oldValue is " + Integer.toString(oldValue));
                    Log.d("TAG","new value is " + Integer.toString(newValue));
                }
            }
        });

        hymnsWheelView.addScrollingListener( new OnWheelScrollListener() {
            public void onScrollingStarted(WheelView wheel) {
                scrolling = true;
            }
            public void onScrollingFinished(WheelView wheel) {
                scrolling = false;
                Log.i("TAG",Integer.toString(hymnsWheelView.getCurrentItem()));
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_number\",\"value\":" + Integer.toString(hymnsWheelView.getCurrentItem()) + "}",
                        UIHandler,getContext());

            }
        });



        tcpClient = TCPCommunicator.getInstance();
        tcpClient.addListener(this);
        hymnBook = HymnBook.get(getContext());
        updateUI();

//        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        int height=display.getHeight();
//        ViewGroup.LayoutParams p = view.getLayoutParams();
//        int h = p.height;
//        p.height  = (int)(height/3);
//        view.setLayoutParams(p);

        return view;
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

            Log.d("DEBUG",messageTypeString);

            if (messageTypeString.equals("CPPP")) {
                final String messageSubTypeString=theMessage.getString("mstype");
                if (messageSubTypeString.equals("sequencer_song_number")) {
                    // update wheel view
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

    private class HymnAdapter4 extends AbstractWheelTextAdapter {
        private HymnBook hymnBook = HymnBook.get(getContext());
        private String[] hymns = hymnBook.getRecArray();

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
            return hymns.length;
        }

        @Override
        protected CharSequence getItemText(int index) {
            return hymns[index];
        }
    }


}

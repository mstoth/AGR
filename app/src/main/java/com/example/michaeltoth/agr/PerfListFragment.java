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
import android.widget.Toast;

import com.example.michaeltoth.agr.widget.OnWheelChangedListener;
import com.example.michaeltoth.agr.widget.OnWheelScrollListener;
import com.example.michaeltoth.agr.widget.WheelView;
import com.example.michaeltoth.agr.widget.adapters.AbstractWheelTextAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class PerfListFragment extends Fragment implements TCPListener {
    private RecyclerView mHymnRecyclerView;
    private PerfListFragment.HymnAdapter4 mAdapter;
    private TCPCommunicator tcpClient;
    private Handler UIHandler = new Handler();
    private HymnBook hymnBook;
    private boolean scrolling;
    WheelView hymnsWheelView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perf_list,container,false);

        scrolling = false;

        hymnsWheelView = view.findViewById(R.id.hymn_recycler_view);
        hymnsWheelView.setVisibleItems(1);
        mAdapter = new PerfListFragment.HymnAdapter4(getContext(),hymnBook);
        hymnsWheelView.setViewAdapter(mAdapter);

        hymnsWheelView.addChangingListener(new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (!scrolling) {
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
                tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"preludeplayer_song_current\",\"value\":" + Integer.toString(hymnsWheelView.getCurrentItem()) + "}",
                        UIHandler,getContext());

            }
        });

        tcpClient = TCPCommunicator.getInstance();
        tcpClient.addListener(this);
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"preludeplayer_list\"}",UIHandler,getContext());
        tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"preludeplayer_song_current\"}",UIHandler,getContext());
        hymnBook = HymnBook.get(getContext());

        updateUI();

        return view;
    }

    @Override
    public void onTCPMessageRecieved(JSONObject message) {
        final JSONObject theMessage=message;
        try {

            final String messageTypeString=theMessage.getString("mtype");

            Log.d("DEBUG",messageTypeString);

            if (messageTypeString.equals("CPPP")) {
                final String messageSubTypeString=theMessage.getString("mstype");
                if (messageSubTypeString.equals("preludeplayer_list")) {
                    final JSONArray hymns = theMessage.getJSONArray("value");
                    hymnBook.setPerfs(hymns);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUI();
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

    private class PerfHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleView;
        private Hymn mPerf;

        public PerfHolder(LayoutInflater inflater,ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_perf,parent,false));
            itemView.setOnClickListener(this);
            mTitleView = itemView.findViewById(R.id.perf_title);

        }

        public void bind(Hymn hymn) {
            mPerf = hymn;
            mTitleView.setText(hymn.getTitle());
        }


        @Override
        public void onClick(View view) {
            Toast.makeText(getActivity(),mPerf.getTitle() + " clicked!",Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        HymnBook hymnBook = HymnBook.get(getActivity());
        List<Hymn> hymns  = hymnBook.getmPerfs();
        mAdapter = new HymnAdapter4(getContext(),hymnBook);
        hymnsWheelView.setViewAdapter(mAdapter);
    }

    private class PerfAdapter extends RecyclerView.Adapter<PerfListFragment.PerfHolder> {
        private List<Hymn> mHymns;

        public PerfAdapter(List<Hymn> hymns) {
            mHymns = hymns;
        }

        @NonNull
        @Override
        public PerfListFragment.PerfHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new PerfListFragment.PerfHolder(layoutInflater,viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull PerfListFragment.PerfHolder hymnHolder, int i) {
            Hymn mHymn = mHymns.get(i);
            hymnHolder.bind(mHymn);
        }

        @Override
        public int getItemCount() {
            return mHymns.size();
        }
    }

    private class HymnAdapter4 extends AbstractWheelTextAdapter {
        private HymnBook hymnBook = HymnBook.get(getContext());
        private String[] hymns = hymnBook.getPerfArray();

        /**
         * Constructor
         */
        protected HymnAdapter4(Context context, HymnBook hymnbook) {
            super(context, R.layout.list_item_perf, NO_RESOURCE);
            setItemTextResource(R.id.perf_title);
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

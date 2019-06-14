
package com.example.michaeltoth.agr;


import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.michaeltoth.agr.R;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private String[] mDataset;
    private String selectedSong;
    private int selectedSongNumber;
    private TCPCommunicator tcpClient = TCPCommunicator.getInstance();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView textView;
        public int selectionNumber;
        public MyViewHolder(TextView v) {
            super(v);
            textView = v;
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            TCPCommunicator tcpClient = TCPCommunicator.getInstance();
            Handler UIHandler = new Handler();
            int oneBasedSelectionNumber = selectionNumber + 1;
            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_number\",\"value\":" + oneBasedSelectionNumber + "}",
                    UIHandler, view.getContext());
            tcpClient.writeStringToSocket("{\"mtype\":\"CPPP\",\"mstype\":\"sequencer_song_number\"}",
                    UIHandler, view.getContext());

        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(String[] myDataset) {

        mDataset = myDataset;
        if (mDataset.length>0) {
            selectedSong = mDataset[0];
        }
    }

    public void setSelectedSong(String s) {
        selectedSong = s;
    }

    public void setSelectedSongNumber(int i) {
        selectedSongNumber = i;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_title, parent, false);
        int selectionNumber = -1;
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.setText(mDataset[position]);
        holder.selectionNumber = position;
        if (selectedSongNumber >= 0 && selectedSongNumber < mDataset.length) {
            String midiFileName = mDataset[position]+".MID";
            if (selectedSongNumber==position) {
                holder.textView.setTextColor(Color.RED);
            } else {
                holder.textView.setTextColor(Color.BLACK);
            }
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}

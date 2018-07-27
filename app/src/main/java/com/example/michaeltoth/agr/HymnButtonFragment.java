package com.example.michaeltoth.agr;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class HymnButtonFragment extends Fragment {
    private ImageButton mHymnVolUpButton;
    private ImageButton mHymnVolDownButton;
    private TextView mHymnVolText;
    private ImageButton mPlayButton;
    private TCPCommunicator tcpClient;
    private Handler UIHandler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hymn_buttons,container,false);
        mHymnVolUpButton = view.findViewById(R.id.hymn_vol_up);
        mHymnVolDownButton = view.findViewById(R.id.hymn_vol_down);
        mHymnVolText = view.findViewById(R.id.hymn_vol_text);
        tcpClient = TCPCommunicator.getInstance();
        mPlayButton = view.findViewById(R.id.hymn_play_button);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tcpClient.writeStringToSocket("{\"mtype\":\"SEQR\",\"mstype\":\"play\"}",UIHandler,getContext());
            }
        });
        return view;
    }

}

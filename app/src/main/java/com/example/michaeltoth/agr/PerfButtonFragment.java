package com.example.michaeltoth.agr;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class PerfButtonFragment extends Fragment {
    private ImageButton mPerfVolUpButton;
    private ImageButton mPerfVolDownButton;
    private ImageButton mPerfPlayButton;
    private TextView mPerfVolText;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perf_buttons,container,false);
        mPerfVolUpButton = view.findViewById(R.id.perf_vol_up);
        mPerfVolDownButton = view.findViewById(R.id.perf_vol_down);
        mPerfVolText = view.findViewById(R.id.perf_vol_text);
        mPerfPlayButton = (ImageButton) view.findViewById(R.id.perf_play_button);
        mPerfPlayButton.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"Play Button Pressed",Toast.LENGTH_SHORT).show();
            }
        });

        mPerfVolUpButton.setOnLongClickListener(new ImageButton.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getContext(),"Long Click on Up Button",Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        mPerfVolUpButton.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"Up Button",Toast.LENGTH_SHORT).show();
            }
        });

        mPerfVolDownButton.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"Down Button",Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

}

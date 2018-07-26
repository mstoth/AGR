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

public class PerfButtonFragment extends Fragment {
    private ImageButton mPerfVolUpButton;
    private ImageButton mPerfVolDownButton;
    private TextView mPerfVolText;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perf_buttons,container,false);
        mPerfVolUpButton = view.findViewById(R.id.perf_vol_up);
        mPerfVolDownButton = view.findViewById(R.id.perf_vol_down);
        mPerfVolText = view.findViewById(R.id.perf_vol_text);
        return view;
    }

}

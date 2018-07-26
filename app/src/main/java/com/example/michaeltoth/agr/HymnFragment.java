package com.example.michaeltoth.agr;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class HymnFragment extends Fragment {
    private Hymn mHymn;
    private Button upButton;
    private Button downButton;
    private TextView volValueTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHymn = new Hymn();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hymn, container, false);
        upButton = (Button) v.findViewById(R.id.vol_up_button);
        downButton = (Button) v.findViewById(R.id.vol_down_button);
        volValueTextView = (TextView) v.findViewById(R.id.hymn_vol_value);
        return v;


    }
}

package com.example.michaeltoth.agr;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.michaeltoth.agr.BuildConfig;

public class HomeButtonFragment extends Fragment {
    private TextView mHomeText;
    private View buttonView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_buttons,container,false);
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        mHomeText = (TextView) view.findViewById(R.id.home_text_view);
        versionName = mHomeText.getText() + " Version: " + versionName;
        mHomeText.setText(versionName);
        return view;
    }

}

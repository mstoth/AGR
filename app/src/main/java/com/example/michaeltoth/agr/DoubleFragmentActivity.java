package com.example.michaeltoth.agr;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public abstract class DoubleFragmentActivity extends AppCompatActivity {
    protected abstract Fragment createListFragment();
    protected abstract Fragment createButtonFragment();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        Fragment listFragment = fm.findFragmentById(R.id.list_container);
        Fragment buttonFragment = fm.findFragmentById(R.id.button_container);

        if (listFragment==null) {
            listFragment = createListFragment();
            fm.beginTransaction().add(R.id.list_container, listFragment).commit();
        }
        if (buttonFragment==null) {
            buttonFragment = createButtonFragment();
            fm.beginTransaction().add(R.id.button_container, buttonFragment).commit();
        }
    }

}

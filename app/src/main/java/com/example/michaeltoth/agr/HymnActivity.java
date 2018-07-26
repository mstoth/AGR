package com.example.michaeltoth.agr;

import android.support.v4.app.Fragment;

public class HymnActivity extends DoubleFragmentActivity {
    @Override
    protected Fragment createListFragment() {
        return new HymnListFragment();
    }

    @Override
    protected Fragment createButtonFragment() {
        return new HymnButtonFragment();
    }
}

package com.example.michaeltoth.agr;

import android.support.v4.app.Fragment;

public class HymnListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new HymnListFragment();
    }
}

package com.example.michaeltoth.agr;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends DoubleFragmentActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    switchToHymnFragment();
                    return true;
                case R.id.navigation_dashboard:
                    switchToPerfFragment();
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

//        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

//        FragmentManager fm = getSupportFragmentManager();
//        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
//
//        if (fragment == null) {
//            fragment = new HymnFragment();
//            fm.beginTransaction().add(R.id.fragment_container,fragment).commit();
//        }
    }

    @Override
    protected Fragment createListFragment() {
        return new HymnListFragment();
    }

    @Override
    protected  Fragment createButtonFragment() { return new HymnButtonFragment(); }

    public void switchToHymnFragment() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.list_container, new HymnListFragment()).commit();
        manager.beginTransaction().replace(R.id.button_container,new HymnButtonFragment()).commit();
    }

    public void switchToPerfFragment() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.list_container, new HymnListFragment()).commit();
        manager.beginTransaction().replace(R.id.button_container, new PerfButtonFragment()).commit();
    }


}

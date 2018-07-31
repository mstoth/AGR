package com.example.michaeltoth.agr;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


public class MainActivity extends DoubleFragmentActivity implements TCPListener {

    private TCPCommunicator tcpClient;
    private Handler UIHandler = new Handler();
    private TextView mHomeTextView;
    private HymnBook hymnBook;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    switchToHomeFragment();
                    return true;
                case R.id.navigation_hymn:
                    switchToHymnFragment();
                    return true;
                case R.id.navigation_performance:
                    switchToPerfFragment();
                    return true;
                case R.id.navigation_recording:
                    switchToRecFragment();
                    return true;

            }
            return false;
        }
    };

    private void ConnectToServer() {
        tcpClient = TCPCommunicator.getInstance();
        tcpClient.init("192.168.1.4",10002);
        TCPCommunicator.addListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        hymnBook = HymnBook.get(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        tcpClient = TCPCommunicator.getInstance();
        if (!tcpClient.isConnected) {
            tcpClient.setServerHost("192.168.1.4");
            tcpClient.setServerPort(10002);
            ConnectToServer();
            tcpClient.addListener(this);
        } else {
            tcpClient.addListener(this);
            TCPCommunicator.writeStringToSocket("{\"mtype\":\"GIRQ\"}\n",UIHandler,getApplicationContext());
        }


    }

    @Override
    protected Fragment createListFragment() {
        return new HomeListFragment();
    }

    @Override
    protected  Fragment createButtonFragment() {
        return new HomeButtonFragment();
    }

    public void switchToHomeFragment() {
        tcpClient.removeAllListeners();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.list_container, new HomeListFragment()).commit();
        manager.beginTransaction().replace(R.id.button_container,new HomeButtonFragment()).commit();
        View v = findViewById(R.id.list_container);
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        lp.height = MATCH_PARENT;
        v.setLayoutParams(lp);

    }

    public void switchToHymnFragment() {
        tcpClient.removeAllListeners();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.list_container, new HymnListFragment()).commit();
        manager.beginTransaction().replace(R.id.button_container,new HymnButtonFragment()).commit();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int height=display.getHeight();
        View view = findViewById(R.id.list_container);
        ViewGroup.LayoutParams p = view.getLayoutParams();
        int h = p.height;
        p.height  = (int)(height/5);
        view.setLayoutParams(p);
        view.invalidate();
    }

    public void switchToPerfFragment() {
        tcpClient.removeAllListeners();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.list_container, new PerfListFragment()).commit();
        manager.beginTransaction().replace(R.id.button_container, new PerfButtonFragment()).commit();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int height=display.getHeight();
        View view = findViewById(R.id.list_container);
        ViewGroup.LayoutParams p = view.getLayoutParams();
        int h = p.height;
        p.height  = (int)(height/4);
        view.setLayoutParams(p);

    }

    public void switchToRecFragment() {
        tcpClient.removeAllListeners();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.list_container, new RecListFragment()).commit();
        manager.beginTransaction().replace(R.id.button_container, new RecButtonFragment()).commit();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int height=display.getHeight();
        View view = findViewById(R.id.list_container);
        ViewGroup.LayoutParams p = view.getLayoutParams();
        int h = p.height;
        p.height  = (int)(height/4);
        view.setLayoutParams(p);
    }


    @Override
    public void onTCPMessageRecieved(JSONObject message) {
        final JSONObject theMessage=message;
        try {

            final String messageTypeString=theMessage.getString("mtype");

            Log.d("DEBUG",messageTypeString);

            if (messageTypeString.equals("GIRP")) {
                final String msg = theMessage.getString("copyright");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String s = theMessage.getString("copyright");
                            mHomeTextView = findViewById(R.id.home_text_view);
                            mHomeTextView.setText(s);
                            View v = findViewById(R.id.list_container);
                            ViewGroup.LayoutParams lp = v.getLayoutParams();
                            lp.height = MATCH_PARENT;
                            v.setLayoutParams(lp);


                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            if (messageTypeString.equals("CPPP")) {
                final String messageSubTypeString=theMessage.getString("mstype");
                if (messageSubTypeString.equals("hymnplayer_hymn_list")) {
                    final JSONArray hymns = theMessage.getJSONArray("value");
                    hymnBook.setHymns(hymns);
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void onTCPConnectionStatusChanged(boolean isConnectedNow) {
        if(isConnectedNow)
        {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Toast.makeText(getApplicationContext(), "Connected to server", Toast.LENGTH_LONG).show();
                    TCPCommunicator.writeStringToSocket("{\"mtype\":\"GIRQ\"}\n",UIHandler,getApplicationContext());
                }
            });

        }
    }


}

package com.example.michaeltoth.agr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class TCPCommunicator {
    private static TCPCommunicator uniqInstance;
    private static String serverHost;
    private static int serverPort;
    private static List<TCPListener> allListeners;
    private static BufferedWriter out;
    private static BufferedReader in;
    private static Socket s;
    private static Handler UIHandler;
    private static Context appContext;
    private static boolean hymnsLoaded = false;
    private static boolean perfsLoaded = false;
    public static boolean isConnected = false;

    private TCPCommunicator()
    {
        allListeners = new ArrayList<TCPListener>();
    }

    public static TCPCommunicator getInstance()
    {
        if(uniqInstance==null)
        {
            uniqInstance = new TCPCommunicator();
        }
        return uniqInstance;
    }

    public void setHymnsLoaded(boolean loaded) {
        hymnsLoaded = loaded;
    }

    public boolean getHymnsLoaded() {
        return hymnsLoaded;
    }

    public void setPerfsLoaded(boolean loaded) {
        perfsLoaded = loaded;
    }

    public boolean getPerfsLoaded() {
        return perfsLoaded;
    }

    public  TCPWriterErrors init(String host,int port)
    {
        setServerHost(host);
        setServerPort(port);
        InitTCPClientTask task = new InitTCPClientTask();
        task.execute(new Void[0]);
        return TCPWriterErrors.OK;
    }
    public static TCPWriterErrors writeStringToSocket(final String msg, final Handler handle, Context context) {
        UIHandler = handle;
        appContext = context;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Timer timer = new Timer();
                try {
                    out.write(msg);
                    out.flush();
                    Log.i("TcpClient", "sent: " + msg);

                } catch(final Exception e) {
                    UIHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            Toast.makeText(appContext ,"a problem has occured, the app might not be able to reach the server" + e.toString(), Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        return TCPWriterErrors.OK;
    }

    public static  TCPWriterErrors writeToSocket(final JSONObject obj,Handler handle,Context context)
    {
        UIHandler=handle;
        appContext=context;
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try
                {
                    // String outMsg = obj.toString() + System.getProperty("line.separator");
                    String outMsg = obj.get(EnumsAndStatics.MESSAGE_CONTENT_FOR_JSON).toString();
                    out.write(outMsg + "\n");
                    out.flush();
                    Log.i("TcpClient", "sent: " + outMsg);
                }
                catch(Exception e)
                {
                    final Exception ef = e;
                    isConnected = false;
                    UIHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            Toast.makeText(appContext ,"a problem has occured, the app might not be able to reach the server. \n" + ef.getCause(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

        };
        Thread thread = new Thread(runnable);
        thread.start();
        return TCPWriterErrors.OK;

    }

    public static void addListener(TCPListener listener)
    {
        // allListeners.clear();
        allListeners.add(listener);
    }
    public static void removeAllListeners()
    {
        allListeners.clear();
    }

    public static void closeStreams()
    {
        try
        {
            s.close();
            in.close();
            out.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    public static String getServerHost() {

        return serverHost;
    }

    public static void setServerHost(String serverHost) {
        TCPCommunicator.serverHost = serverHost;
    }

    public static int getServerPort() {

        return serverPort;
    }

    public static void setServerPort(int serverPort) {

        TCPCommunicator.serverPort = serverPort;
    }


    public class InitTCPClientTask extends AsyncTask<Void, Void, Void>
    {
        public InitTCPClientTask()
        {

        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub

            try
            {
                String inMsg;
                String partial;
                s = new Socket(getServerHost(), getServerPort());
                in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                isConnected = true;
                for(TCPListener listener:allListeners)
                    listener.onTCPConnectionStatusChanged(true);
                while(true)
                {
                    char inCharArray[] = new char[10000];
                    int i, count;
                    char c;
                    int offset = 0;
                    count = 0;
                    partial = "";
                    while (simpleJSONValidator(partial)==false) {
                        i = in.read();
                        c = (char)i;
                        if (c=='{') {
                            count = count + 1;
                        }
                        if (c=='}') {
                            count = count - 1;
                        }
                        partial = partial + Character.toString(c);
                        if (count == 0) {
                            if (simpleJSONValidator(partial)) {
                                try {
                                    JSONObject j = new JSONObject(partial);
                                    for (TCPListener listener : allListeners)
                                        listener.onTCPMessageRecieved(j);
                                    Log.i("TcpClient", "received: " + partial);
                                    partial = "";
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }

    }

    public boolean simpleJSONValidator(String s) {
        int count = 0;
        int i;
        if (s.length()==0) {
            return false;
        }
        if (s.charAt(0) != '{') {
            return false;
        }
        for (i=0;i<s.length();i++) {
            if (s.charAt(i) == '{') {
                count = count + 1;
            }
            if (s.charAt(i) == '}') {
                count = count - 1;
            }
        }
        if (count == 0) {
            return true;
        } else {
            return false;
        }
    }


    public enum TCPWriterErrors{UnknownHostException,IOException,otherProblem,OK}


    class RetrySend extends TimerTask {

        @Override
        public void run() {

        }
    }
}
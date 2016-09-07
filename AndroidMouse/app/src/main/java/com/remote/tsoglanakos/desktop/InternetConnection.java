package com.remote.tsoglanakos.desktop;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by tsoglani on 20/7/2015.
 */
public class InternetConnection extends Service {

    public static int port = 2000;
    static Socket returnSocket = null;
    public static String lastIP;

    boolean isFromMobile;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        try {
            isFromMobile = intent.getBooleanExtra("isFromMobile", false);
        }catch (Exception e){
            isFromMobile=true;
        }
        returnSocket = null;

        new Thread() {
            @Override
            public void run() {
                try {


                    sendToAllIpInNetwork();
                    if (!found) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "No Wifi Network, or run the jar file on WLAN-Internet mode (read description) ", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception | Error e) {
                    e.printStackTrace();
                    if (MouseUIActivity.ps == null) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "No connection cause: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                        startActivity(new Intent(InternetConnection.this, MainActivity.class));
                    }
                }
            }
        }.start();


        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static boolean found = false;

    public boolean sendToAllIpInNetwork() throws UnknownHostException, IOException {
        found = false;
        ArrayList<String> ipList = getLocal();

        for (String ip : ipList) {

            if (returnSocket != null) {
                break;
            }
            for (int i = 1; i < 255; i++) {
                final String checkIp = ip + i;
                // Log.e("ip=",checkIp);
                if (returnSocket != null) {
                    break;
                }
                new Thread() {
                    public void run() {
                        try {
                            //      System.out.println(checkIp + "  :  " + InetAddress.getByName(checkIp).isReachable(2000));

                            final Socket s = new Socket(checkIp, port);
                            found = true;
                            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                            //  br.readLine();
                            if (returnSocket == null && s != null) {
                                returnSocket = s;
                                MouseUIActivity.ps = new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(returnSocket.getOutputStream())),
                                        true);
                                MouseUIActivity.ps.println("LOCAL_IP");
                                MainActivity.typeOfConntection = "LOCAL_IP";
                                MouseUIActivity.bf = new DataInputStream(returnSocket.getInputStream());

                                    Intent intent = new Intent(InternetConnection.this, MouseUIActivity.class);
                                if(isFromMobile){
                                    intent.putExtra("Type", "WLAN");
                                }else{
                                    intent.putExtra("Type", "Wear");
                                }

                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                sendMessage("/main","Search OK".getBytes());
                                Log.e("success   ", checkIp);
//                                Toast.makeText(InternetConnection.this, "Connected ", Toast.LENGTH_SHORT).show();
                                lastIP = checkIp;
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(s!=null&&s.getInetAddress()!=null)
                                        Toast.makeText(getApplicationContext(),"connected to "+s.getInetAddress().toString(), Toast.LENGTH_LONG).show();

                                    }
                                });


                            }
                        } catch (Exception ex) {
                            //   System.out.println(checkIp + " is not available");

                        }

                    }
                }.start();
//                new AsyncTask<Void, Void, Void>() {
//
//                    private Socket s = null;
//
//                    @Override
//                    protected Void doInBackground(Void... params) {
//                        try {
//                            Socket s = new Socket(checkIp, port);
//                            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
//
//                            br.readLine();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        return null;
//                    }
//
//                    @Override
//                    protected void onPostExecute(Void aVoid) {
//                        if (s != null && returnSocket == null) {
//                            returnSocket = s;
//                            try {
//                                MouseUIActivity.ps = new PrintWriter(new BufferedWriter(
//                                        new OutputStreamWriter(returnSocket.getOutputStream())),
//                                        true);
//
//                                MouseUIActivity.bf = new DataInputStream(returnSocket.getInputStream());
//                                Intent intent = new Intent(context.getb, MouseUIActivity.class);
//                                intent.putExtra("Type", "Internet");
//                               InternetConnection.this.context.startActivity(intent);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }.execute();

                if (found) {
                    break;
                }
            }
            if (found) {
                break;
            }

        }
        return found;
    }


    public void createConnection() {
        client = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
//                        toast("Connection Faild");

                    }
                })
                .addApi(Wearable.API)
                .build();

        client.connect();

    }

    private GoogleApiClient client;

    private void sendMessage(final String message, final byte[] payload) {
        if (client == null) {
            createConnection();
        }
        Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                List<Node> nodes = getConnectedNodesResult.getNodes();
                for (Node node : nodes) {


                    Wearable.MessageApi.sendMessage(client, node.getId(), message, payload).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (sendMessageResult.getStatus().isSuccess()) {

                            }else{
                                client=null;
                            }

                        }
                    });
                }

            }
        });
    }



    private static ArrayList<String> getLocal() throws SocketException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        ArrayList<String> list = new ArrayList<String>();
        while (e.hasMoreElements()) {

            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {

                InetAddress inet = (InetAddress) ee.nextElement();
                if (!inet.isLinkLocalAddress()) {

                    String hostAdd = inet.getHostAddress();
                    // System.out.println(hostAdd);
                    String str = "";
                    String[] ars = hostAdd.split("\\.");
                    //    System.out.println("ars.length = " + ars.length);
                    for (int j = 0; j < ars.length - 1; j++) {
                        //    System.out.println(ars[j]);
                        str += ars[j] + ".";
                    }
                    //  System.out.println("str = " + str);
                    list.add(str);
                }
            }
        }
        return list;
    }


}

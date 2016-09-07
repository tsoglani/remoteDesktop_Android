package com.remote.tsoglanakos.desktop;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private Button local_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                sendMessage("/main", ("main").getBytes());
                local_search = (Button) stub.findViewById(R.id.local_search);
                local_search.setBackground(getResources().getDrawable(R.drawable.auto_connection));
                local_search.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AsyncTask<Void,Void,Void>(){
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                local_search.setEnabled(false);
                                local_search.setBackground(getResources().getDrawable(R.drawable.waiting_connection));
                            }

                            @Override
                            protected Void doInBackground(Void... params) {
                                sendMessage("/main", ("Search_WLAN").getBytes());
                                try {
                                    Thread.sleep(6000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                local_search.setEnabled(true);
                                local_search.setBackground(getResources().getDrawable(R.drawable.auto_connection));

                            }
                        }.execute();
                    }
                });
            }
        });


    }

    private GoogleApiClient client;


    public void createConnection() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        toast("Connection Faild");

                    }
                })
                .addApi(Wearable.API)
                .build();

        client.connect();

    }


    private void toast(final String s) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(final String message, final byte[] payload) {
        if (client == null) {
            createConnection();
        }

        Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                List<Node> nodes = getConnectedNodesResult.getNodes();
                for (Node node : nodes) {

                    //toast(node.getId());
                    Wearable.MessageApi.sendMessage(client, node.getId(), message, payload).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (sendMessageResult.getStatus().isSuccess()) {
                                if (new String(payload).equals("close_application")) {
                                    if (client!= null)
                                        client.disconnect();
                                    client = null;
                                }
                            } else {
                                client = null;
                                toast("Not connected with phone device");
                            }
                        }
                    });
                }

            }
        });
    }


    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(client, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        String message = new String(messageEvent.getData());
        if (message.equalsIgnoreCase("Search OK")) {
            goToRemoteMouseView();

        }
    }

    private void goToRemoteMouseView() {
//       Toast.makeText(MainActivity.this, "goToRemoteMouseView", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, MouseView.class);
        Wearable.MessageApi.removeListener(client, this);
        client.disconnect();
        client = null;
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendMessage("/main", "close_application".getBytes());
        Wearable.MessageApi.removeListener(client, this);

    }
}
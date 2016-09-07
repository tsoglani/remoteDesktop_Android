package com.remote.tsoglanakos.desktop;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.IOException;
import java.util.List;

public class MobileService extends WearableListenerService {


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        //Log.i(MobileService.class.getSimpleName(), "WEAR Message " + messageEvent.getPath());

        String message = new String(messageEvent.getData());
//        Toast.makeText(MobileService.this, "Message = "+message, Toast.LENGTH_SHORT).show();

        if (message.equals("start")) {
            MouseUIActivity.type = "Wear";
        } else if (message.equals("Search_WLAN")) {
            if (MouseUIActivity.ps != null) {
                clearSocket();
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(startMain);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Intent intent = new Intent(this, InternetConnection.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("isFromMobile", false);
            startService(intent);
        } else if (message.equals("close_connection")) {

            clearSocket();

        } else if (message.equals("main")) {
            Intent startMain = new Intent(this, MainActivity.class);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(startMain);
        } else if (message.equals("close_application")) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(startMain);
        } else if (message.startsWith("MoveTo:")) {
            if (MouseUIActivity.ps != null) {
                if (PageOneFragment.zoomValue != 0) {
                    PageOneFragment.zoomValue = 0;
                    MouseUIActivity.ps.println("ZOOM:" + PageOneFragment.zoomValue);
                }
                MouseUIActivity.ps.println(message);
            }
        } else if (message.equals("LEFT_CLICK")) {
            if (MouseUIActivity.ps != null)
                MouseUIActivity.ps.println("LEFT_CLICK_DOWN");
            MouseUIActivity.ps.println("LEFT_CLICK_UP");
        } else if (message.startsWith("LEFT_CLICK") || message.startsWith("RIGHT_CLICK")) {
            MouseUIActivity.ps.println(message);

        }


    }


    private void clearSocket() {
        if (MouseUIActivity.ps != null) {
            MouseUIActivity.ps.close();
        }
        MouseUIActivity.ps = null;
        if (InternetConnection.returnSocket != null) {
            try {
                InternetConnection.returnSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    private void toast(final String s) {

        Toast.makeText(MobileService.this, s, Toast.LENGTH_SHORT).show();

    }


    public void onReadyForContent() {
        client = new GoogleApiClient.Builder(this)
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

    private GoogleApiClient client;

    private void sendMessage(final String message, final byte[] payload) {
        if (client == null) {
            onReadyForContent();
        }
        if (client != null)
            Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    List<Node> nodes = getConnectedNodesResult.getNodes();
                    for (Node node : nodes) {


                        Wearable.MessageApi.sendMessage(client, node.getId(), message, payload).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                if (sendMessageResult.getStatus().isSuccess()) {

                                } else {
                                    client = null;
                                }

                            }
                        });
                    }

                }
            });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (client != null) {
            client.disconnect();

        }

    }

}
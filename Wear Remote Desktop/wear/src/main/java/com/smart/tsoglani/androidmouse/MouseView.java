package com.smart.tsoglani.androidmouse;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MouseView extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    static FrameLayout frameLayout;
    private BoxInsetLayout mainFrameLayout;
    private View coursor;
    private Point startPoint, coursorStartPoint;
    private Button back;
    static MouseView mouseView;
    private int Width, Height;
    private long startEvent, finishEvent;
    boolean isRound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mouse_view);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub_mouse_view);
        stub.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {

            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                mouseView=MouseView.this;
                //////////////////////////////////////////////////
                // IMPORTANT - the following line is required
                stub.onApplyWindowInsets(windowInsets);

                // Set the instance variable to say whether this is round...
                isRound = windowInsets.isRound();
                return windowInsets;
            }
        });
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                createDataConnection();
                sendMessage("/mouseView", "start".getBytes());
                coursor = findViewById(R.id.coursor);
                frameLayout = (FrameLayout) findViewById(R.id.mouseView);
                mainFrameLayout = (BoxInsetLayout) findViewById(R.id.mainMouseView);
                Width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 2.2);
                Height = (int) (getWindowManager().getDefaultDisplay().getHeight() * 2.2);
                BoxInsetLayout.LayoutParams params = new BoxInsetLayout.LayoutParams(Width, Height);
                coursor.setX(mainFrameLayout.getWidth() / 2);
                coursor.setY(mainFrameLayout.getHeight() / 2);
                frameLayout.setLayoutParams(params);

                frameLayout.setX(-Width / 2 + getWindowManager().getDefaultDisplay().getWidth() / 2);
                frameLayout.setY(-Height / 2 + getWindowManager().getDefaultDisplay().getHeight() / 2);


                back = (Button) findViewById(R.id.back);
                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        Intent intent = new Intent(MouseView.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    }
                });
                frameLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();

                        if (chechForDoubleTouchDown(event)) {
                            return true;
                        }
                        if (MotionEvent.ACTION_DOWN == action) {
                            startPoint = new Point((int) event.getX(), (int) event.getY());
                            coursorStartPoint = new Point((int) coursor.getX(), (int) coursor.getY());
                            startEvent = event.getEventTime();
                        } else if (action == MotionEvent.ACTION_MOVE) {
                            int difX = (int) event.getX() - startPoint.x;
                            int difY = (int) event.getY() - startPoint.y;
                            coursor.setX(coursorStartPoint.x + difX);
                            coursor.setY(coursorStartPoint.y + difY);
                            if (coursor.getX() > frameLayout.getWidth()) {
                                coursor.setX(frameLayout.getWidth());
                            }
                            if (coursor.getX() < -coursor.getWidth()) {
                                coursor.setX(-coursor.getWidth());
                            }

                            if (coursor.getY() > frameLayout.getHeight()) {
                                coursor.setY(frameLayout.getHeight());
                            }
                            if (coursor.getX() < -coursor.getHeight()) {
                                coursor.setX(-coursor.getHeight());
                            }
                            sendMessage("/mouseView", ("MoveTo:x=" + (coursor.getX()) / frameLayout.getWidth() + ":y=" + (coursor.getY()) / frameLayout.getHeight()).getBytes());

                        } else if (action == MotionEvent.ACTION_UP) {


                            float frameLayoutX = frameLayout.getX(), frameLayoutY = frameLayout.getY();

                            if (!isRound) {
                                frameLayoutX = -coursor.getX() + getWindowManager().getDefaultDisplay().getWidth() / 2;
                                frameLayoutY = -coursor.getY() + getWindowManager().getDefaultDisplay().getHeight() / 2;
                                if (frameLayoutX > 0) {

                                    frameLayoutX = 0;
                                }
                                if (frameLayoutX < -Width + getWindowManager().getDefaultDisplay().getWidth()) {
                                    frameLayoutX = -Width + getWindowManager().getDefaultDisplay().getWidth();
                                }
//
                                if (frameLayoutY > 0) {
                                    frameLayoutY = 0;
                                }
                                if (frameLayoutY < -Height + getWindowManager().getDefaultDisplay().getHeight()) {
                                    frameLayoutY = -Height + getWindowManager().getDefaultDisplay().getHeight();
                                }

                            } else {

                                int extraX = getWindowManager().getDefaultDisplay().getWidth() / 7, extraY = getWindowManager().getDefaultDisplay().getHeight() / 7;
                                frameLayoutX = -coursor.getX() + getWindowManager().getDefaultDisplay().getWidth() / 2;
                                frameLayoutY = -coursor.getY() + getWindowManager().getDefaultDisplay().getHeight() / 2;
                                if (frameLayoutX > extraX) {

                                    frameLayoutX = extraX;
                                }
                                if (frameLayoutX < -Width + getWindowManager().getDefaultDisplay().getWidth() - extraX) {
                                    frameLayoutX = -Width + getWindowManager().getDefaultDisplay().getWidth() - extraX;
                                }
//
                                if (frameLayoutY > extraY) {
                                    frameLayoutY = extraY;
                                }
                                if (frameLayoutY < -Height + getWindowManager().getDefaultDisplay().getHeight() - extraY) {
                                    frameLayoutY = -Height + getWindowManager().getDefaultDisplay().getHeight() - extraY;
                                }

                            }


                            frameLayout.setX(frameLayoutX);
                            frameLayout.setY(frameLayoutY);


                            finishEvent = event.getEventTime();

                            float ydif = event.getY() - startPoint.y, xdif = event.getX() - startPoint.x;
                            if ((finishEvent - startEvent < 110) && (Math.abs(ydif) < 10 || Math.abs(xdif) < 10)) {
                                sendMessage("/mouseView", "LEFT_CLICK".getBytes());
                            }


                            startPoint = null;
                        }
                        return true;
                    }
                });


            }
        });
    }


    boolean chechForDoubleTouchDown(MotionEvent m) {
        //Number of touches
        int pointerCount = m.getPointerCount();
        int action = m.getActionMasked();

        if (action == MotionEvent.ACTION_POINTER_DOWN && (pointerCount == 2)) {
            sendMessage("/mouseView", "RIGHT_CLICK".getBytes());
            return true;
        }
        return false;
    }


    static GoogleApiClient dataClient;

    private GoogleApiClient messageClient;

    public void createDataConnection() {
        dataClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        toast("Connection Faild");

                    }
                })
                .addApi(Wearable.API)
                .build();

        dataClient.connect();

    }


    public void createMessageConnection() {
        messageClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        toast("Connection Faild");

                    }
                })
                .addApi(Wearable.API)
                .build();

        messageClient.connect();

    }


    private void toast(final String s) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                Toast.makeText(MouseView.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(final String message, final byte[] payload) {
        if (messageClient == null) {
            createMessageConnection();
        }

        Wearable.NodeApi.getConnectedNodes(messageClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                List<Node> nodes = getConnectedNodesResult.getNodes();
                for (Node node : nodes) {
                    Wearable.MessageApi.sendMessage(messageClient, node.getId(), message, payload).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (sendMessageResult.getStatus().isSuccess()) {
                                if (new String(payload).equals("close_connection")) {
                                    closeDataConnection();
                                    closeMessageConnection();
                                }
                            } else {

                                toast("not sended");
                            }
                        }
                    });
                }

            }
        });
    }





    private void closeDataConnection() {
        if (dataClient != null) {
//            Wearable.DataApi.removeListener(dataClient, this);
            dataClient.disconnect();
            dataClient = null;
        }
    }

    private void closeMessageConnection() {
        if (messageClient != null) {
            messageClient.disconnect();
            messageClient = null;
        }
    }
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
////        sendMessage("/mouseView", "close_connection".getBytes());
////        Toast.makeText(MouseView.this, "Back", Toast.LENGTH_SHORT).show();
////        closeDataConnection();
//
//    }


    @Override
    protected void onStop() {
        super.onStop();
        closeDataConnection();
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessage("/mouseView", "close_connection".getBytes());
            }
        }.start();

        closeMessageConnection();
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeDataConnection();
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessage("/mouseView", "close_connection".getBytes());

            }
        }.start();


    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (dataClient == null || !dataClient.isConnected()) {
            dataClient = null;
            createDataConnection();
        }
//        if (dataClient != null)
//            Wearable.DataApi.addListener(dataClient, this);

    }

    AsyncTask<Void, Void, Void> async;

//    @Override
//    public void onDataChanged(DataEventBuffer dataEventBuffer) {
//
//
//        for (final DataEvent event : dataEventBuffer) {
//            if (event.getType() == DataEvent.TYPE_CHANGED &&
//                    event.getDataItem().getUri().getPath().equals("/image")) {
//                if (async != null) {
//                    continue;
//                }
//                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
//                final Asset profileAsset = dataMapItem.getDataMap().getAsset("profileImage");
//                async = new AsyncTask<Void, Void, Void>() {
//                    Bitmap bitmap;
//                    BitmapDrawable ob;
//
//                    @Override
//                    protected Void doInBackground(Void... params) {
//                        try {
//                            bitmap = loadBitmapFromAsset(profileAsset);
//                            ob = new BitmapDrawable(getResources(), bitmap);
//
////                        Wearable.DataApi.deleteDataItems(dataClient, getUriForDataItem());
////                        dataClient.disconnect();
//
//                        } catch (Exception e) {
//                            Intent intent = new Intent(MouseView.this, MainActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
//                        }
//                        return null;
//                    }
//
//                    @Override
//                    protected void onPostExecute(Void aVoid) {
//                        super.onPostExecute(aVoid);
//                        if (ob != null) {
//                            frameLayout.setBackground(ob);
//
////                            dataClient.disconnect();
////                            dataClient=null;
//                        }
//
//
//                    }
//                };
//                async.execute();
////                Bitmap bitmap = loadBitmapFromAsset(profileAsset);
//
//
//                // Do something with the bitmap
//            } else if (event.getType() == DataEvent.TYPE_CHANGED &&
//                    event.getDataItem().getUri().getPath().equals("/close")) {
//                Intent intent = new Intent(this, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//        }
//        dataEventBuffer.release();
//
//    }
//
//    private int TIMEOUT_MS = 10000;

//    public Bitmap loadBitmapFromAsset(Asset asset) {
//
//        if (asset == null) {
//            throw new IllegalArgumentException("Asset must be non-null");
//        }
//
//        ConnectionResult result = dataClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
//        if (!result.isSuccess()) {
//            return null;
//        }
//        // convert asset into a file descriptor and block until it's ready
//        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
//                dataClient, asset).await().getInputStream();
//        Wearable.DataApi.removeListener(dataClient, MouseView.this);
//        dataClient.disconnect();
//        dataClient = null;
//        async = null;
//        createDataConnection();
//
//        if (assetInputStream == null) {
//            Log.w("request uknown", "Requested an unknown Asset.");
//            return null;
//        }
//        // decode the stream into a bitmap
//        return BitmapFactory.decodeStream(assetInputStream);
//    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(MouseView.this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }


}

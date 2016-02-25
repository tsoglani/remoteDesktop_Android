package com.smart.tsoglani.androidmouse;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Toast;

import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.IOException;

/**
 * Created by tsoglani on 25/2/2016.
 */
public class WearService extends WearableListenerService {


    @Override
    public void onChannelOpened(Channel channel) {

        if (channel.getPath().equals("/image")) {
            if (MouseView.dataClient == null || MouseView.frameLayout == null) {
                return;
            }
//            File file = new File("/sdcard/file.png");
//            if (!file.exists())
//                try {
//                    file.createNewFile();
//                } catch (IOException e) {
//                    //handle error
//                }
            byte[] data = new byte[800 * 800];
            try {
                channel.getInputStream(MouseView.dataClient).await().getInputStream().read(data);
               final  Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                MouseView.mouseView.runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        MouseView.frameLayout.setBackground(new BitmapDrawable(bitmap));
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
//                channel.receiveFile(dataClient, Uri.fromFile(file), false);
        } else if (channel.getPath().equals("/close")) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    //when file is ready
    @Override
    public void onInputClosed(Channel channel, int i, int i1) {

//        Toast.makeText(this, "File received!", Toast.LENGTH_SHORT).show();

    }
}
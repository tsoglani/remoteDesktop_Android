package com.nikos.tsoglani.androidmouse;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

/**
 * Created by tsoglani on 20/7/2015.
 */
public class MyBlueTooth extends AsyncTask<Void, Boolean, Boolean> {
    Activity context;

    public MyBlueTooth(Activity context) {
        this.context = context;

    }

    // private BluetoothAdapter mBluetoothAdapter;

    // This will find a bluetooth printer device

    boolean found = false;

    @Override
    protected void onPreExecute() {
//        context.runOnUiThread(new Thread() {
//            @Override
//            public void run() {
//                Button internet = (Button) context.findViewById(com.nikos.tsoglani.androidmouse.R.id.internetbuttonn);
//                internet.setEnabled(false);
//                Button bluetooth = (Button) context.findViewById(com.nikos.tsoglani.androidmouse.R.id.bluetoothbutton);
//                bluetooth.setEnabled(false);
//            }
//        });

        super.onPreExecute();
    }

    boolean findBT() throws Exception {


        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                .getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (final BluetoothDevice device : pairedDevices) {
                Log.e("dev name" + device.getName().toString(), device.getAddress());
                // MP300 is the name of the bluetooth printer device
                //    if (device.getName().equals("GT-I9300")) {

                openBT(device);


//                        break;
                //                  }
            }


        }

        return found;
    }

    // mmSocket;

    // Tries to open a connection to the bluetooth printer device
    void openBT(BluetoothDevice mmDevice) throws IOException {
        try {
            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            BluetoothSocket    mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect(); /// waits until connected

            BufferedReader br = new BufferedReader(new InputStreamReader(mmSocket.getInputStream()));
            br.readLine();
            MouseUIActivity.ps = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(mmSocket.getOutputStream())),
                    true);

            MouseUIActivity.bf=new DataInputStream(mmSocket.getInputStream()) ;
            found = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected Boolean doInBackground(Void... params) {
        boolean b = false;
        try {
            b = findBT();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return found;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        context.runOnUiThread(new Thread() {
            @Override
            public void run() {
//                Button internet = (Button) context.findViewById(com.nikos.tsoglani.androidmouse.R.id.internetbuttonn);
//                internet.setEnabled(true);
//                Button bluetooth = (Button) context.findViewById(com.nikos.tsoglani.androidmouse.R.id.bluetoothbutton);
//                bluetooth.setEnabled(true);
            }
        });
        super.onPostExecute(aBoolean);
    }
}

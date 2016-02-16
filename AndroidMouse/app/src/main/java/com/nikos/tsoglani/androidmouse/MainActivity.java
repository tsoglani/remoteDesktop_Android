package com.nikos.tsoglani.androidmouse;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

import lt.lemonlabs.android.expandablebuttonmenu.ExpandableButtonMenu;
import lt.lemonlabs.android.expandablebuttonmenu.ExpandableMenuOverlay;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


public class MainActivity extends ActionBarActivity {
    static String typeOfConntection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(com.nikos.tsoglani.androidmouse.R.layout.activity_main);
        getWindow().setBackgroundDrawable(null);
        typeOfConntection=null;
        if(InternetConnection.returnSocket!=null){
            try {
                InternetConnection.returnSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            InternetConnection.returnSocket = null;
        }

        final ExpandableMenuOverlay menuOverlay = (ExpandableMenuOverlay) findViewById(R.id.button_menu);

        showHelp(false);


        menuOverlay.setOnMenuButtonClickListener(new ExpandableButtonMenu.OnMenuButtonClick() {
            @Override
            public void onClick(ExpandableButtonMenu.MenuButton action) {
                switch (action) {
                    case MID:
                        // do stuff and dismiss
                        if (!isNetworkAvailable()) {
                            noInternetConnectionEnableFunction();
                        } else {
                            connectFunction(menuOverlay);
                        }
                        menuOverlay.getButtonMenu().toggle();
                        break;
                    case LEFT:
                        bluetoothConnectFunction(menuOverlay);
                        menuOverlay.getButtonMenu().toggle();
                        break;
                    case RIGHT:
                        if (!isNetworkAvailable()) {
                            noInternetConnectionEnableFunction();
                        } else {
                            internet(menuOverlay);
                        }
                        menuOverlay.getButtonMenu().toggle();
                        break;
                }
            }
        });

    }


    private void noInternetConnectionEnableFunction() {
        try {
            Toast.makeText(MainActivity.this, "WIFI is closed", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("Open WIFI");

            builder.setMessage("Do you want to open Wifi ...?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } catch (Exception ex) {
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showHelp(final boolean showAnyway) {

        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Thread() {
                    @Override
                    public void run() {


                        ShowcaseConfig config = new ShowcaseConfig();
                        config.setDelay(500); // half second between each showcase view

                        MaterialShowcaseSequence sequence;
                        if (showAnyway) {
                            sequence = new MaterialShowcaseSequence(MainActivity.this);
                        } else {
                            sequence = new MaterialShowcaseSequence(MainActivity.this, "first_time_expandable_menubutton");
                        }
                        View v = findViewById(R.id.button_menu);
                        sequence.setConfig(config);
                        sequence.addSequenceItem(v, "Press Computer Connection to start searching for a connection. You must download first the jar file to your computer (read description)" +
                                " and choose between \"Bluetooth\" or \"WLAN-Internet\" depending on the connection type you want to use(on computer side).", "");


                        sequence.addSequenceItem(v, "After pressing Computer Connection button, you will be able to choose between three types of connection: \n" +
                                "1- Bluetooth connection : needs to pair the mobile and computer device via bluetooth." +
                                "\n2- Auto WLAN connection (WLAN mode) : the mobile just has to be at the same network with the computer. " +
                                "\n3- IP connection : Needs IP and username (is shown on jar file -> computer device), you can connect with your computer device from every network. Hole punching required.", "");


                        sequence.start();
                    }
                });
            }
        }.start();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.nikos.tsoglani.androidmouse.R.menu.menu_main, menu);


        return true;
    }

    public void helpFunction(View v) {
        showHelp(true);
    }


    public void changePortFunction(View v) {


        final EditText portView = new EditText(this);
        portView.setText(Integer.toString(InternetConnection.port));
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Change port (must also change on computer side).")
                        //.setMessage("Enter Public ip")

                .setView(portView)

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            InternetConnection.port = Integer.parseInt(portView.getText().toString().replaceAll(" ", ""));
                            runOnUiThread(new Thread() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "After closing the app the port value will be back to default (2000).", Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (Exception e) {
                            InternetConnection.port = 2000;

                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        })
                .show();
    }


    public void connectFunction(View v) {
        Intent intent = new Intent(this, InternetConnection.class);
        intent.putExtra("isFromMobile", true);
        startService(intent);

    }

    String ip = null;

    public void internet(View v) {

        startActivity(new Intent(this, ConnectionHistory.class));
//        internetAlgorithm();
    }

    public void internetAlgorithm() {

        try {


            final EditText input = new EditText(MainActivity.this);
            SharedPreferences settings = getSharedPreferences("RemoteControl", 0);
            input.setText(settings.getString("Global Ip", "").toString());
            ip = input.getText().toString();
            LinearLayout ll = new LinearLayout(getApplicationContext());
            final LinearLayout showLayout = new LinearLayout(getApplicationContext());
            showLayout.setOrientation(LinearLayout.VERTICAL);
            TextView txtView1 = new TextView(getApplicationContext());
            txtView1.setText("Enter Public ip");
            ll.addView(txtView1);
            ll.addView(input);
            input.setMinEms(10);

            final EditText input2 = new EditText(MainActivity.this);


            input2.setText(settings.getString("NickName", "").toString());
            input2.setFocusable(true);
            input2.setClickable(true);
            input2.setFocusableInTouchMode(true);
            input2.setSelectAllOnFocus(true);
            input2.setSingleLine(true);
            final LinearLayout ll2 = new LinearLayout(getApplicationContext());
            TextView txtView2 = new TextView(getApplicationContext());
            txtView2.setText("Computer's Name");
            input2.setMinEms(10);
            ll2.addView(txtView2);
            ll2.addView(input2);
            showLayout.addView(new View(getApplicationContext()), new ViewGroup.LayoutParams(100, 50));
            showLayout.addView(ll);
            showLayout.addView(new View(getApplicationContext()), new ViewGroup.LayoutParams(100, 200));
            showLayout.addView(ll2);
            runOnUiThread(new Thread() {
                @Override
                public void run() {
                    runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(getApplicationContext(), "Be sure you have the same username on computer", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {

                            }
                        }
                    });
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Remote Controll with public IP")
                                    //.setMessage("Enter Public ip")

                            .setView(showLayout)

                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String ip2 = input.getText().toString();
                                    if (ip2 == null || ip2.replace(" ", "").equals("")) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {

                                                Toast.makeText(getApplicationContext(), "Enter a real IP", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                        return;
                                    }
                                    if (ip != ip2) {
                                        SharedPreferences settings = getSharedPreferences("RemoteControl", 0);
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putString("Global Ip", input.getText().toString());


                                        editor.putString("NickName", input2.getText().toString());


                                        editor.commit();
                                        // deal with the editable
                                    }
                                    ip = ip2;
                                    if (ip == null || ip.equals("")) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "Not Valid IP .. ", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        return;
                                    }
                                    runOnUiThread(new Thread() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Wait .. ", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            try {

                                                Socket s = new Socket();

                                                // s.setReuseAddress(true);
                                                s.connect(new InetSocketAddress(ip, InternetConnection.port));
                                                InternetConnection.lastIP = ip;
                                                runOnUiThread(new Thread() {
                                                    @Override
                                                    public void run() {

                                                        Toast.makeText(getApplicationContext(), "success .. ", Toast.LENGTH_SHORT).show();

                                                    }
                                                });


                                                InternetConnection.returnSocket = s;
                                                MouseUIActivity.ps = new PrintWriter(InternetConnection.returnSocket.getOutputStream(), true);
                                                MouseUIActivity.bf = new DataInputStream(InternetConnection.returnSocket.getInputStream());
                                                MouseUIActivity.ps.println("GLOBAL_IP:" + input2.getText().toString());
                                                typeOfConntection = "GLOBAL_IP:" + input2.getText().toString();
                                                runOnUiThread(new Thread() {
                                                    @Override
                                                    public void run() {
                                                        Log.e(Boolean.toString(InternetConnection.returnSocket.isConnected()), "InternetConnection.returnSocket.isConnected()");
                                                        Intent intent = new Intent(MainActivity.this, MouseUIActivity.class);
                                                        intent.putExtra("Type", "Internet");
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(intent);

                                                    }
                                                });
                                            } catch (final Exception e) {
                                                runOnUiThread(new Thread() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        } catch (Exception e) {

                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }.start();


                                }
                            })
                            .show();
                }
            });

//                        s.connect((new InetSocketAddress(InetAddress.getByName("78.87.53.120"), 2000)), 5000);

//                    s = new Socket(ip, 6667);


        } catch (final Exception e) {
            e.printStackTrace();
            runOnUiThread(new Thread() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), "Error:  " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


    }


    public void infoFunction(View v) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        TextView message = new TextView(MainActivity.this);
        Spanned myLnk = Html.fromHtml("<a href=https://raw.githubusercontent.com/tsoglani/AndroidRemoteJavaServer/master/Internet%20Image%20Example/Screenshot%202.png> Hole punching Example</a>");


        message.setText("  This application makes possible to control your computer's Device from your mobile phone, you are able to move your mouse from your android device.\n" +
                        "works via Internet Connection(need hole punching), Wifi (Wlan-Hotspot) or Bluetooth, you have just to download and run this jar file ->(https://github.com/tsoglani/AndroidRemoteJavaServer/tree/master/storePaid press Server Remote Control.jar and View Raw to download it) on your computer, and choose the type of connection you want to use(Wlan, bluetooth)." +
                        " \n-Parameters for Wlan connection :\n" +
                        "1) The computer must share a local network.\n" +
                        "2) The android device must be connected to the computer's network.\n" +
                        "\n" +
                        "-Parameters for Bluetooth connection :\n" +
                        "1) You must have already done the connection (You must have the computer on my devices before use it).\n" +
                        "2) Much more slower than network connection and needs time to connect (so, not recommended).\n" +
                        " \n-Parameters for Internet connection :\n" +
                        "1) You have to do port forwarding.\n" +
                        "On PC GOTO : Router preferences (propubly needs to press 192.168.1.1 on your Browser)->Nat ->Virtual Server -> Lan ip Address = your pc local address->Lan port=2000, public port=2000 (these are the default values)  -->"
        );
        message.append(myLnk);
        message.append("\n" +
                "2) Enter the Public/External PC ip in your android device " +
                "\n" +
                "3)Your computer must run this jar file ->(https://github.com/tsoglani/AndroidRemoteJavaServer/tree/master/storePaid) on WLAN-Internet mode.\n" +
                "\n" +
                "\n" +
                "\n lib used : \n " +
                "https://github.com/zzz40500/AndroidSweetSheet \n" +
                "https://github.com/Yalantis/Side-Menu.Android\n" +
                "https://github.com/ozodrukh/CircularReveal\n" +
                "https://github.com/lemonlabs/ExpandableButtonMenu\n" +
                "https://github.com/darsh2/MultipleImageSelect/blob/master/README.md\n" +
                "https://github.com/AnderWeb/discreteSeekBar\n\n" +
                "Thanks guys .." +

                "\n\n Directed By Tsoglani");
        message.setMovementMethod(LinkMovementMethod.getInstance());

        // set title
        alertDialogBuilder.setTitle("Info");
        // Linkify.addLinks();        // set dialog message
        alertDialogBuilder
                .setView(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity

                    }
                });


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it\

        alertDialog.show();


    }


    public void bluetoothConnectFunction(View v) {

        Toast.makeText(getApplicationContext(), "Wait", Toast.LENGTH_LONG).show();
        new Thread() {
            @Override
            public void run() {


                try {
                    if (new MyBlueTooth(MainActivity.this).execute().get()) {
                        Intent intent = new Intent(MainActivity.this, MouseUIActivity.class);
                        intent.putExtra("Type", "bluetooth");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        typeOfConntection = "Bluetooth";
                    } else {
                        runOnUiThread(new Thread() {
                            @Override
                            public void run() {

                                Toast.makeText(getApplicationContext(), "No Bluetooth connection", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(1);
    }
}

package com.nikos.tsoglani.androidmouse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ConnectionHistory extends Activity {
    LinearLayout connection_history_linear;
    DB_connectionHistory db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_history);
        connection_history_linear = (LinearLayout) findViewById(R.id.connection_history_linear);
        db = new DB_connectionHistory(this);
        loadHistory();
    }

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }

    private void loadHistory() {
        connection_history_linear.setGravity(Gravity.CENTER);
        ArrayList<String> list = db.getAllCotacts();

        for (int i = 0; i < list.size(); i++) {
            RelativeLayout rl = (RelativeLayout) getLayoutInflater().inflate(R.layout.connection_history_tab, null);
            String value = list.get(i);
            final TextView ipText = (TextView) rl.findViewById(R.id.IP_text);
            final TextView usernameText = (TextView) rl.findViewById(R.id.username_text);
            ipText.setText(value.split("@@@")[0]);
            usernameText.setText(value.split("@@@")[1]);
            connection_history_linear.addView(rl);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(5));

            final Button connect = (Button) rl.findViewById(R.id.connectButton);
            connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connect(ipText.getText().toString(), usernameText.getText().toString());
                }
            });

            final Button delete = (Button) rl.findViewById(R.id.delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            db.deleteContact(ipText.getText().toString(), usernameText.getText().toString());
                        }
                    });
                }
            });

            final Button edit = (Button) rl.findViewById(R.id.edit);
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    editInternetAlgorithm(ipText.getText().toString(), usernameText.getText().toString());
                }
            });
            View v = new View(this);
            v.setLayoutParams(params);
            connection_history_linear.addView(v);

        }

        Button button = new Button(this);
        button.setGravity(Gravity.CENTER_HORIZONTAL);

        ViewGroup.LayoutParams addNewParams = new ViewGroup.LayoutParams(  dpToPx(50), dpToPx(50));
        button.setLayoutParams(addNewParams);
        button.setBackgroundResource(R.drawable.add);
        connection_history_linear.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                internetAlgorithm();

            }
        });
    }

    public void editInternetAlgorithm(final String inputIp, final String inputUsername) {

        try {


            final EditText ipInput = new EditText(ConnectionHistory.this);
//            SharedPreferences settings = getSharedPreferences("RemoteControl", 0);
//            ipInput.setText(settings.getString("Global Ip", "").toString());
            ip = ipInput.getText().toString();
            LinearLayout ll = new LinearLayout(getApplicationContext());
            final LinearLayout showLayout = new LinearLayout(getApplicationContext());
            showLayout.setOrientation(LinearLayout.VERTICAL);
            TextView txtView1 = new TextView(getApplicationContext());
            txtView1.setText("Enter Public ip");
            ll.addView(txtView1);
            ll.addView(ipInput);
            ipInput.setMinEms(10);

            final EditText usernameInput = new EditText(ConnectionHistory.this);


//            usernameInput.setText(settings.getString("NickName", "").toString());
            usernameInput.setFocusable(true);
            usernameInput.setClickable(true);
            usernameInput.setFocusableInTouchMode(true);
            usernameInput.setSelectAllOnFocus(true);
            usernameInput.setSingleLine(true);
            usernameInput.setText(inputUsername);
            ipInput.setText(inputIp);
            final LinearLayout ll2 = new LinearLayout(getApplicationContext());
            TextView txtView2 = new TextView(getApplicationContext());
            txtView2.setText("Computer's Name");
            usernameInput.setMinEms(10);
            ll2.addView(txtView2);
            ll2.addView(usernameInput);
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
                    new AlertDialog.Builder(ConnectionHistory.this)
                            .setTitle("Remote Controll with public IP")
                                    //.setMessage("Enter Public ip")

                            .setView(showLayout)

                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ip = ipInput.getText().toString();
                                    if (ip == null || ip.replace(" ", "").equals("") || !validate(ip)) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {

                                                Toast.makeText(getApplicationContext(), "Enter a real IP", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                        return;
                                    }
                                    String usernameEntered = usernameInput.getText().toString();
                                    if (usernameEntered == null || usernameEntered.replace(" ", "").equals("")) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {

                                                Toast.makeText(getApplicationContext(), "Enter a real Username", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                        return;
                                    }
                                    if (ip == null || ip.equals("")) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "Not Valid IP .. ", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        return;
                                    }

                                    ///////////////////////////////
//                                    connect(usernameInput.getText().toString());
                                    if (ipInput != null && !ipInput.getText().toString().replaceAll("", "").equals("") && usernameInput != null && !usernameInput.getText().toString().replaceAll("", "").equals("")) {

                                        if (!db.isStored(ipInput.getText().toString(), usernameInput.getText().toString())) {
                                            runOnUiThread(new Thread() {
                                                @Override
                                                public void run() {
                                                    db.deleteContactAndCreateAnotherOne(inputIp, inputUsername, ipInput.getText().toString(), usernameInput.getText().toString());
                                                }
                                            });

                                        } else {
                                            runOnUiThread(new Thread() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(ConnectionHistory.this, "is Already stored", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    } else {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ConnectionHistory.this, "Enter real values", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }})
                            .show();
                }
            });


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


    String ip = null;

    public void internetAlgorithm() {

        try {


            final EditText ipInput = new EditText(ConnectionHistory.this);
//            SharedPreferences settings = getSharedPreferences("RemoteControl", 0);
//            ipInput.setText(settings.getString("Global Ip", "").toString());
            ip = ipInput.getText().toString();
            LinearLayout ll = new LinearLayout(getApplicationContext());
            final LinearLayout showLayout = new LinearLayout(getApplicationContext());
            final CheckBox checkBox = new CheckBox(this);
            checkBox.setText("Save data.");
            checkBox.setChecked(true);

            showLayout.setOrientation(LinearLayout.VERTICAL);
            TextView txtView1 = new TextView(getApplicationContext());
            txtView1.setText("Enter Public ip");
            ll.addView(txtView1);
            ll.addView(ipInput);
            ipInput.setMinEms(10);

            final EditText usernameInput = new EditText(ConnectionHistory.this);


//            usernameInput.setText(settings.getString("NickName", "").toString());
            usernameInput.setFocusable(true);
            usernameInput.setClickable(true);
            usernameInput.setFocusableInTouchMode(true);
            usernameInput.setSelectAllOnFocus(true);
            usernameInput.setSingleLine(true);
            final LinearLayout ll2 = new LinearLayout(getApplicationContext());
            TextView txtView2 = new TextView(getApplicationContext());
            txtView2.setText("Computer's Name");
            usernameInput.setMinEms(10);
            ll2.addView(txtView2);
            ll2.addView(usernameInput);
            showLayout.addView(new View(getApplicationContext()), new ViewGroup.LayoutParams(100, 50));
            showLayout.addView(ll);
            showLayout.addView(new View(getApplicationContext()), new ViewGroup.LayoutParams(100, 200));
            showLayout.addView(ll2);
            showLayout.addView(checkBox);
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
                    new AlertDialog.Builder(ConnectionHistory.this)
                            .setTitle("Remote Controll with public IP")
                                    //.setMessage("Enter Public ip")

                            .setView(showLayout)

                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ip = ipInput.getText().toString();
                                    if (ip == null || ip.replace(" ", "").equals("") || !validate(ip)) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {

                                                Toast.makeText(getApplicationContext(), "Enter a real IP", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                        return;
                                    }
                                    String usernameEntered = usernameInput.getText().toString();
                                    if (usernameEntered == null || usernameEntered.replace(" ", "").equals("")) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {

                                                Toast.makeText(getApplicationContext(), "Enter a real Username", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                        return;
                                    }
//                                    if (ip != ip2) {
////                                        ddPreferences settings = getSharedPreferences("RemoteControl", 0);
////                                        SharedPreferences.Editor editor = settings.edit();
////                                        editor.putString("Global Ip", ipInput.getText().toString());
////
////
////                                        editor.putString("NickName", usernameInput.getText().toString());
////
////
////                                        editor.commit();
//                                        // deal with the editable
//                                    }
                                    if (ip == null || ip.equals("")) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "Not Valid IP .. ", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        return;
                                    }

                                    ///////////////////////////////
//                                    connect(usernameInput.getText().toString());
                                    if (ipInput != null && !ipInput.getText().toString().replaceAll("", "").equals("") && usernameInput != null && !usernameInput.getText().toString().replaceAll("", "").equals("")) {
                                        if (checkBox.isChecked()) {
                                            if (!db.isStored(ipInput.getText().toString(), usernameInput.getText().toString())) {
                                                db.insertContact(ipInput.getText().toString(), usernameInput.getText().toString());
                                                startActivity(new Intent(ConnectionHistory.this, ConnectionHistory.class));
                                            } else {
                                                runOnUiThread(new Thread() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(ConnectionHistory.this, "is Already stored", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        } else {
                                            connect(ipInput.getText().toString(), usernameInput.getText().toString());
                                        }
                                    } else {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ConnectionHistory.this, "Enter real values", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }

                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }

                    )
                            .

                                    show();
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


    public void connect(final String ip, final String username) {
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
                    MouseUIActivity.ps.println("GLOBAL_IP:" + username);
                    MainActivity.typeOfConntection = "GLOBAL_IP:" + username;
                    runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            Log.e(Boolean.toString(InternetConnection.returnSocket.isConnected()), "InternetConnection.returnSocket.isConnected()");
                            Intent intent = new Intent(ConnectionHistory.this, MouseUIActivity.class);
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

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }


//    public int pxToDp(int px) {
//        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
//        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
//        return dp;
//    }

}

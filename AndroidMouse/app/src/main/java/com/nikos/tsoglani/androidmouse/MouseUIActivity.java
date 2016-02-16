package com.nikos.tsoglani.androidmouse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.mingle.sweetpick.CustomDelegate;
import com.mingle.sweetpick.SweetSheet;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import io.codetail.animation.ViewAnimationUtils;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;
import yalantis.com.sidemenu.interfaces.Resourceble;
import yalantis.com.sidemenu.interfaces.ScreenShotable;
import yalantis.com.sidemenu.model.SlideMenuItem;
import yalantis.com.sidemenu.util.ViewAnimator;
import io.codetail.animation.SupportAnimator;

public class MouseUIActivity extends ActionBarActivity implements SensorEventListener, ViewAnimator.ViewAnimatorListener {//ActionBar.TabListener ,
    private SensorManager sensorManager;
    private TextView x_axis, y_axis, z_axis;
    private boolean collect = true;
    public static PrintWriter ps;
    public static DataInputStream bf;
    // private ActionBar bar;
    ///<used for images>
    private GoogleApiClient client;
    Bitmap bitmapimage;
    private AsyncTask<Void, Void, Void> asTask;
    int width = -1, height = -1;
    private static Object lock = new Object();
    ///</used for images>
    public static int sleepingTime = 700;
    private ArrayList<SlideMenuItem> list = new ArrayList<SlideMenuItem>();
    boolean isMenuOpen = false;
    private Menu menu;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ContentFragment contentFragment;
    private ViewAnimator viewAnimator;
    private int res = R.drawable.icn_1;
    private LinearLayout linearLayout;
    private int maxResolution;
    private static int qualityPad, qualityCam;
    public static final String is_zoom_radio_on_String = "isZoomRadioUsed";
    public static final String isReverseString = "isReverseScrollingUsed", show_computer_mouse_seperateString = "show_computer_mouse_seperate";
     static String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createKeyboardShortcuts();
        setContentView(com.nikos.tsoglani.androidmouse.R.layout.activity_mouse);
        //  bar = getSupportActionBar();
//        mKeyboard = null;
        type= getIntent().getStringExtra("Type");

        stopService(new Intent(this, InternetConnection.class));
        if (type != null) {
            try {
                Toast.makeText(MouseUIActivity.this,type, Toast.LENGTH_SHORT).show();
                if (type.equalsIgnoreCase("bluetooth")) {
                    ps.println("bluetooth");
                    receivedImageX = 200;
                    receivedImageY = 200;
                    sleepingTime = 1300;
                } else if (type.equals("Internet")) {
                    receivedImageX = 580;
                    receivedImageY = 580;
                    sleepingTime = 1000;
                } else if (type.equals("WLAN")) {
                    receivedImageX = 900;
                    receivedImageY = 900;
                    sleepingTime = 400;
                } else if (type.equals("Wear")) {
                    receivedImageX = 900;
                    receivedImageY = 900;
                    sleepingTime = 2000;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        new AsyncTask<Void, Void, Void>() {

            BufferedReader br;
            ProgressDialog pd;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                try {
                    pd = new ProgressDialog(MouseUIActivity.this);
                    pd.setTitle("If takes  too long restart computer jar file and run it again");
                    pd.show();
                    pd.setCancelable(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                checkAyscTask chk = new checkAyscTask(this);
// Thread keeping 1 minute time watch
                (new Thread(chk)).start();
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (InternetConnection.returnSocket == null) {
                    try {
                        closeAndCreateAgaiConnection();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        br = new BufferedReader(new InputStreamReader(InternetConnection.returnSocket.getInputStream()));
                        String str = br.readLine();
                        str = str.replace("works:maxResolution:", "");
                        final String str2 = str.split("@@")[0];
                        maxResolution = Integer.parseInt(str2);
                        qualityPad = Integer.parseInt(str.split("@@")[1].replace("qualityPad:", ""));
                        qualityCam = Integer.parseInt(str.split("@@")[2].replace("qualityCam:", ""));


                        int qualityPad2 = qualityCam, qualityCam2 = qualityCam;
                        qualityCam = getSavedInt(MouseUIActivity.this, "qualityCam", qualityCam2);

                        qualityPad = getSavedInt(MouseUIActivity.this, "qualityPad", qualityPad2);
                        if (!type.equals("Wear")){
                            qualityPad=80;
                        }


                        MouseUIActivity.ps.println("QualityCam:" + qualityCam);
                        MouseUIActivity.ps.println("QualityPad:" + qualityPad);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                try {
                    if (pd != null)
                        pd.dismiss();
                } catch (Exception e) {
                }
                 if (!type.equals("Wear"))
                receivedImageX = getSavedInt(MouseUIActivity.this, "Resolution", receivedImageX);
                if (receivedImageX > maxResolution) {
                    receivedImageX = maxResolution;
                }
                MouseUIActivity.ps.println("Resolution:" + receivedImageX);
                contentFragment = ContentFragment.newInstance(R.drawable.icn_1);
                if (previousTab == null) {
                    previousTab = "MousePad";
                }
                switchTabString(previousTab);
                drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawerLayout.setScrimColor(Color.TRANSPARENT);
                drawerLayout.closeDrawers();
                linearLayout = (LinearLayout) findViewById(R.id.left_drawer);
                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        drawerLayout.closeDrawers();
                    }
                });


                setActionBar();
                createMenuList();
                viewAnimator = new ViewAnimator<>(MouseUIActivity.this, list, contentFragment, drawerLayout, MouseUIActivity.this);


                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1500);
                            runOnUiThread(new Thread() {
                                @Override
                                public void run() {

                                    try {
                                        firstTime();
                                    } catch (Exception e) {

                                    }
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

                drawerToggle.syncState();


            }
        }.execute();


//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent_black_percent_10)));
    }

    class checkAyscTask implements Runnable {
        AsyncTask<Void, Void, Void> mAT;
        Context context;
        int timeMSeconds;

        public checkAyscTask(AsyncTask<Void, Void, Void> at, final int timeMSeconds) {
            mAT = at;
            this.timeMSeconds = timeMSeconds;
        }

        public checkAyscTask(AsyncTask<Void, Void, Void> at) {
            this(at, 6000);
        }

        @Override
        public void run() {
            mHandler.postDelayed(runnable, timeMSeconds);
            // After 6sec the task in run() of runnable will be done
        }

        android.os.Handler mHandler = new android.os.Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mAT.getStatus() == AsyncTask.Status.RUNNING || mAT.getStatus() == AsyncTask.Status.PENDING) {
                    mAT.cancel(true); //Cancel Async task or do the operation you want after 1 minute]
                    ps.println("NULL");
                    closeAll();
                    goToHomeScreen();

                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        closeAll();
        super.onDestroy();
        InternetConnection.returnSocket = null;
        ps = null;
        MouseUIActivity.bf = null;

if(type.equals("Wear")){
    Bitmap.Config conf = Bitmap.Config.ARGB_8888;
    Bitmap bmp = Bitmap.createBitmap(100, 100, conf);
        Asset asset = createAssetFromBitmap(bmp);
        PutDataMapRequest request = PutDataMapRequest.create("/close");
        DataMap map = request.getDataMap();
        map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
        map.putAsset("profileImage", asset);
    if(client!=null)
        Wearable.DataApi.putDataItem(client, request.asPutDataRequest());}

    }

    private void firstTime() {
        showHelp(previousTab, true);

//        //  tool.getChildAt(1);
//        ShowcaseConfig config = new ShowcaseConfig();
//        config.setDelay(500); // half second between each showcase view
//        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "More options first time");
//
//        sequence.setConfig(config);


//        sequence.start();
    }

    private void createMenuList() {
        SlideMenuItem menuItem0 = new SlideMenuItem(ContentFragment.CLOSE, R.drawable.icn_close);
        list.add(menuItem0);
        SlideMenuItem menuItem = new SlideMenuItem(ContentFragment.MousePad, R.drawable.icn_1);
        list.add(menuItem);
//        SlideMenuItem menuItem2 = new SlideMenuItem(ContentFragment.Keyboard, R.drawable.icn_2);
//        list.add(menuItem2);
        SlideMenuItem menuItem3 = new SlideMenuItem(ContentFragment.PowerControl, R.drawable.icn_3);
        list.add(menuItem3);
        SlideMenuItem menuItem4 = new SlideMenuItem(ContentFragment.SpyCam, R.drawable.icn_4);
        list.add(menuItem4);
        SlideMenuItem menuItem5 = new SlideMenuItem(ContentFragment.SpyMic, R.drawable.icn_5);
        list.add(menuItem5);
        SlideMenuItem menuItem6 = new SlideMenuItem(ContentFragment.BashCommandLine, R.drawable.icn_6);
        list.add(menuItem6);
        SlideMenuItem menuItem7 = new SlideMenuItem(ContentFragment.Send, R.drawable.icn_7);
        list.add(menuItem7);
        SlideMenuItem menuItem8 = new SlideMenuItem(ContentFragment.MotionSensor, R.drawable.icn_8);
        list.add(menuItem8);

        SlideMenuItem menuItem9 = new SlideMenuItem(ContentFragment.GamePad, R.drawable.joy_icon);
        list.add(menuItem9);
    }


    private Hashtable<Integer, String> hash = new Hashtable<Integer, String>();

    private void createKeyboardShortcuts() {
        hash.clear();
        hash.put(49, "1");
        hash.put(50, "2");
        hash.put(51, "3");
        hash.put(52, "4");
        hash.put(53, "5");
        hash.put(54, "6");
        hash.put(55, "7");
        hash.put(56, "8");
        hash.put(57, "9");
        hash.put(48, "0");

        hash.put(158, "=");
        hash.put(159, "-");
        hash.put(171, "BACKSPACE");
        hash.put(113, "q");
        hash.put(119, "w");
        hash.put(101, "e");
        hash.put(114, "r");
        hash.put(116, "t");
        hash.put(121, "y");
        hash.put(117, "u");
        hash.put(105, "i");
        hash.put(111, "o");
        hash.put(112, "p");
        hash.put(165, "{");
        hash.put(166, "}");

        hash.put(97, "a");
        hash.put(115, "s");
        hash.put(100, "d");
        hash.put(102, "f");
        hash.put(103, "g");
        hash.put(104, "h");
        hash.put(106, "j");
        hash.put(107, "k");
        hash.put(108, "l");
        hash.put(156, "UP");
        hash.put(64, "@");


        hash.put(-1, "CAPS");
        hash.put(122, "z");
        hash.put(120, "x");
        hash.put(99, "c");
        hash.put(118, "v");
        hash.put(98, "b");
        hash.put(110, "n");
        hash.put(109, "m");
        hash.put(154, "LEFT");
        hash.put(153, "RIGHT");


        hash.put(152, ",");
        hash.put(46, ".");
        hash.put(58, ":");
        hash.put(33, "!");
        hash.put(35, "UNDO");
        hash.put(63, "INSERT");
        hash.put(164, ";");
        hash.put(172, "ESC");
        hash.put(155, "DOWN");
        hash.put(174, "HOME");

        hash.put(163, "PRINT SCREEN");
        hash.put(173, "MIN.");
        hash.put(-5, "DELETE");
        hash.put(162, "TAB");
        hash.put(151, "VOL. --");
        hash.put(150, "VOL. ++");
        hash.put(161, "ENTER");
        hash.put(169, "\\");
        hash.put(170, "/");


        hash.put(32, "SPACE");
        hash.put(-4, "HIDE");

    }

    private void setActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextColor(getResources().getColor(R.color.WhiteSmoke));

        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */


                toolbar,  /* nav drawer icon to replace 'Up' caret */
                R.string.hello_world,  /* "open drawer" description */
                R.string.hello_world  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                linearLayout.removeAllViews();
                linearLayout.invalidate();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (slideOffset > 0.6 && linearLayout.getChildCount() == 0)
                    viewAnimator.showMenuContent();

            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                hideKeyboard();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

    }


    private Thread receiveThread;

    @Override
    public void onSensorChanged(SensorEvent event) {
//        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER){
//            return;
//        }

        if (collect) {
            collect = false;
            if (receiveThread != null && receiveThread.isAlive()) {
                return;
            }
            receiveThread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                        collect = true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            };
            receiveThread.start();
            int X = 0, Y = 1, Z = 2;
            x_axis = (TextView) findViewById(R.id.x_axis);
            y_axis = (TextView) findViewById(R.id.y_axis);
            z_axis = (TextView) findViewById(R.id.z_axis);
            //  float[] values = event.values;
            int[] val = new int[3];
            int maxPointTheory = 10;
            //int reallyPoints = 1;

            for (int i = 0; i < val.length; i++) {
//                if (i == Z) {
//                    val[i] = Math.abs((int) (reallyPoints * event.values[i]));
//                } else {
//                    val[i] = Math.abs((int) (maxPointTheory * event.values[i]));
//                }
//                if (val[i] > reallyPoints && i != Z) {
//                    val[i] = Math.abs(val[i] - (reallyPoints));
//                }
                val[i] = ((maxPointTheory * Math.round(2 * event.values[i])));
            }

            if (x_axis != null && y_axis != null && z_axis != null) {
                x_axis.setText(Integer.toString(val[0]));
                y_axis.setText(Integer.toString(val[1]));
                z_axis.setText(Integer.toString(val[2]));
                //Log.e(Float.toString(values[0]), Float.toString(values[1]));
                sendInformation();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors

    }

    public void rightClickFunction(View v) {
        ps.println("RIGHT_CLICK");
        ps.flush();
    }

    public void leftClickFunction(View v) {
        ps.println("LEFT_CLICK");
        ps.flush();
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();

        if (sensorManager != null)
            sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (drawerLayout != null)
            drawerLayout.closeDrawers();


    }


    protected void closeAll() {

        try {
            if (mf != null) {
                mf.setIsPlaying(false);
            }
            if (p3f != null) {
                p3f.onChangeView();
                p3f = null;
            }
            if (ps != null) {
                ps.println("Null");
                ps.flush();
                ps.close();
                ps = null;
            }
        } catch (Exception e) {
        }
        if (bf != null) {
            try {

                bf.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            bf = null;

        }

        if (InternetConnection.returnSocket != null) {
            try {
                InternetConnection.returnSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            InternetConnection.returnSocket = null;
        }

        System.gc();
    }

    @Override
    public void onBackPressed() {
        if (isMenuOpen) {
            drawerLayout.closeDrawers();
            isMenuOpen = false;
            return;
        }

        ViewGroup configure_layout = (ViewGroup) findViewById(R.id.configure_RelativeLayout_in_joystic);
        if (configure_layout != null && configure_layout.getVisibility() == View.VISIBLE) {
            configure_layout.setVisibility(View.INVISIBLE);
//            ViewGroup configure_RelativeLayout_in_joystic = (ViewGroup) findViewById(R.id.configure_RelativeLayout_in_joystic);
//            configure_RelativeLayout_in_joystic.setVisibility(View.INVISIBLE);

            return;
        }

        if (mSweetSheet != null && mSweetSheet.isShow()) {
            mSweetSheet.dismiss();
            return;
        }
        if (hideKeyboard()) {
            return;
        }
        if (mSweetSheetKeyboard != null && mSweetSheetKeyboard.isShow()) {
            mSweetSheetKeyboard.dismiss();
            return;
        }


        if (!previousTab.equalsIgnoreCase("MousePad")) {
            View rl = findViewById(R.id.zoom_onoff);
            rl.setVisibility(View.VISIBLE);
            View mouse_buttons_visibility = findViewById(R.id.mouse_buttons_visibility);
            mouse_buttons_visibility.setVisibility(View.VISIBLE);
            View bar_visibility_show = findViewById(R.id.bar_visibility_show);
            bar_visibility_show.setVisibility(View.VISIBLE);
            final ProgressDialog progress = ProgressDialog.show(MouseUIActivity.this, "Please wait ...", "", true);
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected void onPreExecute() {
                    isReceivingImages = false;
                    progress.setCancelable(false);
                    progress.show();
                    super.onPreExecute();
                }

                @Override
                protected Void doInBackground(Void... params) {
                    if (previousTab.equalsIgnoreCase("Send") || previousTab.equalsIgnoreCase("Spy Camera")) {

                        closeAndCreateAgaiConnection();
                        Toast.makeText(getApplicationContext(), previousTab, Toast.LENGTH_LONG).show();
                    } else if (mf != null) {
                        closeAndCreateAgaiConnection();
                        mf.setIsPlaying(false);
                        mf = null;
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    try {
                        runOnUiThread(new Thread() {
                            @Override
                            public void run() {

                                menu.findItem(R.id.help).setVisible(true);
                                menu.findItem(R.id.settings).setVisible(true);
                                menu.findItem(R.id.configure).setVisible(false);
//                                menu.findItem(R.id.keyboard).setVisible(true);
                                final TextView keyboard = (TextView) findViewById(R.id.keyboard);
                                keyboard.setVisibility(View.VISIBLE);

                            }
                        });
                        isReceivingImages = true;
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_frame, new PageOneFragment())
                                .commit();
                        startReceivingImages(MouseUIActivity.this, true);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progress.dismiss();

                }
            }.execute();

            previousTab = "MousePad";
            Toast.makeText(getApplicationContext(), "Press back to Exit", Toast.LENGTH_SHORT).show();
            return;
        }

        isReceivingImages = false;
        super.onBackPressed();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        closeAll();
        goToHomeScreen();
    }

    private void goToHomeScreen() {

        finish();
        Intent intent = new Intent(MouseUIActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    private void sendInformation() {
        if (ps == null) {
            return;
        }

        ps.println("Motion:x:" + x_axis.getText().toString() + "@@" + "y:" + y_axis.getText().toString() + "@@" + "z:" + z_axis.getText().toString());


    }

    //private Thread thread;


    private int receivedImageX = 1000, receivedImageY = 1000;
    private boolean isRunning = false;
    private int sleepintTimeForScrenShot = 700;

    public void startReceivingImages(final Activity activity, final boolean getComputerScreen) throws RuntimeException {

        bitmapimage = null;
        if (ps == null || bf == null) {
            return;
        }

        final String sendCommand;
        if (getComputerScreen) {
            sendCommand = "SCREENSHOT";
        } else {
            sendCommand = "CAM_SCREENSHOT";
        }

        if (isRunning) {
            new Thread() {
                public void run() {
                    try {
                        isReceivingImages = false;
                        if (sendCommand.equalsIgnoreCase("CAM_SCREENSHOT")) {
                            Thread.sleep(sleepintTimeForScrenShot + 1);
                        } else if (sendCommand.equalsIgnoreCase("SCREENSHOT")) {
                            Thread.sleep(sleepingTime + 1);
                        }
                        isReceivingImages = true;
                        startReceivingImages(activity, getComputerScreen);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        if (width == -1)
            width = getWindowManager().getDefaultDisplay().getWidth();
        if (height == -1) {
            height = getWindowManager().getDefaultDisplay().getHeight();
        }
        asTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                isRunning = true;
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Void... params) {

                while (isReceivingImages) {
                    try {
                        if (sendCommand.equalsIgnoreCase("CAM_SCREENSHOT")) {
                            Thread.sleep(sleepintTimeForScrenShot);
                        } else if (sendCommand.equalsIgnoreCase("SCREENSHOT")) {
                            Thread.sleep(sleepingTime);
                        }

//
                        if (!isReceivingImages) {
                            break;
                        }
                        synchronized (lock) {
                            if (ps == null) {
                                Intent intent = new Intent(MouseUIActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                return null;
                            }


                            ps.println(sendCommand);
                            ps.flush();

                            int bytesRead = 0;

                            byte[] pic = new byte[receivedImageX * receivedImageX];
                            try {

                                bytesRead = bf.read(pic, 0, pic.length);
                                try {


                                    boolean is_show_computer_mouse_seperate = MouseUIActivity.getSavedBoolean(MouseUIActivity.this, show_computer_mouse_seperateString, true);
                                    bitmapimage = BitmapFactory.decodeByteArray(pic, 0, bytesRead);

                                    if ((!PageOneFragment.waitUntilDraw) && getComputerScreen) {//


                                        if (bitmapimage != null) {
                                            bitmapimage.prepareToDraw();
                                            // final Drawable draw = new BitmapDrawable(activity.getResources(), bitmapimage);
                                            final ImageView iv = (ImageView) activity.findViewById(R.id.mousepad_screen);
                                            activity.runOnUiThread(new Thread() {
                                                @Override
                                                public void run() {

                                                    try {
                                                        bitmapimage.prepareToDraw();

                                                        if (type.equalsIgnoreCase("Wear")) {
//                                                                Asset asset = createAssetFromBitmap(bitmapimage);
//                                                                PutDataRequest request = PutDataRequest.create("/image");
//                                                                request.putAsset("profileImage", asset);
//                                                                Wearable.DataApi.putDataItem(mGoogleApiClient, request);
                                                            if (client == null) {
                                                                createGoogleApiConnection();
                                                            }
                                                            if (client != null) {

//                                                                    PutDataMapRequest request = PutDataMapRequest.create("/image");
//                                                                    Asset asset = createAssetFromBitmap(bitmap);
//                                                                    request.putAsset("profileImage", asset);
//                                                                    DataMap dataMap = request.getDataMap();
//                                                                    dataMap.putLong("timestamp", System.currentTimeMillis());
//                                                                    PutDataRequest dataRequest = request.asPutDataRequest();
//                                                                    Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest);
//




                                                                Asset asset = createAssetFromBitmap(bitmapimage);
                                                                PutDataMapRequest request = PutDataMapRequest.create("/image");
                                                                DataMap map = request.getDataMap();
                                                                map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
                                                                map.putAsset("profileImage", asset);
                                                                Wearable.DataApi.putDataItem(client, request.asPutDataRequest());


                                                            }
                                                        }

                                                        if (iv != null) {
                                                            iv.setImageBitmap(bitmapimage);


                                                        } else {
                                                            try {
                                                                getSupportFragmentManager().beginTransaction()
                                                                        .replace(R.id.content_frame, new PageOneFragment())
                                                                        .commit();
                                                            } catch (Exception e) {

                                                            }
                                                        }
                                                    } catch (final Exception e) {

                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }
                                    } else if (!getComputerScreen) {
                                        final ImageView iv = (ImageView) activity.findViewById(R.id.imageView);

                                        activity.runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {
                                                if (bitmapimage != null && iv != null) {
                                                    bitmapimage.prepareToDraw();
                                                    iv.setImageBitmap(bitmapimage);
                                                }
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();

                                    throw new NullPointerException();
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                runOnUiThread(new Thread() {
                                    @Override
                                    public void run() {
                                        new AsyncTask<Void, Void, Void>() {
                                            ProgressDialog pd;

                                            @Override
                                            protected void onPreExecute() {
                                                super.onPreExecute();
                                                pd = new ProgressDialog(MouseUIActivity.this);
                                                pd.setTitle("Loading .. ");
                                                pd.setCancelable(true);
                                            }

                                            @Override
                                            protected Void doInBackground(Void... params) {
                                                closeAndCreateAgaiConnection();
                                                switchTabString(previousTab);
                                                return null;
                                            }


                                            @Override
                                            protected void onPostExecute(Void aVoid) {
                                                super.onPostExecute(aVoid);
                                                try {
                                                    pd.dismiss();
                                                } catch (Exception e) {

                                                }
                                            }
                                        }.execute();
                                    }
                                });

                                continue;
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                isReceivingImages = false;
                                closeAll();
                                goToHomeScreen();
                            } catch (Exception e) {
                                e.printStackTrace();

                            } catch (OutOfMemoryError error) {
                                error.printStackTrace();
                                System.gc();

                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //  System.gc();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                isRunning = false;
                if (!getComputerScreen) {
                    if (ps != null)
                        ps.println("STOP_CAM");
                }

                if (ps != null)
                    ps.flush();

            }
        };
        asTask.execute();
//        thread = new Thread() {
//            @Override
//            public void run() {
//
//            }
//        };
//        thread.start();
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    public void createGoogleApiConnection() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {

                        Toast.makeText(MouseUIActivity.this, "Connection Faild", Toast.LENGTH_SHORT).show();
                        client = null;
                    }
                })
                .addApi(Wearable.API)
                .build();

        client.connect();

    }


    private void startAgainActivity() {

        Intent intent = new Intent(MouseUIActivity.this, MouseUIActivity.class);
        intent.putExtra("Type", type);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    static boolean isReceivingImages = true;
    private PageThreeFragment p3f = null;
    MicFragment mf;
    static boolean isShowing = true;

    void closeAndCreateAgaiConnection() {
        isShowing = true;
        closeAll();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Here you should write your time consuming task...
                    // Let the progress ring for 10 seconds...
                    Thread.sleep(2100);

                } catch (Exception e) {

                }
                isShowing = false;
            }
        }).start();


        while (isShowing) {
            continue;
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                createConnection();
            }


        }.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
    }

    public static void createConnection() {

        try {

            InternetConnection.returnSocket = new Socket(InternetConnection.lastIP, InternetConnection.port);
            MouseUIActivity.ps = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(InternetConnection.returnSocket.getOutputStream())),
                    true);
            ps.println(MainActivity.typeOfConntection);
            MouseUIActivity.bf = new DataInputStream(InternetConnection.returnSocket.getInputStream());
            ps.println("STOP_AUDIO_RECORDING");
        } catch (IOException e) {
            try {
                Thread.sleep(1000);
                createConnection();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }


//    @Override
//    public void onTabSelected(final ActionBar.Tab tab, FragmentTransaction ft) {
//        final ProgressDialog progress = ProgressDialog.show(MouseUIActivity.this, "Please wait ...", "", true);
//        progress.setCancelable(false);
//
//
//   // Toast.makeText(getApplicationContext(),"Please Wait ..",Toast.LENGTH_SHORT).show();
//new Thread(){
//    @Override
//    public void run() {
//        if (mf != null) {
//            closeAndCreateAgaiConnection();
//
//            mf.isPlaying = false;
//            mf = null;
//        }
//
//        if(previousTab!=null&&(previousTab.equalsIgnoreCase("MousePad")&&tab.getText().toString().equalsIgnoreCase("Spy Microphone"))){
//            closeAndCreateAgaiConnection();
//        }
//     runOnUiThread(new Thread(){
//         @Override
//         public void run() {
//             previousTab=tab.getText().toString();
//
//
//
//
//
//             FrameLayout fragContainer = (FrameLayout) findViewById(com.nikos.tsoglani.androidmouse.R.id.app);
//             fragContainer.removeAllViews();
//             if (p3f != null) {
//                 p3f.onChangeView();
//                 p3f = null;
//             }
//
//             if (sensorManager != null) {
//                 sensorManager.unregisterListener(MouseUIActivity.this);
//             }
//             if (tab.getText().toString().equalsIgnoreCase("MousePad")) {
//                 try {
////                     isReceivingImages = true;
////                     FrameLayout ll = new FrameLayout(MouseUIActivity.this);
////                     ll.setId(12345);
////                     getFragmentManager().beginTransaction().add(ll.getId(), new PageOneFragment(), "Mousepad").commit();
////                     fragContainer.addView(ll);
////                     startReceivingImages(MouseUIActivity.this, true);
////
//
//                 } catch (Exception e) {
//                     e.printStackTrace();
//                 }
//
//             } else if (tab.getText().toString().equalsIgnoreCase("Spy Microphone")) {
//                 isReceivingImages = false;
//                 FrameLayout ll = new FrameLayout(MouseUIActivity.this);
//                 ll.setId(12345);
//                 if (mf == null) {
//                     mf = new MicFragment();
//                     mf.start();
//                 }
//                 getFragmentManager().beginTransaction().add(ll.getId(), mf, "Mic").commit();
//                 fragContainer.addView(ll);
//
//             } else if (tab.getText().toString().equalsIgnoreCase("Keyboard")) {
//                 isReceivingImages = false;
//                 FrameLayout ll = new FrameLayout(MouseUIActivity.this);
//                 ll.setId(12345);
//                 p3f = new PageThreeFragment();
//                 getFragmentManager().beginTransaction().add(ll.getId(), p3f, "Keyboard").commit();
//                 fragContainer.addView(ll);
//
//             } else if (tab.getText().toString().equalsIgnoreCase("Bash Command Line")) {
//                 isReceivingImages = false;
//                 FrameLayout ll = new FrameLayout(MouseUIActivity.this);
//                 ll.setId(12345);
//                 getFragmentManager().beginTransaction().add(ll.getId(), new CMDFragment(), "Bash Command Line").commit();
//                 fragContainer.addView(ll);
//             } else if (tab.getText().toString().equalsIgnoreCase("Power Controls")) {
//                 isReceivingImages = false;
//                 FrameLayout ll = new FrameLayout(MouseUIActivity.this);
//                 ll.setId(12345);
//                 getFragmentManager().beginTransaction().add(ll.getId(), new PageFourFragment(), "Power Controls").commit();
//                 fragContainer.addView(ll);
//             }else if(tab.getText().toString().equalsIgnoreCase("Send")){
//                 isReceivingImages = false;
//                 FrameLayout ll = new FrameLayout(MouseUIActivity.this);
//                 ll.setId(12345);
//                 p3f = new PageThreeFragment();
//                 getFragmentManager().beginTransaction().add(ll.getId(), new SendFragment(), "Send").commit();
//                 fragContainer.addView(ll);
//             } else if (tab.getText().toString().equalsIgnoreCase("Spy Camera")) {
//                 isReceivingImages = false;
//                 FrameLayout ll = new FrameLayout(MouseUIActivity.this);
//                 ll.setId(12345);
//                 getFragmentManager().beginTransaction().add(ll.getId(), new WebCameraFragment(), "Spy Camera").commit();
//                 fragContainer.addView(ll);
//                 final ProgressDialog ringProgressDialog = ProgressDialog.show(MouseUIActivity.this, "Please wait ...", "Be sure that the computer has Camera ...", true);
//                 ringProgressDialog.setCancelable(true);
//                 ps.println("START_CAMERA");
//                 new Thread(new Runnable() {
//                     @Override
//                     public void run() {
//                         try {
//                             Thread.sleep(650);
//
//                             runOnUiThread(new Thread() {
//                                 @Override
//                                 public void run() {
//                                     isReceivingImages = true;
//                                     startReceivingImages(MouseUIActivity.this, false);
//                                 }
//                             });
//                             // Here you should write your time consuming task...
//                             // Let the progress ring for 10 seconds...
//                             Thread.sleep(3000);
//
//                         } catch (Exception e) {
//
//                         }
//                         ringProgressDialog.dismiss();
//                     }
//                 }).start();
//
//             } else {
//                 isReceivingImages = false;
//                 FrameLayout ll = new FrameLayout(MouseUIActivity.this);
//                 ll.setId(123456);
//                 getFragmentManager().beginTransaction().add(ll.getId(), new PaneTwoFragment(), "Motion sensor").commit();
//                 fragContainer.addView(ll);
//                 sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//                 sensorManager.registerListener(MouseUIActivity.this,
//                         sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
//                         SensorManager.SENSOR_DELAY_NORMAL);
//
//             }
//             progress.dismiss();
//
//         }
//     });
//    }
//}.start();
//
//
//    }

    //    @Override
//    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
//
//    }
//
//    @Override
//    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    //  }
    boolean isKeyboardOnView = false;

    public void keyboardFunction(View v) {
        Button b = (Button) v;
        if (b.getText().toString().equalsIgnoreCase("Hide")) {
            if (mSweetSheetKeyboard != null && mSweetSheetKeyboard.isShow())
                mSweetSheetKeyboard.dismiss();
            isKeyboardOnView = true;
            return;
        }
        final EditText txt = (EditText) findViewById(R.id.textScreen);

        if (b.getText().toString().equalsIgnoreCase("send")) {

            ps.println("keyboard Word:" + txt.getText().toString());
            ps.flush();
            hideKeyboard(txt);
            txt.setText("   Enter Text   ");
            txt.setTextColor(getResources().getColor(R.color.transparent_black_percent_30));

        } else {
            ps.println("keyboard:" + toUperCase(b.getText().toString()));
            ps.flush();
        }
    }


    private void hideKeyboard(EditText editText) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

    private String toUperCase(String low) {
        String out = "";
        for (int i = 0; i < low.length(); i++) {
            out += Character.toUpperCase(low.charAt(i));
        }
        return out;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerToggle != null)
            drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        setMenuOnRotate(previousTab);

        return true;
    }


    private void setMenuOnRotate(String title) {
        if (title == null) {
            return;
        }
        menu.findItem(R.id.settings).setVisible(false);
        menu.findItem(R.id.help).setVisible(true);
//        menu.findItem(R.id.keyboard).setVisible(false);
        menu.findItem(R.id.configure).setVisible(false);
        if (title.equals("MousePad")) {
            menu.findItem(R.id.settings).setVisible(true);
//            menu.findItem(R.id.keyboard).setVisible(true);
        } else if (title.equals("GamePad")) {
            menu.findItem(R.id.configure).setVisible(true);
            menu.findItem(R.id.help).setVisible(false);
        } else if (title.equals("Spy Microphone")) {
            menu.findItem(R.id.help).setVisible(false);
        } else if (title.equals("Keyboard")) {


        } else if (title.equals("Bash Command Line")) {

        } else if (title.equals("Power Controls")) {

        } else if (title.equals("Send")) {


        } else if (title.equals("Spy Camera")) {
            menu.findItem(R.id.help).setVisible(false);
            menu.findItem(R.id.settings).setVisible(true);


        } else if (title.equals("Motion Sensor")) {


        }

    }


    private String previousTab = null;
    private ProgressDialog progress;

    private ScreenShotable replaceFragment(ScreenShotable screenShotable, int topPosition, final String title) {
//        this.res = this.res == R.drawable.content_music ? R.drawable.content_films : R.drawable.content_music;
        View view = findViewById(R.id.content_frame);
        int finalRadius = Math.max(view.getWidth(), view.getHeight());
        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(view, 0, topPosition, 0, finalRadius);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(ViewAnimator.CIRCULAR_REVEAL_ANIMATION_DURATION);


        findViewById(R.id.content_overlay).setBackgroundDrawable(new BitmapDrawable(getResources(), screenShotable.getBitmap()));
        animator.start();
        contentFragment = ContentFragment.newInstance(this.res);

        if (previousTab != null && previousTab.equalsIgnoreCase(title)) {
            return contentFragment;
        }
        progress = ProgressDialog.show(MouseUIActivity.this, "Please wait ...", "", true);
        progress.setCancelable(false);


        // Toast.makeText(getApplicationContext(),"Please Wait ..",Toast.LENGTH_SHORT).show();
        new Thread() {
            @Override
            public void run() {
                if (mf != null) {
                    closeAndCreateAgaiConnection();

                    mf.setIsPlaying(false);
                    mf = null;
                }

                if (previousTab != null && ((previousTab.equalsIgnoreCase("MousePad") && title.equalsIgnoreCase("Spy Microphone")))) {
                    closeAndCreateAgaiConnection();
                }

//                if ((previousTab != null && title.equalsIgnoreCase("MousePad") && previousTab.equalsIgnoreCase("Spy Camera"))) {
//                    runOnUiThread(new Thread() {
//                        @Override
//                        public void run() {
//                            goToMousepad();
//                        }
//                    });
//                    progress.dismiss();
//                    return;
//                }

                runOnUiThread(new Thread() {
                    @Override
                    public void run() {

                        menu.findItem(R.id.settings).setVisible(false);
                        menu.findItem(R.id.help).setVisible(true);
//                        menu.findItem(R.id.keyboard).setVisible(false);
                        menu.findItem(R.id.configure).setVisible(false);
                        previousTab = title;


                        //       FrameLayout fragContainer = (FrameLayout) findViewById(com.nikos.tsoglani.androidmouse.R.id.app);
                        //      fragContainer.removeAllViews();
                        if (p3f != null) {
                            p3f.onChangeView();
                            p3f = null;
                        }

                        if (sensorManager != null) {
                            sensorManager.unregisterListener(MouseUIActivity.this);
                        }


                        getSupportActionBar().setTitle("");
                        //  getSupportActionBar().
                        switchTabString(title);
                        progress.dismiss();

                    }
                });
            }
        }.start();


        //    Toast.makeText(this, Integer.toString(topPosition) + "   " + Integer.toString(res), Toast.LENGTH_LONG).show();
        return contentFragment;
    }

    private void goToMousepad() {


        final ProgressDialog progress = ProgressDialog.show(MouseUIActivity.this, "Loading Mousepad ...", "", true);
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                isReceivingImages = false;
                progress.show();

                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (previousTab.equalsIgnoreCase("Send") || previousTab.equalsIgnoreCase("Spy Camera")) {

                    closeAndCreateAgaiConnection();
                    Toast.makeText(getApplicationContext(), previousTab, Toast.LENGTH_LONG).show();
                } else if (mf != null) {
                    closeAndCreateAgaiConnection();
                    mf.setIsPlaying(false);
                    mf = null;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    progress.dismiss();

                    menu.findItem(R.id.help).setVisible(true);
                    menu.findItem(R.id.settings).setVisible(true);
                    menu.findItem(R.id.configure).setVisible(false);
//                                menu.findItem(R.id.keyboard).setVisible(true);
                    final TextView keyboard = (TextView) findViewById(R.id.keyboard);
                    keyboard.setVisibility(View.VISIBLE);


                    isReceivingImages = true;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, new PageOneFragment())
                            .commit();
                    startReceivingImages(MouseUIActivity.this, true);


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    progress.dismiss();
                }


            }
        }.execute();

        previousTab = "MousePad";

    }

    private void switchTabString(String title) {
//        Toast.makeText(MouseUIActivity.this, title, Toast.LENGTH_SHORT).show();

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);


        final TextView keyboard = (TextView) findViewById(R.id.keyboard);
        View rl = findViewById(R.id.zoom_onoff);
        View bar_visibility_hide = findViewById(R.id.bar_visibility_show);
        rl.setVisibility(View.GONE);
        bar_visibility_hide.setVisibility(View.INVISIBLE);
        keyboard.setVisibility(View.INVISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        View mouse_buttons_visibility = findViewById(R.id.mouse_buttons_visibility);
        mouse_buttons_visibility.setVisibility(View.INVISIBLE);

        android.support.v7.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar.getVisibility() != View.VISIBLE) {
            toolbar.setVisibility(View.VISIBLE);
        }
        if (title.equals("MousePad")) {
            try {
                rl.setVisibility(View.VISIBLE);
                mouse_buttons_visibility.setVisibility(View.VISIBLE);
                bar_visibility_hide.setVisibility(View.VISIBLE);
                runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        keyboard.setVisibility(View.VISIBLE);
                        if (menu != null) {
//                            menu.findItem(R.id.keyboard).setVisible(true);
                            menu.findItem(R.id.settings).setVisible(true);
                        }
                    }
                });
//                PageOneFragment.zoomValue = getSavedInt(this, "zoomValue", 0);
                isReceivingImages = true;
                startReceivingImages(MouseUIActivity.this, true);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new PageOneFragment())
                        .commit();


            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (title.equals("GamePad")) {
            try {
                isReceivingImages = false;
                runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        menu.findItem(R.id.help).setVisible(false);
                        menu.findItem(R.id.configure).setVisible(true);
                    }
                });
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new JoystickFragments())
                        .commit();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (title.equals("Spy Microphone")) {
            isReceivingImages = false;
            runOnUiThread(new Thread() {
                @Override
                public void run() {
                    if (menu != null)
                        menu.findItem(R.id.help).setVisible(false);
                }
            });
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new MicFragment())
                    .commit();
            if (mf == null) {
                mf = new MicFragment();
                mf.start();
            }
        } else if (title.equals("Keyboard")) {
            isReceivingImages = false;

            p3f = new PageThreeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, p3f)
                    .commit();

        } else if (title.equals("Bash Command Line")) {
            isReceivingImages = false;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new CMDFragment())
                    .commit();
        } else if (title.equals("Power Controls")) {
            isReceivingImages = false;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new PageFourFragment())
                    .commit();
        } else if (title.equals("Send")) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                        progress = null;
                    }
                    progress = ProgressDialog.show(MouseUIActivity.this, "Please wait ...", "", true);
                    progress.setCancelable(false);
                }

                @Override
                protected Void doInBackground(Void... params) {
                    closeAndCreateAgaiConnection();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    progress.dismiss();
                }
            }.execute();


            isReceivingImages = false;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new SendFragment())
                    .commit();


        } else if (title.equals("Spy Camera")) {

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    isReceivingImages = false;
                    super.onPreExecute();
                }

                @Override
                protected Void doInBackground(Void... params) {


                    runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            if (menu != null)
                                menu.findItem(R.id.help).setVisible(false);
                        }
                    });
                    runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            if (menu != null)
                                menu.findItem(R.id.settings).setVisible(true);
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    try {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_frame, new WebCameraFragment())
                                .commit();
                        final ProgressDialog ringProgressDialog = ProgressDialog.show(MouseUIActivity.this, "Please wait ...", "Be sure that the computer has Camera ...", true);
                        ringProgressDialog.setCancelable(true);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(650);
                                    if (ps != null)
                                        ps.println("START_CAMERA");
                                    Thread.sleep(1000);

                                    runOnUiThread(new Thread() {
                                        @Override
                                        public void run() {
                                            isReceivingImages = true;
                                            startReceivingImages(MouseUIActivity.this, false);
                                        }
                                    });
                                    // Here you should write your time consuming task...
                                    // Let the progress ring for 10 seconds...
                                    Thread.sleep(2000);

                                } catch (Exception e) {

                                }
                                ringProgressDialog.dismiss();
                            }
                        }).start();
                    } catch (IllegalStateException ilegal) {
                        ilegal.printStackTrace();
                    }
                }
            }

                    .

                            execute();


        } else if (title.equals("Motion Sensor")) {
            isReceivingImages = false;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new PaneTwoFragment())
                    .commit();
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensorManager.registerListener(MouseUIActivity.this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                    SensorManager.SENSOR_DELAY_NORMAL);

        }


    }

    @Override
    public ScreenShotable onSwitch(Resourceble slideMenuItem, ScreenShotable screenShotable, int position) {
        switch (slideMenuItem.getName()) {
            case ContentFragment.CLOSE:
                return screenShotable;
            default:
                return replaceFragment(screenShotable, position, slideMenuItem.getName());
        }
    }

    @Override
    public void disableHomeButton() {
        getSupportActionBar().setHomeButtonEnabled(false);
        isMenuOpen = true;

    }

    @Override
    public void enableHomeButton() {
        getSupportActionBar().setHomeButtonEnabled(true);
        isMenuOpen = false;
        drawerLayout.closeDrawers();

    }

    @Override
    public void addViewToContainer(View view) {
        linearLayout.addView(view);
    }

    private SweetSheet mSweetSheet;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        try {
            switch (item.getItemId()) {
                case R.id.settings:
                    createOptions(previousTab);
                    break;
                case R.id.help:
                    generateHelp(previousTab);
                    break;
                case R.id.keyboard:
                    generateKeyboard();
                    break;
                case R.id.configure:
                    generateConfigure();
                    break;
//            case R.id.home:
//          Toast.makeText(getApplicationContext(),"closing",Toast.LENGTH_LONG).show();
//                closeAll();
//                break;

                default:
                    return super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    private SweetSheet mSweetSheetKeyboard = null;

    private void generateConfigure() {
//        if (mSweetSheet != null && mSweetSheet.isShow()) {
//            mSweetSheet.dismiss();
//
//        }
//
//        if (mSweetSheet == null) {
//            RelativeLayout fragContainer = (RelativeLayout) findViewById(com.nikos.tsoglani.androidmouse.R.id.app);
//            mSweetSheet = new SweetSheet(fragContainer);
////定义一个 CustomDelegate 的 Delegate ,并且设置它的出现动画.
//            CustomDelegate customDelegate = new CustomDelegate(true,
//                    CustomDelegate.AnimationType.DuangAnimation);
//            View view = LayoutInflater.from(this).inflate(R.layout.configure_layout, null, false);
////设置自定义视图.
//            customDelegate.setCustomView(view);
////设置代理类
//            mSweetSheet.setDelegate(customDelegate);
//
//
//        }
//        mSweetSheet.show();

        ViewGroup configure_layout = (ViewGroup) findViewById(R.id.configure_RelativeLayout_in_joystic);
        configure_layout.setVisibility(View.VISIBLE);
//        ViewGroup configure_RelativeLayout_in_joystic = (ViewGroup) findViewById(R.id.configure_RelativeLayout_in_joystic);
//        configure_RelativeLayout_in_joystic.setVisibility(View.VISIBLE);

        Spinner upSpinner = (Spinner) configure_layout.findViewById(R.id.up_spinner);
        Spinner downSpinner = (Spinner) configure_layout.findViewById(R.id.down_spinner);
        Spinner rightSpinner = (Spinner) configure_layout.findViewById(R.id.right_spinner);
        Spinner leftSpinner = (Spinner) configure_layout.findViewById(R.id.left_spinner);
        Spinner redSpinner = (Spinner) configure_layout.findViewById(R.id.red_button_spinner);
        Spinner greenSpinner = (Spinner) configure_layout.findViewById(R.id.green_button_spinner);
        Spinner blueSpinner = (Spinner) configure_layout.findViewById(R.id.blue_button_spinner);
        Spinner yellowSpinner = (Spinner) configure_layout.findViewById(R.id.yellow_button_spinner);
        Spinner pauseSpinner = (Spinner) configure_layout.findViewById(R.id.pause_spinner);

        String greenButtonString = getData("green", "Y");
        String redButtonString = getData("red", "X");
        String yellowButtonString = getData("yellow", "O"), blueButtonString = getData("blue", "Z");

        pauseSpinner.setSelection(getIndex(pauseSpinner, getData("pause", "ESC")));
        upSpinner.setSelection(getIndex(upSpinner, getData("up", "UP")));
        downSpinner.setSelection(getIndex(downSpinner, getData("down", "DOWN")));
        rightSpinner.setSelection(getIndex(rightSpinner, getData("right", "RIGHT")));
        leftSpinner.setSelection(getIndex(leftSpinner, getData("left", "LEFT")));
        redSpinner.setSelection(getIndex(redSpinner, redButtonString));
        greenSpinner.setSelection(getIndex(greenSpinner, greenButtonString));
        blueSpinner.setSelection(getIndex(blueSpinner, blueButtonString));
        yellowSpinner.setSelection(getIndex(yellowSpinner, yellowButtonString));
    }

    //
//    public void gameButtonFunction(View v) {
//        Button b = (Button) v;
//        String text = b.getText().toString();
//        MouseUIActivity.ps.println("keyboard:" + text);
//        MouseUIActivity.ps.flush();
//    }
//
    public void pauseJoystickFunction(View v) {
        String pauseString = getData("pause", "ESC");
        MouseUIActivity.ps.println("keyboard:" + pauseString);
        MouseUIActivity.ps.flush();

    }

    private int getIndex(Spinner spinner, String myString) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void save_configue_button_function(View v) {
        ViewGroup configure_layout = (ViewGroup) findViewById(R.id.configure_layout_in_joystic);
        Spinner upSpinner = (Spinner) configure_layout.findViewById(R.id.up_spinner);
        Spinner downSpinner = (Spinner) configure_layout.findViewById(R.id.down_spinner);
        Spinner rightSpinner = (Spinner) configure_layout.findViewById(R.id.right_spinner);
        Spinner leftSpinner = (Spinner) configure_layout.findViewById(R.id.left_spinner);
        Spinner redSpinner = (Spinner) configure_layout.findViewById(R.id.red_button_spinner);
        Spinner greenSpinner = (Spinner) configure_layout.findViewById(R.id.green_button_spinner);
        Spinner blueSpinner = (Spinner) configure_layout.findViewById(R.id.blue_button_spinner);
        Spinner yellowSpinner = (Spinner) configure_layout.findViewById(R.id.yellow_button_spinner);
        Spinner pauseSpinner = (Spinner) configure_layout.findViewById(R.id.pause_spinner);

        saveValue("up", upSpinner.getSelectedItem().toString());
        saveValue("down", downSpinner.getSelectedItem().toString());
        saveValue("right", rightSpinner.getSelectedItem().toString());
        saveValue("left", leftSpinner.getSelectedItem().toString());

        saveValue("pause", pauseSpinner.getSelectedItem().toString());

        saveValue("red", redSpinner.getSelectedItem().toString());
        saveValue("green", greenSpinner.getSelectedItem().toString());
        saveValue("yellow", yellowSpinner.getSelectedItem().toString());
        saveValue("blue", blueSpinner.getSelectedItem().toString());

        updateColorButtonsText();

        hide_configue_button_function(v);
    }

    private void updateColorButtonsText() {
        Button greenButton = (Button) findViewById(R.id.game_button_green);
        Button redButton = (Button) findViewById(R.id.game_button_red);
        Button blueButton = (Button) findViewById(R.id.game_button_blue);
        Button yellowButton = (Button) findViewById(R.id.game_button_yellow);
        Button pauseButton = (Button) findViewById(R.id.pause_button);


        String greenButtonString = getData("green", "Y");
        String redButtonString = getData("red", "X");
        String pauseButtonString = getData("red", "X");
        String yellowButtonString = getData("yellow", "O"), blueButtonString = getData("blue", "Z");

        greenButton.setText(greenButtonString);
        redButton.setText(redButtonString);
        yellowButton.setText(yellowButtonString);
        blueButton.setText(blueButtonString);
    }

    protected static final String MY_PREFS_NAME = "Mouse_Shared_Preferenses";

    private void saveValue(String id, String data) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(id, data);
        editor.commit();
    }

    private void saveBoolean(String id, boolean data) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(id, data);
        editor.commit();
    }

    void saveInteger(String id, int data) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(id, data);
        editor.commit();
    }

    static Boolean getSavedInt(Activity act, String id) {
        return getSavedInt(act, id);
    }

    static Boolean getSavedBoolean(Activity act, String id) {
        return getSavedBoolean(act, id, false);
    }

    static int getSavedInt(Activity act, String id, int defaultInt) {
        SharedPreferences prefs = act.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        int restoredText = prefs.getInt(id, defaultInt);
        return restoredText;
    }

    static Boolean getSavedBoolean(Activity act, String id, boolean defaultBool) {
        SharedPreferences prefs = act.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        boolean restoredText = prefs.getBoolean(id, defaultBool);
        return restoredText;
    }

    private String getData(String id, String defaultValue) {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String restoredText = prefs.getString(id, defaultValue);
        return restoredText;
    }


    public void hide_configue_button_function(View v) {
//        ViewGroup configure_layout = (ViewGroup) findViewById(R.id.configure_layout_in_joystic);
        ViewGroup configure_RelativeLayout_in_joystic = (ViewGroup) findViewById(R.id.configure_RelativeLayout_in_joystic);
        configure_RelativeLayout_in_joystic.setVisibility(View.INVISIBLE);
//        configure_layout.setVisibility(View.INVISIBLE);
    }

    public void generateKeyboard() {
        openKeyboard();

//        if (mSweetSheetKeyboard != null && mSweetSheetKeyboard.isShow()) {
//            mSweetSheetKeyboard.dismiss();
//
//        }
//
//        if (mSweetSheetKeyboard == null) {
//            FrameLayout fragContainer = (FrameLayout) findViewById(com.nikos.tsoglani.androidmouse.R.id.app);
//            mSweetSheetKeyboard = new SweetSheet(fragContainer);
////定义一个 CustomDelegate 的 Delegate ,并且设置它的出现动画.
//            CustomDelegate customDelegate = new CustomDelegate(true,
//                    CustomDelegate.AnimationType.DuangLayoutAnimation);
//            View view = LayoutInflater.from(this).inflate(R.layout.keyboard, null, false);
////设置自定义视图.
//            customDelegate.setCustomView(view);
////设置代理类
//            mSweetSheetKeyboard.setDelegate(customDelegate);
//
//
//        }
//        mSweetSheetKeyboard.show();
//
//
//        final Spinner f_spinner = (Spinner) findViewById(R.id.f_key);
//        final EditText txt = (EditText) findViewById(R.id.textScreen);
//
//        txt.setTextColor(getResources().getColor(R.color.transparent_black_percent_35));
//        txt.setText("   Enter Text   ");
//        addSpinnerListener(f_spinner);
//        txt.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN)
//                    if (txt.getText().toString().equalsIgnoreCase("   Enter Text   ")) {
//                        txt.setText("");
//                        txt.setTextColor(Color.BLACK);
//                    }
//                return false;
//            }
//
//
//        });
//        final Spinner more_spinner = (Spinner) findViewById(R.id.more_spinner);
//        addSpinnerListener(more_spinner);
//        CheckBox ctrl = (CheckBox) findViewById(R.id.control);
//        CheckBox alt = (CheckBox) findViewById(R.id.alt);
//        CheckBox shift = (CheckBox) findViewById(R.id.shift);
//        ctrl.setOnCheckedChangeListener(oncheck);
//        alt.setOnCheckedChangeListener(oncheck);
//        shift.setOnCheckedChangeListener(oncheck);
    }

    private Keyboard mKeyboard;
    private KeyboardView mKeyboardView;

    private void openKeyboard() {
        RelativeLayout ll = (RelativeLayout) findViewById(R.id.keyboard_layout);
        isKeyboardOnView = true;
        if (mKeyboard == null) {
            mKeyboard = new Keyboard(MouseUIActivity.this, R.xml.hexkbd);
        }
        // Lookup the KeyboardView
        mKeyboardView = (KeyboardView) findViewById(R.id.keyboardview);

        CheckBox alt = (CheckBox) ll.findViewById(R.id.alt_checkbox);
        CheckBox shift = (CheckBox) ll.findViewById(R.id.shift_checkbox);
        CheckBox ctrl = (CheckBox) ll.findViewById(R.id.ctrl_checkbox);
        final Spinner f_spinner = (Spinner) ll.findViewById(R.id.f_spinner);


        f_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position != 0) {
                    MouseUIActivity.ps.println("keyboard:" + f_spinner.getSelectedItem().toString());
                    MouseUIActivity.ps.flush();
                    f_spinner.setSelection(0);
                    f_spinner.setRotationY(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        alt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MouseUIActivity.ps.println("keyboard:" + "ALT_START");
                    MouseUIActivity.ps.flush();
                } else {
                    MouseUIActivity.ps.println("keyboard:" + "ALT_STOP");
                    MouseUIActivity.ps.flush();
                }
            }
        });


        shift.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MouseUIActivity.ps.println("keyboard:" + "SHIFT_START");
                    MouseUIActivity.ps.flush();
                } else {
                    MouseUIActivity.ps.println("keyboard:" + "SHIFT_STOP");
                    MouseUIActivity.ps.flush();
                }
            }
        });

        ctrl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MouseUIActivity.ps.println("keyboard:" + "CTRL_START");
                    MouseUIActivity.ps.flush();
                } else {
                    MouseUIActivity.ps.println("keyboard:" + "CTRL_STOP");
                    MouseUIActivity.ps.flush();
                }
            }
        });

        // Attach the keyboard to the view
        mKeyboardView.setKeyboard(mKeyboard);
        // Do not show the preview balloons
        mKeyboardView.setPreviewEnabled(false);
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
//        }


        ll.setVisibility(View.VISIBLE);

    }

    //
//    private void hideKeyboard() {
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
////        mKeyboard.setShifted()
//
//    }
//
    private KeyboardView.OnKeyboardActionListener mOnKeyboardActionListener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
//            Toast.makeText(MouseUIActivity.this, hash.get(primaryCode), Toast.LENGTH_SHORT).show();
            if (hash.get(primaryCode) != null) {
                if (hash.get(primaryCode).equals("HIDE")) {
                    hideKeyboard();
                    return;
                } else {
                    MouseUIActivity.ps.println("keyboard:" + hash.get(primaryCode));
                    MouseUIActivity.ps.flush();
                }
            }
        }

        ;

        @Override
        public void onPress(int arg0) {
        }

        @Override
        public void onRelease(int primaryCode) {
        }

        @Override
        public void onText(CharSequence text) {
            Toast.makeText(MouseUIActivity.this, text, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeUp() {
        }
    };


    CompoundButton.OnCheckedChangeListener oncheck = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.isChecked()) {
                MouseUIActivity.ps.println("keyboard:" + toUperCase(buttonView.getText().toString()) + "_START");
                MouseUIActivity.ps.flush();
            } else {
                MouseUIActivity.ps.println("keyboard:" + toUperCase(buttonView.getText().toString()) + "_STOP");
                MouseUIActivity.ps.flush();
            }

        }
    };

    private void addSpinnerListener(final Spinner spinner) {


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    MouseUIActivity.ps.println("keyboard:" + toUperCase(spinner.getSelectedItem() + ""));
                    MouseUIActivity.ps.flush();
                    runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            Toast.makeText(MouseUIActivity.this, toUperCase(spinner.getSelectedItem() + ""), Toast.LENGTH_LONG).show();

                        }
                    });
                    spinner.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private boolean hideKeyboard() {
        boolean isKeyboardOnView2 = isKeyboardOnView;
        if (isKeyboardOnView2) {
            RelativeLayout ll = (RelativeLayout) findViewById(R.id.keyboard_layout);
            CheckBox alt = (CheckBox) ll.findViewById(R.id.alt_checkbox);
            CheckBox shift = (CheckBox) ll.findViewById(R.id.shift_checkbox);
            CheckBox ctrl = (CheckBox) ll.findViewById(R.id.ctrl_checkbox);

            if (alt.isChecked()) {
                alt.setChecked(false);
                MouseUIActivity.ps.println("keyboard:" + "ALT_STOP");
                MouseUIActivity.ps.flush();
            }
            if (shift.isChecked()) {
                shift.setChecked(false);
                MouseUIActivity.ps.println("keyboard:" + "SHIFT_STOP");
                MouseUIActivity.ps.flush();
            }
            if (ctrl.isChecked()) {
                ctrl.setChecked(false);
                MouseUIActivity.ps.println("keyboard:" + "CTRL_STOP");
                MouseUIActivity.ps.flush();
            }

            ll.setVisibility(View.INVISIBLE);
        }

        isKeyboardOnView = false;
        return isKeyboardOnView2;
    }

    boolean firstTime;

    private void showHelp(String tabTitle, boolean firstTime) {
        this.firstTime = firstTime;
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view
        hideKeyboard();
        MaterialShowcaseSequence firstTimeSequence;
        if (firstTime)
            firstTimeSequence = new MaterialShowcaseSequence(this, "isFirstTimeShowing :" + tabTitle);
        else {
            firstTimeSequence = new MaterialShowcaseSequence(this);
        }
        firstTimeSequence.setConfig(config);


        switch (tabTitle) {
            case "MousePad":
//                RelativeLayout mousepad_screen = (RelativeLayout) findViewById(R.id.mousepad);
//                sequence.addSequenceItem(mousepad_screen,
//                        "Remote desktop's screen", "");

                Toolbar tool = (Toolbar) findViewById(R.id.toolbar);
                firstTimeSequence.addSequenceItem(tool.getChildAt(4), "Press this button for more options.", "");
                firstTimeSequence.addSequenceItem(tool.getChildAt(1), "Press this button to show-hide zoom bar.", "");
                firstTimeSequence.addSequenceItem(tool.getChildAt(2), "Press this button to show-hide the mouse buttons.", "");

                firstTimeSequence.addSequenceItem(tool.getChildAt(3), "Press this button to show-hide toolbar.", "");

                TextView keyboard = (TextView) findViewById(R.id.keyboard);
                firstTimeSequence.addSequenceItem(keyboard, "Press this button to show the keyboard.\n\nOn the right there are various options for each tab." +
                        "\n\nInfo: There are two mouses on screen, android and computer, computer's mouse is useful when you want to see what is happening to you computer ;) .\n\nHint: Enable or disable the second mouse by touching three fingers on the screen.", "");

                DiscreteSeekBar zoom_seekBar = (DiscreteSeekBar) findViewById(R.id.zoom_seekBar);
                firstTimeSequence.addSequenceItem(zoom_seekBar,
                        "Zoom in and zoom out to focus on remote desktop's screen, you can also do it by scrolling-zooming two fingers on screen." +
                                "\n\nHint: enable it instead of \"two finger's scroll\", from option list menu (right on top -> Two Fingers Option).", "");

                Button lc = (Button) findViewById(R.id.lc);
                firstTimeSequence.addSequenceItem(lc,
                        "Left mouse click, can be also done by clicking once on screen.\n\n" +
                                "Info: When you see the three dots waiting icon, is good to wait until the icon become invisible, and then touch the screen (if you press it when is visible, you might need to press two fingers to update the remote desktop's icon on mousepad)." +
                                "\n\nHint : Touch with four fingers on the screen to show or hide the zoombar.", "");

                Button scroll_down = (Button) findViewById(R.id.scroll_down);
                firstTimeSequence.addSequenceItem(scroll_down,
                        "Scroll up and down for scrolling mouse effect, you can also do it by scrolling both two fingers on mousepad up or down, by default is enabled." +
                                "\n\nHint: disable it and enable \"two finger's zoom\" instead, from option list menu (right on top-> Two Fingers Option).", "");


                Button rc = (Button) findViewById(R.id.rc);
                firstTimeSequence.addSequenceItem(rc,
                        "Right mouse click \n\n " +

                                "\n\nHint: when you are using the zoom option press two fingers on the screen to update-refresh the screen based on the mouse location. " +
                                "The screen updates also on right-left mouse click.", "");


//                sequence.addSequenceItem(findViewById(menu.getItem(0).getItemId()), "", "Options: each tab has different options");


                break;
            case "Keyboard":

                TextView textScreen = (TextView) findViewById(R.id.textScreen);
                firstTimeSequence.addSequenceItem(textScreen,
                        "Write and send text to your computer", "");

                Button show_more_button = (Button) findViewById(R.id.show_more_button);
                firstTimeSequence.addSequenceItem(show_more_button,
                        "Show extra buttons (some button's combinations) ", "");
                break;
            case "Power Controls":


                Button shut_down = (Button) findViewById(R.id.shut_down);
                firstTimeSequence.addSequenceItem(shut_down,
                        "Shut down immediately or put a timer to your computer to shut down.", "");
                break;

            case "Bash Command Line":
                EditText command_view = (EditText) findViewById(R.id.command_view);
                firstTimeSequence.addSequenceItem(command_view,
                        "Enter commands.", "");
                Button execute = (Button) findViewById(R.id.execute);
                firstTimeSequence.addSequenceItem(execute,
                        "Execute the command you wrote.", "");
                CheckBox remember = (CheckBox) findViewById(R.id.remember);
                firstTimeSequence.addSequenceItem(remember,
                        "Remember or not previous commands.", "");
                Button clr = (Button) findViewById(R.id.clr);
                firstTimeSequence.addSequenceItem(clr,
                        "Clear the previous commands.", "");

                break;

            case "Send":
                EditText folder_name = (EditText) findViewById(R.id.folder_name);
                firstTimeSequence.addSequenceItem(folder_name,
                        "Name of the folder that the files will be stored on your Desktop.", "");

                break;

            case "Motion Sensor":
                Button leftclick = (Button) findViewById(R.id.leftclick);
                firstTimeSequence.addSequenceItem(leftclick,
                        "Rotate your phone to move your mouse.", "");

                break;
        }


        firstTimeSequence.start();
    }


    private void generateHelp(String tabTitle) {
        showHelp(tabTitle, false);
    }

    CustomDelegate customDelegate = null;

    private void createOptions(final String tabTitle) {


        if (mSweetSheet != null && mSweetSheet.isShow()) {
            mSweetSheet.dismiss();

        }

        if (mSweetSheet == null) {
            RelativeLayout fragContainer = (RelativeLayout) findViewById(com.nikos.tsoglani.androidmouse.R.id.app);
            mSweetSheet = new SweetSheet(fragContainer);
//定义一个 CustomDelegate 的 Delegate ,并且设置它的出现动画.
            customDelegate = new CustomDelegate(true,
                    CustomDelegate.AnimationType.DuangLayoutAnimation);
            View view = LayoutInflater.from(this).inflate(R.layout.options_view, null, false);
//设置自定义视图.
            customDelegate.setCustomView(view);
//设置代理类
            mSweetSheet.setDelegate(customDelegate);


        }
        mSweetSheet.show();
        OptionListeners(tabTitle);
//因为使用了 CustomDelegate 所以mSweetSheet3中的 setMenuList和setOnMenuItemClickListener就没有效果了

        TextView txtView = (TextView) findViewById(R.id.resolution_text);
        DiscreteSeekBar resolution = (DiscreteSeekBar) findViewById(R.id.resolution);
        TextView textView8 = (TextView) findViewById(R.id.textView8);
        CheckBox reverse = (CheckBox) findViewById(R.id.reverse);
        RadioGroup myRadioGroup = (RadioGroup) findViewById(R.id.myRadioGroup);
        DiscreteSeekBar mouse_speed_delay = (DiscreteSeekBar) findViewById(R.id.mouse_speed_delay);
        TextView mouse_speed_text = (TextView) findViewById(R.id.mouse_speed_text);
        CheckBox show_computer_mouse_seperate = (CheckBox) findViewById(R.id.show_computer_mouse_seperate);
        View view = findViewById(R.id.view);
        switch (tabTitle) {
            case "Spy Camera":
                resolution.setVisibility(View.INVISIBLE);
                txtView.setVisibility(View.INVISIBLE);
                textView8.setVisibility(View.INVISIBLE);
                reverse.setVisibility(View.INVISIBLE);
                myRadioGroup.setVisibility(View.INVISIBLE);
                view.setVisibility(View.INVISIBLE);
                mouse_speed_delay.setVisibility(View.INVISIBLE);
                mouse_speed_text.setVisibility(View.INVISIBLE);
                show_computer_mouse_seperate.setVisibility(View.INVISIBLE);
                if (customDelegate != null) {
                    customDelegate.setContentHeight(dpToPx(400));
                }
                break;
            case "MousePad":
                resolution.setVisibility(View.VISIBLE);
                txtView.setVisibility(View.VISIBLE);
                textView8.setVisibility(View.VISIBLE);
                show_computer_mouse_seperate.setVisibility(View.VISIBLE);
                reverse.setVisibility(View.VISIBLE);
                myRadioGroup.setVisibility(View.VISIBLE);
                view.setVisibility(View.VISIBLE);
                mouse_speed_delay.setVisibility(View.VISIBLE);
                mouse_speed_text.setVisibility(View.VISIBLE);
                if (customDelegate != null) {
                    int pxl = 0;
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        pxl = dpToPx(536);
                    } else {
                        pxl = dpToPx(300);
                    }
                    if (getWindowManager().getDefaultDisplay().getHeight() - 100 < pxl) {
                        pxl = getWindowManager().getDefaultDisplay().getHeight() - 100;
                    }
                    customDelegate.setContentHeight(pxl);
                }
                break;
        }


        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence;

        sequence = new MaterialShowcaseSequence(this, "FirstTime:" + tabTitle);

        if (tabTitle.equalsIgnoreCase("MousePad")) {
            sequence.addSequenceItem(resolution, "You can change resolution, if you increase resolution, you might have to increase delay", "");
        }
        DiscreteSeekBar delay = (DiscreteSeekBar) findViewById(R.id.time_delay);
        sequence.addSequenceItem(delay, "You can change delay, you need to increase for better resolution and quality", "");

        DiscreteSeekBar quality = (DiscreteSeekBar) findViewById(R.id.quallity);
        sequence.addSequenceItem(quality, "You can change quality, for clearer picture, if you increase resolution, you might have to increase delay", "");
        if (tabTitle.equalsIgnoreCase("MousePad")) {
            sequence.addSequenceItem(show_computer_mouse_seperate, "There are two mouses on screen, one computer's mouse, and one android virtual mouse," +
                    " you can enable and disable computer's mouse from screen ( computer's mouse can be useful to check if the zoom has accuracy ).  ", "");
        }
        sequence.setConfig(config);
        sequence.start();


    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private void OptionListeners(final String tabTitle) {
        DiscreteSeekBar resolution = (DiscreteSeekBar) findViewById(R.id.resolution);
        final TextView resolution_text = (TextView) findViewById(R.id.resolution_text);
        resolution_text.setText("Resolution : " + receivedImageX);
        resolution.setMax(maxResolution);
        receivedImageX = getSavedInt(this, "Resolution", receivedImageX);
        resolution.setProgress(receivedImageX);
        resolution.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int i, boolean b) {
                MouseUIActivity.ps.println("Resolution:" + discreteSeekBar.getProgress());
                receivedImageX = discreteSeekBar.getProgress();
                receivedImageY = discreteSeekBar.getProgress();
                saveInteger("Resolution", i);
                resolution_text.setText("Resolution : " + discreteSeekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {

            }
        });
        boolean is_show_computer_mouse_seperate = MouseUIActivity.getSavedBoolean(MouseUIActivity.this, show_computer_mouse_seperateString, false);

        CheckBox show_computer_mouse_seperate = (CheckBox) findViewById(R.id.show_computer_mouse_seperate);
        show_computer_mouse_seperate.setChecked(is_show_computer_mouse_seperate);
        show_computer_mouse_seperate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveBoolean(show_computer_mouse_seperateString, isChecked);
                MouseUIActivity.ps.println("ShowMouse:" + isChecked);
                MouseUIActivity.ps.flush();

            }
        });
        CheckBox is_two_fingers_scroll = (CheckBox) findViewById(R.id.reverse);
        is_two_fingers_scroll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveBoolean(isReverseString, isChecked);
            }
        });
        DiscreteSeekBar qualityBar = (DiscreteSeekBar) findViewById(R.id.quallity);
        final TextView quality_text = (TextView) findViewById(R.id.quantity_text);
        if (tabTitle.equalsIgnoreCase("Spy Camera")) {
            qualityBar.setProgress(qualityCam);
            quality_text.setText("Quality : " + qualityCam);
        } else if (tabTitle.equalsIgnoreCase("Mousepad")) {
            qualityBar.setProgress(qualityPad);
            quality_text.setText("Quality : " + qualityPad);
        }
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.myRadioGroup);


        boolean is_zoom_radio_on = MouseUIActivity.getSavedBoolean(MouseUIActivity.this, is_zoom_radio_on_String, false);
        CheckBox isReverse = (CheckBox) findViewById(R.id.reverse);
        if (is_zoom_radio_on) {
            radioGroup.check(R.id.zoom_radio);
            isReverse.setEnabled(false);
        } else {
            radioGroup.check(R.id.scroll_radio);
            isReverse.setEnabled(true);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                CheckBox isReverse = (CheckBox) findViewById(R.id.reverse);
                if (checkedId == R.id.scroll_radio) {

                    saveBoolean(is_zoom_radio_on_String, false);
                    isReverse.setEnabled(true);
                    Toast.makeText(MouseUIActivity.this, "Scroll both with two fingers up or down  on screen for mouse scrolling", Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.zoom_radio) {
                    saveBoolean(is_zoom_radio_on_String, true);
                    Toast.makeText(MouseUIActivity.this, "Scroll with two fingers on screen for zoom in or zoum out the remote desktop's image", Toast.LENGTH_SHORT).show();
                    isReverse.setEnabled(false);
                }
            }

        });


        qualityBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int i, boolean b) {
                if (tabTitle.equalsIgnoreCase("Spy Camera")) {
                    qualityCam = discreteSeekBar.getProgress();
                    MouseUIActivity.ps.println("QualityCam:" + qualityCam);
                    quality_text.setText("Quality : " + qualityCam);
                    saveInteger("qualityCam", i);
                } else if (tabTitle.equalsIgnoreCase("Mousepad")) {
                    qualityPad = discreteSeekBar.getProgress();
                    MouseUIActivity.ps.println("QualityPad:" + qualityPad);
                    quality_text.setText("Quality : " + qualityPad);
                    saveInteger("qualityPad", i);
                }

            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {

            }
        });

        DiscreteSeekBar mouseSpeed = (DiscreteSeekBar) findViewById(R.id.mouse_speed_delay);
        final TextView textView = (TextView) findViewById(R.id.mouse_speed_text);
        int firstProgres = getSavedInt(this, "Mouse Speed", 8);
        float extraMultiMouseMove = 1 + firstProgres / 10.0f;

        mouseSpeed.setProgress(firstProgres);
        textView.setText("Mouse Speed : " + extraMultiMouseMove);
        mouseSpeed.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int i, boolean b) {


                textView.setText("Mouse Speed : " + (1 + i / 10.0f));
                saveInteger("Mouse Speed", i);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {

            }
        });


        DiscreteSeekBar delayTime = (DiscreteSeekBar) findViewById(R.id.time_delay);
        final TextView timedelayText = (TextView) findViewById(R.id.delat_time_text);
        if (tabTitle.equalsIgnoreCase("Spy Camera")) {
            delayTime.setProgress(sleepintTimeForScrenShot);
            timedelayText.setText("Time Delay  (ms) : " + sleepintTimeForScrenShot);
        } else if (tabTitle.equalsIgnoreCase("Mousepad")) {
            delayTime.setProgress(sleepingTime);
            timedelayText.setText("Time Delay  (ms) : " + sleepingTime);
        }

        delayTime.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int i, boolean b) {
                if (tabTitle.equalsIgnoreCase("Spy Camera")) {
                    sleepintTimeForScrenShot = discreteSeekBar.getProgress();
                    timedelayText.setText("Time Delay  (ms) : " + sleepintTimeForScrenShot);

                } else if (tabTitle.equalsIgnoreCase("Mousepad")) {
                    sleepingTime = discreteSeekBar.getProgress();
                    timedelayText.setText("Time Delay  (ms) : " + sleepingTime);
                }
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {

            }
        });
    }


    public void onClose(View v) {
        if (mSweetSheet != null && mSweetSheet.isShow()) {
            mSweetSheet.dismiss();

        }

    }

    public void showKeyboard(View v) {
        if (!isKeyboardOnView)
            generateKeyboard();
        else
            hideKeyboard();
    }

//    public void zoom_onoff(View v) {
//        View rl = findViewById(R.id.relativeLayout);
//
//        if (rl.getVisibility() == View.VISIBLE) {
//            v.setBackgroundResource(R.drawable.nozoom);
//
//        } else {
//            v.setBackgroundResource(R.drawable.zoom);
//
//        }
//        toggleZoomBar();
//    }
//
//
//    private void toggleZoomBar() {
//
//        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
//
//        if (relativeLayout.getVisibility() == View.VISIBLE) {
//            relativeLayout.setVisibility(View.GONE);
//        } else {
//            relativeLayout.setVisibility(View.VISIBLE);
//
//        }
//    }
}

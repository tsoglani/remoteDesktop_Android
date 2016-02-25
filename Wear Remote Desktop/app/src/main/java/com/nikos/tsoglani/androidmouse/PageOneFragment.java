package com.nikos.tsoglani.androidmouse;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/**
 * Created by tsoglani on 26/6/2015.
 */
public class PageOneFragment extends android.support.v4.app.Fragment {
    //  private Point point1;
    private long curTime;
    private final int doubleTouchDelay = 50;
    private final int doubleTouchDistance = 15;
    //    private boolean detectFirstTouch = true;
    float finishEventBarVisible, startEventBarVisible = 0;
    float firstTouchX = -1, firstTouchY = -1;
    public float extraMultiMouseMove = 2.0f;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(com.nikos.tsoglani.androidmouse.R.layout.fragment_one, container, false);
    }

    private Thread resetTimeThreadForLeftClick;
    private int counter;
    private static boolean isGoint2left = false;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivePointers = new SparseArray<PointF>();

        PageOneFragment.isOnTouchDown = false;
        zoomValue = 0;
        getActivity().findViewById(R.id.zoom_onoff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View rl = getActivity().findViewById(R.id.relativeLayout);
                toggleZoomBar();

            }
        });

        getActivity().findViewById(R.id.mouse_buttons_visibility).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMouseButtonsLayout();
            }
        });
        final View bar_visibility_show = getActivity().findViewById(R.id.bar_visibility_show);
        final View bar_visibility_hide = getActivity().findViewById(R.id.bar_visibility_hide);
        getActivity().findViewById(R.id.bar_visibility_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleToolbar();
                bar_visibility_hide.setVisibility(View.VISIBLE);
            }
        });
        View zoom_onoff = getActivity().findViewById(R.id.zoom_onoff);
        zoom_onoff.setBackgroundResource(R.drawable.zoom1);
        getActivity().findViewById(R.id.mouse_buttons_visibility).setBackgroundResource(R.drawable.mouse1);

//        bar_visibility_hide.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleToolbar();
//                bar_visibility_hide.setVisibility(View.GONE);
//            }
//        });
        bar_visibility_hide.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                getActivity().runOnUiThread(new Thread() {
                    @Override
                    public void run() {

                        int maxSleepTime = 110;
                        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                            startEventBarVisible = event.getEventTime();
                        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                            if ((event.getEventTime() - startEventBarVisible) > maxSleepTime) {
                                bar_visibility_hide.setX(event.getX() + bar_visibility_hide.getX() - bar_visibility_hide.getWidth() / 2);
                                bar_visibility_hide.setY(event.getY() + bar_visibility_hide.getY() - bar_visibility_hide.getHeight() / 2);
                                if (bar_visibility_hide.getX() < 0) {
                                    bar_visibility_hide.setX(0);
                                }
                                if (bar_visibility_hide.getY() < 0) {
                                    bar_visibility_hide.setY(0);
                                }
                                final RelativeLayout fl = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen);

                                if (bar_visibility_hide.getX() + bar_visibility_hide.getWidth() > fl.getWidth()) {
                                    bar_visibility_hide.setX(fl.getWidth() - bar_visibility_hide.getWidth());
                                }
                                if (bar_visibility_hide.getY() + bar_visibility_hide.getHeight() > fl.getHeight()) {
                                    bar_visibility_hide.setY(fl.getHeight() - bar_visibility_hide.getHeight());
                                }
                            }
                        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                            finishEventBarVisible = event.getEventTime();

                            if (finishEventBarVisible - startEventBarVisible < maxSleepTime) {
                                toggleToolbar();
                                bar_visibility_hide.setVisibility(View.GONE);
                            }
                        }

                    }
                });

                return true;
            }
        });

        if( MouseUIActivity.ps==null){
            return;
        }
        boolean is_show_computer_mouse_seperate = MouseUIActivity.getSavedBoolean(getActivity(), MouseUIActivity.show_computer_mouse_seperateString, false);
        MouseUIActivity.ps.println("ShowMouse:" + is_show_computer_mouse_seperate);
        MouseUIActivity.ps.flush();
        waitUntilDraw = false;
        View coursor = getActivity().findViewById(R.id.coursor);

        View second_coursor = getActivity().findViewById(R.id.second_coursor);

        if (zoomValue >= 3) {
            final RelativeLayout fl = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen);
            coursor.setVisibility(View.INVISIBLE);
            second_coursor.setVisibility(View.VISIBLE);

            updateZoomViewTwice();

//                    setVirtualCursorOnZoom();

        } else {
            coursor.setVisibility(View.VISIBLE);
            second_coursor.setVisibility(View.INVISIBLE);
        }

        MouseUIActivity.ps.println("ZOOM:" + PageOneFragment.zoomValue);

        final Button lc = (Button) getActivity().findViewById(R.id.lc);

        lc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    if (MotionEvent.ACTION_UP == event.getAction()) {
                        MouseUIActivity.ps.println("LEFT_CLICK_UP");
                        MouseUIActivity.ps.flush();
                        lc.setBackgroundResource(R.drawable.left_click);
                        isOnTouchDown = false;
                        waitUntilDraw = false;
                        updateZoomViewWithLocation();
                    } else if (MotionEvent.ACTION_DOWN == event.getAction()) {
                        MouseUIActivity.ps.println("LEFT_CLICK_DOWN");
                        MouseUIActivity.ps.flush();
                        lc.setBackgroundResource(R.drawable.left_click2pressed3);
                        isOnTouchDown = false;
                        waitUntilDraw = false;
                        updateZoomViewWithLocation();
                    }
                } catch (final Exception e) {
                    getActivity().runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return false;
            }
        });

        final Button rc = (Button) getActivity().findViewById(R.id.rc);
        rc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    rc.setBackgroundResource(R.drawable.right_click_click);
                    isOnTouchDown = false;
                    waitUntilDraw = false;
                    updateZoomViewWithLocation();
                } else if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    rc.setBackgroundResource(R.drawable.right_click_click2pressed3);

                }

                return false;
            }
        });


        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) getActivity().findViewById(R.id.toolbar);


        Button scroll = (Button) getActivity().findViewById(R.id.scroll_down);


        scroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                scroll(event);
                return true;
            }
        });


        final RelativeLayout fl = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen);
//        RelativeLayout mousepad_screen_relative_layout = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen_relative_layout);
        int width=getActivity().getWindowManager().getDefaultDisplay().getWidth(),height=getActivity().getWindowManager().getDefaultDisplay().getHeight();
        int maxWidthOrHeight=Math.max(width,height);

        fl.setLayoutParams(new RelativeLayout.LayoutParams(2000,2000));
        fl.setOnTouchListener(onTouchListener);

        DiscreteSeekBar sb = (DiscreteSeekBar) getActivity().findViewById(R.id.zoom_seekBar);
        sb.setProgress(zoomValue);
        sb.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int i, boolean b) {
                if (MouseUIActivity.ps != null){
                    MouseUIActivity.ps.println("ZOOM:" + discreteSeekBar.getProgress());}
                else{
                    Intent intent= new Intent(getContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                TextView zoomView = (TextView) getActivity().findViewById(R.id.zoom_text);
                zoomValue = discreteSeekBar.getProgress();
                zoomView.setText("ZOOM: " + zoomValue + "%");
                updateZoomViewWithLocation();
                View coursor = getActivity().findViewById(R.id.coursor);
                View second_coursor = getActivity().findViewById(R.id.second_coursor);

//                RelativeLayout mousepad_screen_relative_layout = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen_relative_layout);
//                RelativeLayout.LayoutParams p2 = new RelativeLayout.LayoutParams(mousepad_screen_relative_layout.getWidth() + mousepad_screen_relative_layout.getWidth() * (int) (zoomValue / 100.0f), mousepad_screen_relative_layout.getHeight() + mousepad_screen_relative_layout.getHeight() * (int) (zoomValue / 100.0f));
//                fl.setLayoutParams(p2);
                if (zoomValue == 0) {
                    second_coursor.setX(coursor.getX());
                    second_coursor.setY(coursor.getY());
                    coursor.setVisibility(View.VISIBLE);
                    second_coursor.setVisibility(View.INVISIBLE);
                } else if (zoomValue >= 0) {
                    coursor.setVisibility(View.INVISIBLE);
                    second_coursor.setVisibility(View.VISIBLE);

                    updateZoomViewTwice();
//                    setVirtualCursorOnZoom();

                }


            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {
                firstTouchX = -1;
                firstTouchY = -1;
                waitUntilDraw = false;
                View coursor = getActivity().findViewById(R.id.coursor);
                MouseUIActivity.ps.println("MoveTo:x=" + (coursor.getX()) / fl.getWidth() + ":y=" + (coursor.getY()) / fl.getHeight());
                updateZoomViewWithLocation();
            }


            @Override
            public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {
                updateZoomViewTwiceSmall();
//                ((MouseUIActivity) getActivity()).saveInteger("zoomValue", discreteSeekBar.getProgress());
            }
        });


        if (zoomValue != 0) {
            updateZoom();
        }

//
//        final RelativeLayout rl = (RelativeLayout) getActivity().findViewById(R.id.mousepad);
//
//        int height = (int) (getActivity().getWindowManager().getDefaultDisplay().getHeight() * 1.5f);
//        int width = (int) (getActivity().getWindowManager().getDefaultDisplay().getWidth() * 1.5f);
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
//        rl.setLayoutParams(params);

    }

    private void updateZoom() {
        waitUntilDraw = false;
        isOnTouchDown = false;
        View second_coursor = getActivity().findViewById(R.id.second_coursor);
        RelativeLayout fl = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen);
        if (!isColidingX() && !isColideEventDownX) {
            second_coursor.setX(fl.getWidth() / 2);
        } else if (!isColidingY() && !isColideEventDownY) {
            second_coursor.setY(fl.getHeight() / 2);

        }

        updateZoomViewWithLocation();
        DiscreteSeekBar sb = (DiscreteSeekBar) getActivity().findViewById(R.id.zoom_seekBar);
        TextView zoomView = (TextView) getActivity().findViewById(R.id.zoom_text);
        if (sb != null)
            sb.setProgress(PageOneFragment.zoomValue);
        if (zoomView != null)
            zoomView.setText("ZOOM: " + PageOneFragment.zoomValue + "%");
        if (MouseUIActivity.ps != null)
            MouseUIActivity.ps.println("ZOOM:" + PageOneFragment.zoomValue);
        DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);

    }

    private void scroll(MotionEvent event) {
        boolean isGoingUp = false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            previousEventForScrollY = (event.getY());
            return;
        }
        isGoingUp = isGoingUp(event);
        if (isGoingUp) {
            MouseUIActivity.ps.println("SCROLL_DOWN");
            MouseUIActivity.ps.flush();
        } else {
            MouseUIActivity.ps.println("SCROLL_UP");
            MouseUIActivity.ps.flush();
        }
    }

    float previousEventForScrollY = -1;
    boolean isSecondHandPressed = false;
    private SparseArray<PointF> mActivePointers;

    private boolean isGoingUp(MotionEvent event) {
        boolean returningValue = true;
        if (previousEventForScrollY > (event.getY())) {
            returningValue = false;
        }
        previousEventForScrollY = event.getY();
        return returningValue;
    }

    long startEvent, finishEvent;
    Point startPoint;
    boolean isColideEventDown, isColideEventDownX, isColideEventDownY;
    boolean isXZoomFirstTime, isYZoomFirstTime;
    static boolean isOnTouchDown = false, waitUntilDraw = false;

    private Point mouseDownPoint;
    private Point secondMouseDownPoint;
    Thread oneThread;
    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            View second_coursor = getActivity().findViewById(R.id.second_coursor);
            int distanceXToSplit = 0, distanceYToSplit = 0;
            View coursor = getActivity().findViewById(R.id.coursor);
//            View second_coursor = getActivity().findViewById(R.id.second_coursor);
            RelativeLayout fl = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen);

            try {

                boolean handleZoomeValue = handleTouch(event);
                if (loadingAnimation) {
                    return false;
                }
                if (handleZoomeValue) {
                    firstTouchX = -1;
                    firstTouchY = -1;
                    return true;
                }


                int action = event.getAction() & MotionEvent.ACTION_MASK;
                if (MotionEvent.ACTION_DOWN == action) {
//                    drawLastImage();
                    firstTouchX = -1;
                    firstTouchY = -1;
                    startEvent = event.getEventTime();
                    startPoint = new Point((int) event.getX(), (int) event.getY());


                    isOnTouchDown = true;
                    if (zoomValue > 0) {
                        if (!waitUntilDraw) {
                            MouseUIActivity.ps.println("MoveTo:x=" + (coursor.getX()) / fl.getWidth() + ":y=" + (coursor.getY()) / fl.getHeight());
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(200);
                                        if (isOnTouchDown)
                                            waitUntilDraw = true;


                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();

                            isColideEventDownX = isColidingX();
                            isColideEventDownY = isColidingY();
                            isColideEventDown = isColideXY();
                            mouseDownPoint = new Point((int) coursor.getX(), (int) coursor.getY());

                        }
                    }
                    secondMouseDownPoint = new Point((int) second_coursor.getX(), (int) second_coursor.getY());

                } else if (action == MotionEvent.ACTION_MOVE) {
                    ////////////// kinisi pontikiou


                    if (firstTouchX == -1 || firstTouchY == -1) {
                        firstTouchX = event.getX();
                        firstTouchY = event.getY();
                    } else {
                        float moveX = 0, moveY = 0;
                        moveX = event.getX() - firstTouchX;/////to checkk
                        moveY = event.getY() - firstTouchY;/////to checkk
                        extraMultiMouseMove = 1 + MouseUIActivity.getSavedInt(getActivity(), "Mouse Speed", 20) / 10.0f;

                        float coursourX = coursor.getX() + moveX * extraMultiMouseMove;/// to checkk
                        float coursourY = coursor.getY() + moveY * extraMultiMouseMove;/// to checkk

                        if (coursourX <= 0) {
                            coursourX = 0;
                        }
                        if (coursourX > fl.getWidth()) {
                            coursourX = fl.getWidth();
                        }
                        if (coursourY > fl.getHeight()) {
                            coursourY = fl.getHeight();
                        }
                        if (coursourY <= 0) {
                            coursourY = 0;
                        }

//                        if (isColideXY() || second_coursor.getX() > 50 && second_coursor.getX() < fl.getWidth() - 50 && second_coursor.getY() > 50 && second_coursor.getY() < fl.getHeight() - 50) {
                        coursor.setX(coursourX);// set the first coursor 2 dp left for synchronization
                        coursor.setY(coursourY);
//                        }
                        secondCursorAlgo(event);

                        if (finishEvent - startEvent > 110) {
                            counter = -1;
                        }

                        MouseUIActivity.ps.println("MoveTo:x=" + (coursor.getX() + dpToPx(1.5f)) / fl.getWidth() + ":y=" + (coursor.getY()) / (fl.getHeight()));

                        firstTouchX = -1;
                        firstTouchY = -1;
                    }
                    ////////////
                } else if (action == MotionEvent.ACTION_UP) {
                    isOnTouchDown = false;
                    if (zoomValue > 0) {
                        if (second_coursor.getX() <= 50 || second_coursor.getX() >= fl.getWidth() - 50 || second_coursor.getY() <= 50 || second_coursor.getY() >= fl.getHeight() - 50) {

                            if ((!isColideEventDown || !isColideXY())) {
                                secondCursonAlgoUp(event);
                                waitUntilDraw = false;
//                            setVirtualCursorOnZoom();

                            }
                            if (mouseDownPoint != null) {
                                if ((second_coursor.getX() <= 50) && mouseDownPoint.x > 50 || mouseDownPoint.x <= fl.getWidth() - 50 && (second_coursor.getX() >= fl.getWidth() - 50)) {
//                            second_coursor.setX(fl.getWidth() / 2);
                                    waitUntilDraw = false;
                                    updateZoomViewTwice();
                                } else if ((second_coursor.getY() <= 50) && mouseDownPoint.y > 50 || mouseDownPoint.y <= fl.getHeight() - 50 && (second_coursor.getY() >= fl.getHeight() - 50)) {
//                            second_coursor.setY(fl.getHeight() / 2);
                                    waitUntilDraw = false;

                                    updateZoomViewTwice();

                                }

                            }
                        }
                    }


                    finishEvent = event.getEventTime();
                    if (finishEvent - startEvent > 110) {
                        counter = -1;
                    }

                    float ydif = event.getY() - startPoint.y, xdif = event.getX() - startPoint.x;
                    if (Math.abs(ydif) > 10 || Math.abs(xdif) > 10) {
                        counter = -1;
                    }

                    counter++;
                    if (counter == 1) {
                        MouseUIActivity.ps.println("LEFT_CLICK_DOWN");
                        MouseUIActivity.ps.println("LEFT_CLICK_UP");
                        MouseUIActivity.ps.flush();
                        if (oneThread == null || !oneThread.isAlive()) {
                            oneThread = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        sleep(500);
                                        if (oneThread == this) {
                                            updateZoomViewTwice();
                                            oneThread=null;
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            oneThread.start();
                        }

                    }
                    if (counter == 2) {
                        oneThread = null;
                        MouseUIActivity.ps.println("LEFT_CLICK_DOWN");
                        MouseUIActivity.ps.println("LEFT_CLICK_UP");
                        MouseUIActivity.ps.flush();
                        updateZoomViewTwice();

                    }


                    resetTimeThreadForLeftClick = new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);
                                if (resetTimeThreadForLeftClick == this)
                                    counter = 0;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    };
                    resetTimeThreadForLeftClick.start();
                }


                MouseUIActivity.ps.flush();


            } catch (Exception e) {
                ((MouseUIActivity) getActivity()).closeAll();
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
                e.printStackTrace();
            }
            return true;
        }
    };

    private Bitmap getBitmap() {
        Bitmap bitmapimage = ((MouseUIActivity) getActivity()).bitmapimage;
        return bitmapimage;
    }

    private void drawLastImage(final Bitmap bitmapimage) {

        if (bitmapimage != null) {
            bitmapimage.prepareToDraw();
            // final Drawable draw = new BitmapDrawable(activity.getResources(), bitmapimage);
            final RelativeLayout iv = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen);
            getActivity().runOnUiThread(new Thread() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void run() {

                    try {
                        bitmapimage.prepareToDraw();

                            iv.setBackground(new BitmapDrawable(bitmapimage));

                        System.gc();
                    } catch (final Exception e) {
                        e.printStackTrace();
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    Thread thread;

    public void updateZoomViewWithLocation() {

        try {
            if (getActivity() != null) {
                View second_coursor = getActivity().findViewById(R.id.second_coursor);
//            View second_coursor = getActivity().findViewById(R.id.second_coursor);
                RelativeLayout fl = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen);

                isOnTouchDown = false;
                waitUntilDraw = false;
                boolean colideX = isColidingX();
                boolean colideY = isColidingY();
                if (!colideX) {

                    second_coursor.setX(fl.getWidth() / 2);// set the second coursor 2 dp left for synchronization
                }
                if (!colideY) {
                    second_coursor.setY(fl.getHeight() / 2);

                }
                if (colideX || colideY) {
                    setVirtualCursorOnZoom();

                }


                loadAnim();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean loadingAnimation = false;

    private void loadAnim() {
        if (zoomValue <= 0) {
            return;
        }

        getActivity().runOnUiThread(new Thread() {
            @Override
            public void run() {
                startAnim();
            }
        });
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2 * MouseUIActivity.sleepingTime);
                    if (this == thread)// mono sto last thread
                        getActivity().runOnUiThread(new Thread() {
                            @Override
                            public void run() {
                                stopAnim();
//                                waitUntilDraw = true;// stamataei t image
                            }
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        thread.start();

    }

    void startAnim() {
        getActivity().findViewById(R.id.avloadingIndicatorView).setVisibility(View.VISIBLE);
        loadingAnimation = true;
    }

    void stopAnim() {
        getActivity().findViewById(R.id.avloadingIndicatorView).setVisibility(View.GONE);
        loadingAnimation = false;


    }

    boolean isMouseButtonsLayoutDown = false;

    private void toggleMouseButtonsLayout() {
        if (isMouseButtonsLayoutDown) {
            SlideToAbove();
        } else {
            SlideToDown();
        }

    }

    public void SlideToDown() {
        isMouseButtonsLayoutDown = true;
        final RelativeLayout mousebuttons_layout = (RelativeLayout) getActivity().findViewById(R.id.mousebuttons_layout);
        final RelativeLayout mousepad_screen = (RelativeLayout) getActivity().findViewById(R.id.mousepad);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(getActivity().getWindowManager().getDefaultDisplay().getWidth(), getActivity().getWindowManager().getDefaultDisplay().getHeight());
        mousepad_screen.setLayoutParams(params);
        mousebuttons_layout.animate().translationY(mousebuttons_layout.getHeight()).start();
        getActivity().findViewById(R.id.mouse_buttons_visibility).setBackgroundResource(R.drawable.nomouse1);


    }


    public void SlideToAbove() {
        isMouseButtonsLayoutDown = false;
        final RelativeLayout mousebuttons_layout = (RelativeLayout) getActivity().findViewById(R.id.mousebuttons_layout);
        final RelativeLayout mousepad_screen = (RelativeLayout) getActivity().findViewById(R.id.mousepad);
        mousebuttons_layout.animate().translationY(0).start();
        getActivity().findViewById(R.id.mouse_buttons_visibility).setBackgroundResource(R.drawable.mouse1);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(getActivity().getWindowManager().getDefaultDisplay().getWidth(), getActivity().getWindowManager().getDefaultDisplay().getHeight());
        params.addRule(RelativeLayout.ABOVE, R.id.mousebuttons_layout);
        mousepad_screen.setLayoutParams(params);


    }

    private void setVirtualCursorOnZoom() {
        try {
            View second_coursor = getActivity().findViewById(R.id.second_coursor);
            RelativeLayout fl = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen);
            View coursor = getActivity().findViewById(R.id.coursor);

//        float minSpaceX = fl.getWidth() * (100 - zoomValue) / 100 / 2;
//        float minSpaceY = fl.getHeight() * (100 - zoomValue) / 100 / 2;
//        float moveX = second_coursor.getX() - fl.getWidth() / 2;
//        float moveY = second_coursor.getY() - fl.getHeight() / 2;
//        float minDestanceX = Math.min((int) coursor.getX(), (int) (fl.getWidth() - coursor.getX()));
//        float minDestanceY = Math.min((int) coursor.getY(), (int) (fl.getHeight() - coursor.getY()));

            float moveXDistance = distanceLeftForColideX(), moveYDistance = distanceLeftForColideY();

            if (isColidingX()) {
                //
//                Toast.makeText(getActivity(), "moveXDistance= "+moveXDistance, Toast.LENGTH_SHORT).show();
                second_coursor.setX(fl.getWidth() / 2 - moveXDistance);// set the second coursor 2 dp left for synchronization

            }


            if (isColidingY()) {
//            Toast.makeText(getActivity(), "moveYDistance= "+moveYDistance, Toast.LENGTH_SHORT).show();
                second_coursor.setY(fl.getHeight() / 2 - moveYDistance);//second_coursor.getY()
            }

            if (second_coursor.getX() < 0) {
                second_coursor.setX(0);
            }
            if (second_coursor.getY() < 0) {
                second_coursor.setY(0);
            }

            if (second_coursor.getX() > fl.getWidth()) {
                second_coursor.setX(fl.getWidth() - moveXDistance); // set the second coursor 2 dp left for synchronization
            }
            if (second_coursor.getY() > fl.getHeight()) {
                second_coursor.setY(fl.getHeight());
            }

            loadAnim();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void secondCursonAlgoUp(MotionEvent event) {
        RelativeLayout fl = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen);
        View coursor = getActivity().findViewById(R.id.coursor);
        View second_coursor = getActivity().findViewById(R.id.second_coursor);


        if (!isColidingY()) {
            if (MotionEvent.ACTION_UP == event.getAction()) {
                second_coursor.setY(fl.getHeight() / 2);
                setVirtualCursorOnZoom();
            }
        }
        if (!isColidingX()) {
            if (MotionEvent.ACTION_UP == event.getAction())
                second_coursor.setX(fl.getWidth() / 2);
            setVirtualCursorOnZoom();
        }


    }

    private float distanceLeftForColideX() {
        float output = 0;
        RelativeLayout fl = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen);
        View coursor = getActivity().findViewById(R.id.coursor);
        View second_coursor = getActivity().findViewById(R.id.second_coursor);

//        float minDestanceX = Math.min((int) coursor.getX(), (int) (fl.getWidth() - coursor.getX()));

        final float startPoint = Math.min((int) coursor.getX(), (int) (fl.getWidth() - coursor.getX()));// to point pou patiete to kirio mouse
        final float minSpaceX = fl.getWidth() * (100 - zoomValue) / 100 / 2; // to min width pou mporw na exw, ekei pou dn exw allo zoom

        output = minSpaceX - startPoint;
        output *= (100.0f / (100 - zoomValue));
        if ((int) startPoint == (int) (fl.getWidth() - coursor.getX())) {
            output = -output;
        }
        return output;
    }

    private float distanceLeftForColideY() {
        float output = 0;
        View second_coursor = getActivity().findViewById(R.id.second_coursor);
        RelativeLayout fl = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen);
        View coursor = getActivity().findViewById(R.id.coursor);
//        float minDestanceX = Math.min((int) coursor.getX(), (int) (fl.getWidth() - coursor.getX()));
        float startPoint = Math.min((int) coursor.getY(), (int) (fl.getHeight() - coursor.getY()));
        float minSpaceY = fl.getHeight() * (100 - zoomValue) / 100 / 2;
        output = minSpaceY - startPoint;
        output *= (100.0f / (100 - zoomValue));
        if ((int) startPoint == (int) (fl.getHeight() - coursor.getY())) {
            output = -output;
        }
        return output;
    }

    private boolean isColideXY() {

        return isColidingX() || isColidingX();
    }

    private boolean isColidingX() {
        RelativeLayout fl = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen);
        View coursor = getActivity().findViewById(R.id.coursor);
        float minDestanceX = Math.min((int) coursor.getX(), (int) (fl.getWidth() - coursor.getX()));
        float minSpaceX = fl.getWidth() * (100 - zoomValue) / 100 / 2;

        if (minSpaceX > minDestanceX) {
            return true;
        }
        return false;
    }


    private boolean isColidingY() {
        RelativeLayout fl = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen);
        View coursor = getActivity().findViewById(R.id.coursor);
        float minDestanceY = Math.min((int) coursor.getY(), (int) (fl.getHeight() - coursor.getY()));
        float minSpaceY = fl.getHeight() * (100 - zoomValue) / 100 / 2;

        if (minSpaceY > minDestanceY) {
            return true;
        }

        return false;
    }

    private void secondCursorAlgo(MotionEvent event) {
        float moveX = event.getX() - firstTouchX;
        float moveY = event.getY() - firstTouchY;

        View second_coursor = getActivity().findViewById(R.id.second_coursor);
        View coursor = getActivity().findViewById(R.id.coursor);
        RelativeLayout fl = (RelativeLayout) getActivity().findViewById(R.id.mousepad_screen);
        float minDestanceX = Math.min((int) coursor.getX(), (int) (fl.getWidth() - coursor.getX()));
        float minDestanceY = Math.min((int) coursor.getY(), (int) (fl.getHeight() - coursor.getY()));
        float coursourX = coursor.getX() + moveX * extraMultiMouseMove;
        float coursourY = coursor.getY() + moveY * extraMultiMouseMove;

        float x = coursor.getX();
        float y = coursor.getY();
        float cropWidth = (float) (fl.getWidth() * zoomValue / 100.0), cropHeight = (float) (fl
                .getHeight() * zoomValue / 100.0), cropPosX = x
                - cropWidth / 2, cropPosY = y - cropHeight / 2;

        if (cropPosX < 0) {
            cropPosX = 0;
        }

        float minSpaceX = fl.getWidth() * (100 - zoomValue) / 100 / 2;
        if (minSpaceX > minDestanceX) {

            if (isXZoomFirstTime) {
                isXZoomFirstTime = false;
            } else {

                second_coursor.setX(second_coursor.getX() + moveX * extraMultiMouseMove * (100.0f / (100 - zoomValue))); //// check

            }
        } else {
            isXZoomFirstTime = false;

            second_coursor.setX(second_coursor.getX() + moveX * extraMultiMouseMove * (100.0f / (100 - zoomValue)));//// check


        }

        float minSpaceY = fl.getHeight() * (100 - zoomValue) / 100 / 2;
        if (minSpaceY > minDestanceY) {

            second_coursor.setY(second_coursor.getY() + moveY * extraMultiMouseMove * (100.0f / (100 - zoomValue)));//// check

        } else {
            isYZoomFirstTime = false;
            second_coursor.setY(second_coursor.getY() + moveY * extraMultiMouseMove * (100.0f / (100 - zoomValue)));

        }


        if (second_coursor.getX() < 0) {
            second_coursor.setX(0);
        }
        if (second_coursor.getY() < 0) {
            second_coursor.setY(0);
        }

        if (second_coursor.getX() > fl.getWidth()) {
            second_coursor.setX(fl.getWidth());
        }
        if (second_coursor.getY() > fl.getHeight()) {
            second_coursor.setY(fl.getHeight());
        }
    }


    public float dpToPx(float dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    static int zoomValue = 0;
    float fingerSpace = -1;
    Point[] previousPoints = null;

    private synchronized void updateZoomViewOnTrackingStop() {
        updateZoomViewTwice();
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    sleep(1700);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                updateZoomViewTwice();
//
//            }
//        }.start();
    }

    private synchronized void updateZoomViewTwiceSmall() {
        updateZoomViewWithLocation();
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    sleep(MouseUIActivity.sleepingTime);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                updateZoomViewWithLocation();
//
//            }
//        }.start();
    }

    private synchronized void updateZoomViewTwice() {
        updateZoomViewWithLocation();
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    sleep(1700);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                updateZoomViewWithLocation();
//
//            }
//        }.start();
    }

    boolean handleTouch(MotionEvent m) {
        //Number of touches
        int pointerCount = m.getPointerCount();
        int action = m.getActionMasked();
        boolean is_show_computer_mouse_seperate = MouseUIActivity.getSavedBoolean(getActivity(), MouseUIActivity.show_computer_mouse_seperateString, true);

        if (pointerCount == 2) {
            isOnTouchDown = false;
            waitUntilDraw = false;


            boolean isReverse = MouseUIActivity.getSavedBoolean(getActivity(), MouseUIActivity.isReverseString);

            boolean is_zoom_radio_on = MouseUIActivity.getSavedBoolean(getActivity(), MouseUIActivity.is_zoom_radio_on_String, false);

            boolean isScrolling = false;
            if (!is_zoom_radio_on) {
                handleScrollingUpAndDouw(m, isScrolling, isReverse);
                updateZoomViewTwice();
                return true;
            } else {
                if (action == MotionEvent.ACTION_UP) {
                    updateZoomViewOnTrackingStop();
                }
                handleTouchZoom(m);
                updateZoomViewWithLocation();

                return true;
            }

        } else if (action == MotionEvent.ACTION_POINTER_DOWN && (pointerCount == 3)) {
            is_show_computer_mouse_seperate = !is_show_computer_mouse_seperate;

            saveBoolean(MouseUIActivity.show_computer_mouse_seperateString, is_show_computer_mouse_seperate);
            MouseUIActivity.ps.println("ShowMouse:" + is_show_computer_mouse_seperate);
            MouseUIActivity.ps.flush();
        }

//        else if (action == MotionEvent.ACTION_POINTER_DOWN && (pointerCount == 4)) {
//            is_show_computer_mouse_seperate = !is_show_computer_mouse_seperate;
//            saveBoolean(MouseUIActivity.show_computer_mouse_seperateString, is_show_computer_mouse_seperate);
//            MouseUIActivity.ps.println("ShowMouse:" + is_show_computer_mouse_seperate);
//            MouseUIActivity.ps.flush();
//            toggleToolbar();
//        }
        else if (action == MotionEvent.ACTION_POINTER_DOWN && (pointerCount == 4)) {
            is_show_computer_mouse_seperate = !is_show_computer_mouse_seperate;
            saveBoolean(MouseUIActivity.show_computer_mouse_seperateString, is_show_computer_mouse_seperate);
            MouseUIActivity.ps.println("ShowMouse:" + is_show_computer_mouse_seperate);
            MouseUIActivity.ps.flush();
            toggleZoomBar();
        }

        prevSpace = 0;
        previousPoints = null;

        return false;
    }

    private void toggleToolbar() {

        android.support.v7.widget.Toolbar relativeLayout = (android.support.v7.widget.Toolbar) getActivity().findViewById(R.id.toolbar);

        if (relativeLayout.getVisibility() == View.VISIBLE) {
            relativeLayout.setVisibility(View.GONE);

        } else {
            relativeLayout.setVisibility(View.VISIBLE);

        }
    }

    private void toggleZoomBar() {

        RelativeLayout relativeLayout = (RelativeLayout) getActivity().findViewById(R.id.relativeLayout);

        View zoom_onoff = getActivity().findViewById(R.id.zoom_onoff);


        if (relativeLayout.getVisibility() == View.VISIBLE) {
            relativeLayout.setVisibility(View.GONE);
            zoom_onoff.setBackgroundResource(R.drawable.nozoom1);
        } else {
            relativeLayout.setVisibility(View.VISIBLE);
            zoom_onoff.setBackgroundResource(R.drawable.zoom1);
        }
    }

    private void saveBoolean(String id, boolean data) {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MouseUIActivity.MY_PREFS_NAME, MouseUIActivity.MODE_PRIVATE).edit();
        editor.putBoolean(id, data);
        editor.commit();
    }

    private void handleScrollingUpAndDouw(MotionEvent m, boolean isScrolling, boolean isMacMode) {
        if (previousPoints == null) {
            previousPoints = new Point[2];
            previousPoints[0] = new Point((int) m.getX(0), (int) m.getY(0));
            previousPoints[1] = new Point((int) m.getX(1), (int) m.getY(1));

        } else {
            if (previousPoints[0].y > (int) m.getY(0) &&
                    previousPoints[1].y > (int) m.getY(1)) {
                isScrolling = true;
                if (isMacMode) {
                    MouseUIActivity.ps.println("SCROLL_DOWN");
                    MouseUIActivity.ps.flush();
                } else {
                    MouseUIActivity.ps.println("SCROLL_UP");
                    MouseUIActivity.ps.flush();
                }
            }
            if (previousPoints[0].y < (int) m.getY(0) &&
                    previousPoints[1].y < (int) m.getY(1)) {
                isScrolling = true;
                if (isMacMode) {
                    MouseUIActivity.ps.println("SCROLL_UP");
                    MouseUIActivity.ps.flush();
                } else {
                    MouseUIActivity.ps.println("SCROLL_DOWN");
                    MouseUIActivity.ps.flush();
                }
            }
//            if (!isScrolling) {
////                    handleTouchZoom(m);
//            }
            previousPoints = null;
        }


    }

    float prevSpace;

    void handleTouchZoom(MotionEvent m) {
        if (prevSpace == 0) {
            prevSpace = getFingerSpacing(m);
        } else {
            float curSpace = getFingerSpacing(m);
            if (prevSpace > curSpace) {
                zoomValue++;
            } else if (prevSpace < curSpace) {
                zoomValue--;
            }
            if (zoomValue > 90) {
                zoomValue = 90;
            }
            if (zoomValue < 0) {
                zoomValue = 0;
            }

            DiscreteSeekBar sb = (DiscreteSeekBar) getActivity().findViewById(R.id.zoom_seekBar);
            TextView zoomView = (TextView) getActivity().findViewById(R.id.zoom_text);
            sb.setProgress(zoomValue);
//            MouseUIActivity.ps.println("ZOOM:" + zoomValue);
//            zoomView.setText("ZOOM: " + zoomValue + "%");
//
//            View coursor = getActivity().findViewById(R.id.coursor);
//            View second_coursor = getActivity().findViewById(R.id.second_coursor);
//            ImageView fl = (ImageView) getActivity().findViewById(R.id.mousepad_screen);
//
//            if (zoomValue != 0) {
//                float secontMousepadX = second_coursor.getX() * 100 / (100 - zoomValue);
//                float secontMousepadY = second_coursor.getY() * 100 / (100 - zoomValue);
//
//                second_coursor.setX(secontMousepadX);
//                second_coursor.setY(secontMousepadY);
//            }
            prevSpace = 0;
        }
    }


    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);


        return (float) Math.sqrt(x * x + y * y);
    }

    public float max(float f1, float f2) {
        if (f1 > f2) {
            return f1;
        }
        return f2;
    }

}

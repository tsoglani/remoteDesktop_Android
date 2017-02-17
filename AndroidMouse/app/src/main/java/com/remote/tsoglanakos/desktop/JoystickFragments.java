package com.remote.tsoglanakos.desktop;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by tsoglani on 26/11/2015.
 */
public class JoystickFragments extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.joystick, null, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateColorButtonsText();
        final Button greenButton = (Button) getActivity().findViewById(R.id.game_button_green);
        final Button redButton = (Button) getActivity().findViewById(R.id.game_button_red);
        final Button blueButton = (Button) getActivity().findViewById(R.id.game_button_blue);
        final Button yellowButton = (Button) getActivity().findViewById(R.id.game_button_yellow);
        final Button upButton = (Button) getActivity().findViewById(R.id.up);
        final Button downButton = (Button) getActivity().findViewById(R.id.down);
        final Button rightButton = (Button) getActivity().findViewById(R.id.right);
        final Button leftButton = (Button) getActivity().findViewById(R.id.left);

        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameDirectionButtonFunction(rightButton);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    rightButton.setBackgroundResource(R.drawable.down_pressed);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    rightButton.setBackgroundResource(R.drawable.down);
                }


                return true;
            }
        });
        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameDirectionButtonFunction(leftButton);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    leftButton.setBackgroundResource(R.drawable.up_pressed);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    leftButton.setBackgroundResource(R.drawable.up);
                }
                return true;
            }
        });
        upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameDirectionButtonFunction(upButton);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    upButton.setBackgroundResource(R.drawable.right_pressed);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    upButton.setBackgroundResource(R.drawable.right);
                }
                return true;
            }
        });
        downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameDirectionButtonFunction(downButton);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downButton.setBackgroundResource(R.drawable.left_pressed);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    downButton.setBackgroundResource(R.drawable.left);
                }
                return true;
            }
        });

        yellowButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameButtonFunction(yellowButton);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    yellowButton.setBackgroundResource(R.drawable.circle_drawable_purple);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    yellowButton.setBackgroundResource(R.drawable.circle_drawable_yellow);
                }
                return true;
            }
        });
        redButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameButtonFunction(redButton);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    redButton.setBackgroundResource(R.drawable.circle_drawable_purple);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    redButton.setBackgroundResource(R.drawable.circle_drawable_red);
                }
                return true;
            }
        });
        blueButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameButtonFunction(blueButton);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    blueButton.setBackgroundResource(R.drawable.circle_drawable_purple);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    blueButton.setBackgroundResource(R.drawable.circle_drawable_blue);
                }
                return true;
            }
        });
        greenButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gameButtonFunction(greenButton);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    greenButton.setBackgroundResource(R.drawable.circle_drawable_purple);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    greenButton.setBackgroundResource(R.drawable.circle_drawable_green);
                }
                return true;
            }
        });
    }

    private void updateColorButtonsText() {
        Button greenButton = (Button) getActivity().findViewById(R.id.game_button_green);
        Button redButton = (Button) getActivity().findViewById(R.id.game_button_red);
        Button blueButton = (Button) getActivity().findViewById(R.id.game_button_blue);
        Button yellowButton = (Button) getActivity().findViewById(R.id.game_button_yellow);


        String greenButtonString = getData("green", "Y");
        String redButtonString = getData("red", "X");
        String yellowButtonString = getData("yellow", "O"), blueButtonString = getData("blue", "Z");

        greenButton.setText(greenButtonString);
        redButton.setText(redButtonString);
        yellowButton.setText(yellowButtonString);
        blueButton.setText(blueButtonString);
    }

    private String getData(String id, String defaultValue) {
        SharedPreferences prefs = getActivity().getSharedPreferences(MouseUIActivity.MY_PREFS_NAME, getActivity().MODE_PRIVATE);
        String restoredText = prefs.getString(id, defaultValue);
        return restoredText;
    }


    public void gameButtonFunction(View v) {
        Button b = (Button) v;
       final String text = b.getText().toString();
        new Thread(){
            @Override
            public void run() {
                MouseUIActivity.ps.println("keyboard:" + text);


            }
        }.start();
        MouseUIActivity.ps.flush();
    }

    public void gameDirectionButtonFunction(View v) {
        Button downButton = (Button) getActivity().findViewById(R.id.down);
        Button upButton = (Button) getActivity().findViewById(R.id.up);
        Button rightButton = (Button) getActivity().findViewById(R.id.right);
        Button leftButton = (Button) getActivity().findViewById(R.id.left);

      final  String upString = getData("up", "UP");
        final  String downString = getData("down", "DOWN");
        final  String rightString = getData("right", "RIGHT"), leftString = getData("left", "LEFT");


        if (v == upButton) {
            new Thread(){
                @Override
                public void run() {
                    MouseUIActivity.ps.println("keyboard:" + upString);
                    MouseUIActivity.ps.flush();

                }
            }.start();

        } else if (v == downButton) {
            new Thread(){
                @Override
                public void run() {
                    MouseUIActivity.ps.println("keyboard:" + downString);
                    MouseUIActivity.ps.flush();

                }
            }.start();
        } else if (v == rightButton) {
            new Thread(){
                @Override
                public void run() {
                    MouseUIActivity.ps.println("keyboard:" + rightString);
                    MouseUIActivity.ps.flush();

                }
            }.start();

        } else if (v == leftButton) {
            new Thread(){
                @Override
                public void run() {
                    MouseUIActivity.ps.println("keyboard:" + leftString);
                    MouseUIActivity.ps.flush();

                }
            }.start();

        }
    }
}

package com.remote.tsoglanakos.desktop;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by tsoglani on 6/9/2015.
 */
public class CMDFragment extends android.support.v4.app.Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(com.remote.tsoglanakos.desktop.R.layout.cmd_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        RelativeLayout cmdLayout = (RelativeLayout) getActivity().findViewById(R.id.cmd_l);
        Button clr = (Button) getActivity().findViewById(R.id.clr);
        final TextView commandView = (TextView) getActivity().findViewById(R.id.cmd_layout);
        Button execute = (Button) getActivity().findViewById(R.id.execute);
        commandView.setMovementMethod(new ScrollingMovementMethod());
        final EditText command = (EditText) getActivity().findViewById(R.id.command_view);
        final CheckBox checkBox = (CheckBox) getActivity().findViewById(R.id.remember);
//        cmdLayout.setBackgroundColor(Color.DKGRAY);
//        command.setBackgroundColor(Color.LTGRAY);
//        command.setTextColor(Color.CYAN);
//        commandView.setBackgroundColor(Color.BLACK);
//        commandView.setTextColor(Color.rgb(50, 205, 50));
        clr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commandView.setText("");
            }
        });
        execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MouseUIActivity.ps.println("CommandLine:" + command.getText());
                //  commandView.setText("");
                if (!checkBox.isChecked()) {
                    commandView.setText("");
                }
                MouseUIActivity.ps.flush();
                hideKeyboard(command);
                receiveString();

            }
        });

        command.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    MouseUIActivity.ps.println("CommandLine:" + command.getText());
                    if (!checkBox.isChecked()) {
                        commandView.setText("");
                    }

                    MouseUIActivity.ps.flush();
                    receiveString();

                    return true;
                }
                return false;
            }
        });

    }

    String input = null;

    private void receiveString() {
        final EditText command = (EditText) getActivity().findViewById(R.id.command_view);
        final TextView commandView = (TextView) getActivity().findViewById(R.id.cmd_layout);

        InternetConnection ic = new InternetConnection();
        AsyncTask<Void, Void, Void> async = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    input = "";

                    ///////////////UDP receiving
                    while (!MouseUIActivity.isReceivingImages) {
                        byte[] b = new byte[1000];
                        MouseUIActivity.bf.read(b);
                        input += new String(b);
                        if (input == null || input.equalsIgnoreCase("@END@") || input.contains("@END@")) {
                            break;
                        }
                        Log.e("eeeeeeeeee", input);

                    }

                    input = input.split("@END@")[0];
                    String[] allCmdResponds = input.split(command.getText().toString() + "_Command");
                    input = "";
                    for (int i = 1; i < allCmdResponds.length; i++) {
                        input += allCmdResponds[i] + "\n";
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                final ScrollView scroll = (ScrollView) getActivity().findViewById(R.id.scrollView);
                final TextView commandView = (TextView) getActivity().findViewById(R.id.cmd_layout);

                commandView.append("- Command :: " + command.getText().toString() + ":");
                command.setText("");
                commandView.append(input + "\n");

                scroll.post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        };
        async.execute();
    }

    private void hideKeyboard(EditText editText) {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

}

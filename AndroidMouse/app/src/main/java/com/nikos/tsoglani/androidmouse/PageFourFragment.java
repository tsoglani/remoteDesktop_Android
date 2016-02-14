package com.nikos.tsoglani.androidmouse;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by tsoglani on 15/8/2015.
 */
public class PageFourFragment extends android.support.v4.app.Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.close,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button shut_down= (Button) getActivity().findViewById(R.id.shut_down);
        Button sleep= (Button) getActivity().findViewById(R.id.resolution);
        Button restart= (Button) getActivity().findViewById(R.id.restart);
        shut_down.setOnClickListener(shutDownListener);
        restart.setOnClickListener(restartListener);
        sleep.setOnClickListener(sleepListener);
    }


private void shutDown(){
    final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());



    LinearLayout linear=new LinearLayout(getActivity());

    linear.setOrientation(1);
    final TextView text=new TextView(getActivity());
    text.setText("Shut Down now");
    text.setPadding(10, 10, 10, 10);

    final SeekBar seek=new SeekBar(getActivity());
    seek.setMax(320);
    linear.addView(seek);
    linear.addView(text);

    alert.setView(linear);
seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (progress == 0) {
            text.setText("Shut Down now");
        } else {
            text.setText("Shut Down in " + progress + " min");
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
});


    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            MouseUIActivity.ps.println("SHUT_DOWN_IN:" + seek.getProgress());
            MouseUIActivity.ps.flush();
        }
    });

alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
    @Override
    public void onClick(DialogInterface dialog, int which) {

    }
});
    alert.show();
}

    View.OnClickListener shutDownListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                shutDown();
//       TextView txtView= (TextView) getActivity().findViewById(R.id.shut_down_text);
//                MouseUIActivity.ps.println(txtView.getText());
            }
    };
    View.OnClickListener restartListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            TextView txtView= (TextView) getActivity().findViewById(R.id.restart_text_view);
            MouseUIActivity.ps.println("RESTART");
            }
    };
    View.OnClickListener sleepListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            TextView txtView= (TextView) getActivity().findViewById(R.id.sleep_text_view);
            MouseUIActivity.ps.println("SLEEP");
            }
    };

}

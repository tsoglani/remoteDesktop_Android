package com.nikos.tsoglani.androidmouse;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.mingle.entity.MenuEntity;
import com.mingle.sweetpick.CustomDelegate;
import com.mingle.sweetpick.DimEffect;
import com.mingle.sweetpick.RecyclerViewDelegate;
import com.mingle.sweetpick.SweetSheet;
import com.mingle.sweetpick.ViewPagerDelegate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tsoglani on 17/7/2015.
 */
public class PageThreeFragment extends android.support.v4.app.Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(com.nikos.tsoglani.androidmouse.R.layout.keyboard, container, false);
    }


    public void onChangeView() {
        MouseUIActivity.ps.println("keyboard:CHECKBOXES_STOP");
        MouseUIActivity.ps.flush();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final EditText txt = (EditText) getActivity().findViewById(R.id.textScreen);
        txt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    EditText txtView = (EditText) v;
                    MouseUIActivity.ps.println("keyboard Word:" + txtView.getText().toString());
                    MouseUIActivity.ps.flush();
                    txtView.setText("");


                    return true;
                }
                return false;
            }
        });

//        MenuEntity min = new MenuEntity();
//        MenuEntity crl_z = new MenuEntity();
//        MenuEntity vol_up = new MenuEntity();
//        MenuEntity vol_down = new MenuEntity();
//        MenuEntity prt_Scr = new MenuEntity();
//        MenuEntity space = new MenuEntity();
//        MenuEntity f1 = new MenuEntity();
//        MenuEntity f2 = new MenuEntity();
//        MenuEntity f3 = new MenuEntity();
//        MenuEntity f4= new MenuEntity();
//        MenuEntity f5= new MenuEntity();
//        MenuEntity f6 = new MenuEntity();
//        MenuEntity f7 = new MenuEntity();
//        MenuEntity f8 = new MenuEntity();
//        MenuEntity f9= new MenuEntity();
//        MenuEntity f10= new MenuEntity();
//        MenuEntity f11= new MenuEntity();
//        MenuEntity f12= new MenuEntity();
//        MenuEntity tab= new MenuEntity();
//        MenuEntity delete= new MenuEntity();
//
//        f1.title="F1";
//        f2.title = "F2";
//        f3.title = "F3";
//        f4.title = "F4";
//        f5.title = "F5";
//        f6.title = "F6";
//        f7.title = "F7";
//        f8.title = "F8";
//        f9.title = "F9";
//        f10.title = "F10";
//        f11.title = "F11";
//        f12.title = "F12";
//        space.title = "SPACE";
//        prt_Scr.title = "PRINT SCREEN";
//        tab.title="TAB";
//        delete.title="DELETE";
//        min.title = "MIN.";
//        crl_z.title = "CRL+Z";
//        vol_up.title = "VOL. ++";
//
//        vol_down.title = "VOL. --";
//// SweetSheet 控件,根据 rl 确认位置
//        final SweetSheet mSweetSheet = new SweetSheet((RelativeLayout) getActivity().findViewById(R.id.keyboardrelative));
//
//        //  View view = LayoutInflater.from(this).inflate(R.layout.layout_custom_view, null, false);
////设置自定义视图.
//        List list = new ArrayList();
//        list.add(min);
//        list.add(delete);
//        list.add(tab);
//        list.add(f1);
//        list.add(f2);
//        list.add(f3);
//        list.add(f4);
//        list.add(f5);
//        list.add(f6);
//        list.add(f7);
//        list.add(f8);
//        list.add(f9);
//        list.add(f10);
//        list.add(f11);
//        list.add(f12);
//        list.add(space);
//        list.add(prt_Scr);
//        list.add(crl_z);
//        list.add(vol_up);
//        list.add(vol_down);
//        mSweetSheet.setMenuList(list);
//        mSweetSheet.setDelegate(new RecyclerViewDelegate(true, getActivity().getWindowManager().getDefaultDisplay().getHeight()/4));
//
//        mSweetSheet.setBackgroundClickEnable(false);
////根据设置不同Effect来设置背景效果:BlurEffect 模糊效果.DimEffect 变暗效果,NoneEffect 没有效果.
//        mSweetSheet.setBackgroundEffect(new DimEffect(8));
////设置菜单点击事件
//        mSweetSheet.setOnMenuItemClickListener(new SweetSheet.OnMenuItemClickListener() {
//            @Override
//            public boolean onItemClick(int position, MenuEntity menuEntity1) {
//
//                //根据返回值, true 会关闭 SweetSheet ,false 则不会.
//
//                MouseUIActivity.ps.println("keyboard:" + menuEntity1.title.toString());
//                MouseUIActivity.ps.flush();
//
//                return false;
//            }
//        });
//
//      final  Button b = (Button)getActivity().findViewById(R.id.show_more_button);
//        b.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(b.getText().toString().startsWith("Show")){
//                mSweetSheet.show();
//                    b.setText("Hide");
//                }
//                else{
//                    mSweetSheet.dismiss();
//                    b.setText("Show more");
//                }
//            }
//        });

        final     Spinner f_spinner= (Spinner) getActivity().findViewById(R.id.f_key);
        f_spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MouseUIActivity.ps.println("keyboard:" + toUperCase(f_spinner.getSelectedItem() + ""));
                MouseUIActivity.ps.flush();

                Toast.makeText(getActivity(), toUperCase(f_spinner.getSelectedItem() + ""), Toast.LENGTH_LONG).show();
            }
        });
        f_spinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getActivity(), toUperCase(f_spinner.getSelectedItem() + ""), Toast.LENGTH_LONG).show();
            }
        });

        final     Spinner more_spinner= (Spinner) getActivity().findViewById(R.id.more_spinner);
        f_spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MouseUIActivity.ps.println("keyboard:" + toUperCase(more_spinner.getSelectedItem() + ""));
                Toast.makeText(getActivity(), toUperCase(more_spinner.getSelectedItem() + ""), Toast.LENGTH_LONG).show();
                MouseUIActivity.ps.flush();
            }
        });

        CheckBox ctrl = (CheckBox) getActivity().findViewById(R.id.control);
        CheckBox alt = (CheckBox) getActivity().findViewById(R.id.alt);
        CheckBox shift = (CheckBox) getActivity().findViewById(R.id.shift);
        ctrl.setOnCheckedChangeListener(oncheck);
        alt.setOnCheckedChangeListener(oncheck);
        shift.setOnCheckedChangeListener(oncheck);
    }

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


    private String toUperCase(String line) {
        String output = "";
        for (char c : line.toCharArray()) {
            output += Character.toUpperCase(c);
        }
        return output;

    }

}


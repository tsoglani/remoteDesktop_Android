package com.remote.tsoglanakos.desktop;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.mingle.entity.MenuEntity;
import com.mingle.sweetpick.DimEffect;
import com.mingle.sweetpick.RecyclerViewDelegate;
import com.mingle.sweetpick.SweetSheet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by tsoglani on 22/9/2015.
 */
public class SendFragment extends android.support.v4.app.Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.send_image_layout, container, false);
    }
    SweetSheet mSweetSheet;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        Button sendImage = (Button) getActivity().findViewById(R.id.select_image_button);
//        Button sendAudio = (Button) getActivity().findViewById(R.id.select_audio_button);
//        Button sendVideo = (Button) getActivity().findViewById(R.id.select_video_button);
//        Button sendFile = (Button) getActivity().findViewById(R.id.select_file_button);
//
//
//




        MenuEntity sendImage = new MenuEntity();
        MenuEntity sendAudio = new MenuEntity();
        MenuEntity sendVideo = new MenuEntity();
        MenuEntity sendFile = new MenuEntity();
        sendImage.title="Send image";
        sendAudio.title="Send audio";
        sendVideo.title="Send video";
        sendFile.title="Send file";
// SweetSheet 控件,根据 rl 确认位置
        mSweetSheet = new SweetSheet((RelativeLayout) getActivity().findViewById(R.id.rl));
       
        //  View view = LayoutInflater.from(this).inflate(R.layout.layout_custom_view, null, false);
//设置自定义视图.

        mSweetSheet.setBackgroundClickEnable(false);
        List list = new ArrayList();
        list.add(sendImage);

        list.add(sendAudio);

        list.add(sendVideo);
        list.add(sendFile);

        mSweetSheet.setMenuList(list);
        mSweetSheet.setDelegate(new RecyclerViewDelegate(true));
//根据设置不同Effect来设置背景效果:BlurEffect 模糊效果.DimEffect 变暗效果,NoneEffect 没有效果.
        mSweetSheet.setBackgroundEffect(new DimEffect(8));
//设置菜单点击事件
        mSweetSheet.setOnMenuItemClickListener(new SweetSheet.OnMenuItemClickListener() {
            @Override
            public boolean onItemClick(int position, MenuEntity menuEntity1) {

                //根据返回值, true 会关闭 SweetSheet ,false 则不会.
                switch (menuEntity1.title.toString()){
                    case "Send image":

                                pickImage();


                        break;
                    case "Send audio":
                        pickAudio();
                        break;
                    case "Send video":
                        pickVideo();
                        break;
                    case "Send file":
                        pickFile();
                        break;

                }

                return false;
            }
        });


        mSweetSheet.show();





//
//        sendImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pickImage();
//            }
//        });
//        sendAudio.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pickAudio();
//            }
//        });
//        sendVideo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pickVideo();
//            }
//        });
//        sendFile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pickFile();
//            }
//        });

    }



    private void pickFile() {


       Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        intent.putExtra("CONTENT_TYPE", "*/*");

       intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
       startActivityForResult(Intent.createChooser(intent, "Choose a file"), 1);
    }

    private void pickVideo() {
        Intent intent_upload = new Intent();
        intent_upload.setType("video/*");
        intent_upload.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent_upload, "Choose a file"), 1);

    }


    private void pickAudio() {

        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent_upload, "Choose a file"), 1);
    }

    private void pickImage() {
        Intent intent = new Intent(getActivity(), AlbumSelectActivity.class);
//set limit on number of images that can be selected, default is 1
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 1);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }



    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {


        if (requestCode == Constants.REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            //  Toast.makeText(getActivity(), "Wait ..", Toast.LENGTH_LONG).show();
//            new Thread() {
//                @Override
//                public void run() {
//                    sendAlgorithm(data);
//                }
//            }.start();
            new AsyncTask<Void, Void, Void>() {
                ProgressDialog progress;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progress = ProgressDialog.show(getActivity(), "sending, please wait ...", "", true);
                    progress.setCancelable(false);
                    progress.show();
                }

                @Override
                protected Void doInBackground(Void... params) {

                    sendAlgorithm(data);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    progress.dismiss();
                }
            }.execute();
        } else if (requestCode == 1) {
            new AsyncTask<Void, Void, Void>() {
                ProgressDialog progress;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progress = ProgressDialog.show(getActivity(), "sending, please wait ...", "", true);
                    progress.setCancelable(false);
                    progress.show();
                }

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                protected Void doInBackground(Void... params) {
                    if (resultCode == Activity.RESULT_OK) {
                        //data.getParcelableArrayExtra(name);
                        //If Single image selected then it will fetch from Gallery
                        if (data.getData() != null) {

                            Uri mImageUri = data.getData();
                            sendAudio(mImageUri);
                        } else {
                            if (data.getClipData() != null) {
                                ClipData mClipData = data.getClipData();
                                for (int i = 0; i < mClipData.getItemCount(); i++) {

                                    ClipData.Item item = mClipData.getItemAt(i);
                                    Uri uri = item.getUri();
                                    sendAudio(uri);

                                }
                            }
                        }
                    }


                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    progress.dismiss();
                }
            }.execute();

            //the selected audio.

        }

        super. onActivityResult(requestCode, resultCode, data);

    }



    private synchronized  void sendAudio(Uri uri) {
        try {

            MouseUIActivity.ps.println("Send:" + 1);
            BufferedReader br = new BufferedReader(new InputStreamReader(InternetConnection.returnSocket.getInputStream()));
            String ready = br.readLine();
           ;
            if (!ready.equalsIgnoreCase("ok")) {
                ready = br.readLine();
                if (!ready.equalsIgnoreCase("ok"))
                return;
            }

            File f = new File(getRealPathFromURI(uri));

            send(f, br);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send(File f, BufferedReader br) throws IOException {
        // toast(f.getAbsolutePath());
        String allTitles = f.getName() + "@@@@@@";
        EditText editText = (EditText) getActivity().findViewById(R.id.folder_name);
        String folder = editText.getText().toString();
        if (folder.equalsIgnoreCase("")) {
            folder = " ";
        }
        allTitles += folder;
        MouseUIActivity.ps.println(allTitles);
        MouseUIActivity.ps.flush();
        byte[] result = getBytes(f.getAbsolutePath());
        DataOutputStream dos = new DataOutputStream(InternetConnection.returnSocket.getOutputStream());
        dos.write(result);
        dos.write("end of file".getBytes());
        dos.flush();

        String res = br.readLine();
        if (res.equalsIgnoreCase("send_over")) {
            toast("Successfull");
        } else {
            toast("Unsuccessfull");
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private String getRealPathFromPath(String uri) {
        Uri contentURI = Uri.parse(uri);
        String result;
        Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void sendAlgorithm(Intent data) {



        try {
            ArrayList<com.darsh.multipleimageselect.models.Image> imagesPaths = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);


            MouseUIActivity.ps.println("Send:" + imagesPaths.size());
            BufferedReader br = new BufferedReader(new InputStreamReader(InternetConnection.returnSocket.getInputStream()));
            String ready = br.readLine();
            if (!ready.equalsIgnoreCase("ok")) {
                ready = br.readLine();
                if (!ready.equalsIgnoreCase("ok"))
                    return;
            }
            //  toast("not returned");

            String allTitles = "";
            for (int i = 0; i < imagesPaths.size(); i++) {

                allTitles += imagesPaths.get(i).name + "@@@@@@";
            }

            EditText editText = (EditText) getActivity().findViewById(R.id.folder_name);
            String folder = editText.getText().toString();
            if (folder.equalsIgnoreCase("")) {
                folder = " ";
            }
            allTitles += folder;

            MouseUIActivity.ps.println(allTitles);
            MouseUIActivity.ps.flush();
            for (int i = 0; i < imagesPaths.size(); i++) {
                findAndSend(imagesPaths.get(i).path);
            }
            String result = br.readLine();
            if (result.equalsIgnoreCase("send_over")) {
                toast("Successfull");
            } else {
                toast("Unsuccessfull");
            }
        } catch (Exception e) {
e.printStackTrace();
        }
    }

    private void toast(final String s) {
        getActivity().runOnUiThread(new Thread() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
            }
        });
    }

    private byte[] getBytes(Bitmap b) {

//calculate how many bytes our image consists of.
        int bytes = b.getByteCount();
//or we can calculate bytes this way. Use a different value than 4 if you don't use 32bit images.
//int bytes = b.getWidth()*b.getHeight()*4;

        ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
        b.copyPixelsToBuffer(buffer); //Move the byte data to the buffer

        byte[] array = buffer.array();
        return array;
    }

    public byte[] getBytes(String selectedImagePath) throws IOException {
        File myFile = new File(selectedImagePath);
        int filelenghth = (int) myFile.length();
        byte[] mybytearray = new byte[filelenghth];
        DataInputStream bis1 = new DataInputStream(new FileInputStream(myFile));

        bis1.read(mybytearray, 0, mybytearray.length);
        return mybytearray;

    }

    private void findAndSend(String imagesPaths) {
        try {
            DataOutputStream dos = new DataOutputStream(InternetConnection.returnSocket.getOutputStream());


            File imgFile = new File(imagesPaths);

            if (imgFile.exists()) {

                final Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());


                //  byte[] imageBytes = getBytes(myBitmap);
                // MouseUIActivity.ps.println("size:"+imageBytes.length);
                //    MouseUIActivity.ps.flush();
//
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] b = baos.toByteArray();
                dos.write(b);
                // dos.flush();
                dos.write("end of file".getBytes());
                dos.flush();


                // toast(Integer.toString(imageBytes.length));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}

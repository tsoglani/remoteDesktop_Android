package com.remote.tsoglanakos.desktop;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.felipecsl.gifimageview.library.GifImageView;

import java.io.InputStream;


/**
 * Created by tsoglani on 11/9/2015.
 */
public class MicFragment extends android.support.v4.app.Fragment {


    private final int frequency = 16000;
    private final int channelConfiguration = AudioFormat.CHANNEL_OUT_MONO;
    private final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private boolean isPlaying;
    private Thread thread;
    private GifImageView gifView;
    private AsyncTask<Void, Void, Void> asyncTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mic_layout, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gifView = (GifImageView) getActivity().findViewById(R.id.gifImageView);
        try {
            gifView.setBytes(getData("gif_waves/wave5.gif"));
            gifView.startAnimation();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public byte[] getData(String inFile) throws Exception {
        InputStream stream = getActivity().getAssets().open(inFile);
        int size = stream.available();
        byte[] buffer = new byte[size];
        stream.read(buffer);
        stream.close();
        return buffer;

    }


    synchronized void start() {
        isPlaying = true;


        int playBufSize;
        final AudioTrack audioTrack;
        playBufSize = AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM);
        if (thread != null && thread.isAlive()) {
            return;
        }
        asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                new Thread(){
                    @Override
                    public void run() {
                        MouseUIActivity.ps.println("START_AUDIO_RECORDING");
                        MouseUIActivity.ps.flush();

                    }
                }.start();
            }

            @Override
            protected Void doInBackground(Void... params) {
                thread = new Thread() {

                    public void run() {


                        try {
                            //   InternetConnection.returnSocket.getInputStream().skip(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //  int readSize = 0;
                        try {

                            //   short[] sbuffer = new short[1000];
                            while (isPlaying) {
                                byte[] buffer = new byte[1000];


                                //  readSize =

                                InternetConnection.returnSocket.getInputStream().read(buffer);

                                audioTrack.write(buffer, 0, buffer.length);
                                audioTrack.flush();
                                audioTrack.play();

                                //

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } catch (Error e) {

                        }
                    }

                };
                thread.start();
                return null;
            }


        };
        asyncTask.execute();


    }

    @Override
    public void onPause() {
        super.onPause();
        if (gifView != null && gifView.isAnimating())
            gifView.stopAnimation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (gifView != null && gifView.isAnimating())
            gifView.stopAnimation();
    }


    public int getFrequency() {
        return frequency;
    }

    public int getChannelConfiguration() {
        return channelConfiguration;
    }

    public int getAudioEncoding() {
        return audioEncoding;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public GifImageView getGifView() {
        return gifView;
    }

    public void setGifView(GifImageView gifView) {
        this.gifView = gifView;
    }

    public AsyncTask<Void, Void, Void> getAsyncTask() {
        return asyncTask;
    }

    public void setAsyncTask(AsyncTask<Void, Void, Void> asyncTask) {
        this.asyncTask = asyncTask;
    }
}

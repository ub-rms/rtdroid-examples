package edu.buffalo.rtdroid.sound;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import edu.buffalo.rtdroid.app.ComponentManager;
import edu.buffalo.rtdroid.app.RealtimeService;
import edu.buffalo.rtdroid.app.Repeatable;
import edu.buffalo.rtdroid.content.RealtimeIntent;
import edu.buffalo.rtdroid.content.RealtimeReceiver;
import edu.buffalo.rtdroid.os.RealtimeMessage;
import edu.buffalo.rtdroid.os.RealtimeMsgPassingChannel;

import java.nio.ByteBuffer;

import java.lang.System;
import java.util.Arrays;

import java.io.File;
import java.io.FileInputStream;
import java.lang.*;

public class AudioTrack extends RealtimeService {
    public static final int bufferSize = 960;
    private track_loop track1;
    private static track_loop track2;
    private static boolean[] release = {true, false};

    int numThreads = 15;
    private track1_loop[] tracks = new track1_loop[numThreads];
    private int[] releaseCount = new int[numThreads];


    int track3_frames = 1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        System.out.println("RecorderService onCreate...");
        long startTime = System.nanoTime();
        System.out.println("Start Time: " + startTime);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("RecorderService onStartCommand...");

        //Sync signal
        track1 = new track_loop("/sdcard/20000.wav", 0);
//        registerPeriodicTask("sync_signal", track1);
        //Ack signal device 1
//        track2 = new track_loop("/sdcard/signal.wav", 1);
//        registerPeriodicTask("sync_reply", track2);
        //Ack signal device 2
//        track2 = new track_loop("/sdcard/19000.wav", 1);
//        registerPeriodicTask("track2_loop", track2);

//        Multiple stream tests
//        releaseCount[0] = 3000;
//        for(int i=1;i<numThreads; i++){
//            releaseCount[i] = 1000;
//        }

/*        for (int i = 0; i < numThreads; i++) {
            tracks[i] = new track1_loop(i, releaseCount[i]);
            registerPeriodicTask("track" + (i + 1) + "_loop", tracks[i]);
        }*/
//        registerPeriodicTask("track1_loop", tracks[0]);


        return 0;
    }

    @Override
    public void onDestroy() {
        System.out.println("RecorderService onDestroy...");
    }

    @Override
    public void onPause(Intent intent) {
        System.out.println("RecorderService onDestroy...");
    }

    private class track_loop implements Repeatable {
        int track_frames = 1;
        int tracknum;
        FileInputStream fs;

        public track_loop(String path, int num) {
            tracknum = num;
            try {
                fs = new FileInputStream(new File(path));
            } catch (java.io.IOException e) {
                System.out.println("Open failed");
            }
        }

        @Override
        public void onRelease(Context context) {
            //read from device
            RealtimeMsgPassingChannel channel = (RealtimeMsgPassingChannel) ComponentManager.getInstance().
                    getChannels().get("msg.buf.handler");
            if (release[tracknum]) {
                try {
                    if (fs.available() > 0 && track_frames < 1500) {
                        RealtimeMessage msg = channel.obtain();
                        msg.setWhat(AudioManager.MESSAGE_AUDIO_SAMPLE);
                        msg.setSender("track" + tracknum);
                        byte[] buffer = new byte[bufferSize];
                        fs.read(buffer);
                        msg.getByteBuffer().clear();
                        msg.getByteBuffer().put(buffer);

                        channel.enqueue(msg);
                        track_frames++;
                    } else {
                        RealtimeMessage msg = channel.obtain();
                        msg.setWhat(AudioManager.MESSAGE_AUDIO_CLOSE);
                        channel.enqueue(msg);
                    }
                } catch (java.io.IOException e) {
                    System.out.println("Exception!");
                }
                release[tracknum] = false;
            }
        }
    }

    private class track1_loop implements Repeatable {
        private int tracknum, numReleases, track_frames;

        public track1_loop(int num, int releases) {
            tracknum = num;
            numReleases = releases;
            track_frames = 0;
            System.out.println("Created " + tracknum + " with " + numReleases);
        }

        @Override
        public void onRelease(Context context) {
            //read from
            if (track_frames < numReleases) {
                System.out.println("EXPw:track" + tracknum + " " + System.nanoTime());
                RealtimeMsgPassingChannel channel = (RealtimeMsgPassingChannel) ComponentManager.getInstance().
                        getChannels().get("msg.buf.handler");
                // int * 1792
                RealtimeMessage msg = channel.obtain();
                msg.setWhat(AudioManager.MESSAGE_AUDIO_SAMPLE);
                msg.setSender("track" + tracknum);
                byte[] buffer = new byte[bufferSize];
                Arrays.fill(buffer, (byte) 0x12);
                msg.getByteBuffer().clear();
                msg.getByteBuffer().put(buffer);
                channel.enqueue(msg);
                track_frames++;
            } else {
                if (tracknum == 0) {
                    System.exit(0);
                }
            }
        }
    }


}

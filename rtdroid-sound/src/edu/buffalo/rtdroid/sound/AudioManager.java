package edu.buffalo.rtdroid.sound;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import edu.buffalo.rtdroid.app.RealtimeService;
import edu.buffalo.rtdroid.app.Repeatable;
import edu.buffalo.rtdroid.content.RealtimeIntent;
import edu.buffalo.rtdroid.os.RealtimeHandler;
import edu.buffalo.rtdroid.os.RealtimeMessage;
import com.fiji.fivm.r1.fivmRuntime;
import com.fiji.fivm.r1.Import;
import com.fiji.fivm.r1.Pointer;

import java.nio.ByteBuffer;

import java.lang.System;
import java.util.*;

import java.io.File;
import java.io.FileOutputStream;

public class AudioManager extends RealtimeService {

    public int bufferSize = 960;
    private boolean ready = false;
    private int[] resBuffer = new int[bufferSize];
    private byte[] resBufferShorts = new byte[bufferSize];
    private int mNumberOfStreams = 5;
    private MessageBufferHandler handler = new MessageBufferHandler("msg.buf.handler");
    private Object lock = new Object();
    private final short PCM_MAX_VALUE = 32767;
    private final short PCM_MIN_VALUE = -32768;
    private final int SIGNAL_SAMPLE_RATE = 44100;

    public static final int MESSAGE_AUDIO_SAMPLE = 1;
    public static final int MESSAGE_AUDIO_CLOSE = 2;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        System.out.println("AudioManager onCreate...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("AudioManager onStartCommand...");

        registerRealtimeHandler("msg.buf.handler", handler);
        registerPeriodicTask("playbackThread", new PlaybackThread());
        System.out.println("Start Time Manager: " + System.nanoTime());
        Pointer p = fivmRuntime.getCString("PCM Init");
        registerPeriodicTask("recordThread", new RecordThread());
//        pcmNativeOpen(p);
//        pcmNativeRecOpen(p);
        return 0;
    }

    @Override
    public void onPause(Intent intent) {
        System.out.println("AudioManager onPause...");
    }

    @Override
    public void onDestroy() {
    }

    private class MessageBufferHandler extends RealtimeHandler {

        public MessageBufferHandler(String name) {
            super(name);
        }

        @Override
        public void handleMessage(RealtimeMessage msg) {
            switch (msg.getWhat()) {
                case MESSAGE_AUDIO_SAMPLE:
                    ByteBuffer stream = msg.getByteBuffer();
                    stream.flip();
                    byte[] audioData = new byte[bufferSize];
                    stream.get(audioData);
                    stream.flip();
                    synchronized (lock) {
                        if (ready == false) {
                            ready = true;
                            System.arraycopy(audioData, 0, resBufferShorts, 0, bufferSize);
                        } else {
//                            //Single buffer already present mix additional streams.
                            for (int i = 0; i < bufferSize; i = i + 2) {
                                short msb = (short) resBufferShorts[i + 1];
                                short lsb = (short) resBufferShorts[i];
                                short resB = audioByteToShort(msb, lsb);
                                short audD = audioByteToShort((short) audioData[i + 1], (short) audioData[i]);
                                if (audD > 0 && resB > 0) {
                                    resB = (short) ((audD + resB) - ((audD * resB) / PCM_MAX_VALUE));
                                } else if (audD < 0 && resB < 0) {
                                    resB = (short) ((audD + resB) - ((audD * resB) / PCM_MIN_VALUE));
                                } else {
                                    resB = (short) (resB + audD);
                                }
                                resBufferShorts[i] = (byte) (resB & 0xff);
                                resBufferShorts[i + 1] = (byte) ((resB & 0xff00) >> 8);
                            } //End for
                        }   //End if-else block for mixing
                    }   //Sync end
                    break;
                case MESSAGE_AUDIO_CLOSE:
                    pcmNativeClose();
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }   //End handleMessage

        byte[] audioShortToByte(short[] s, int size) {
            byte[] result = new byte[size * 2];
            for (int i = 0; i < size; i++) {
                result[2 * i] = (byte) (s[i] & 0xff);
                result[2 * i + 1] = (byte) ((s[i] & 0xff00) >> 8);
            }
            return result;
        }
    }

    private class PlaybackThread implements Repeatable {
        @Override
        public void onRelease(Context context) {
            synchronized (lock) {
                //Pass buffer to PCM card if there has been a write else write 0s
                if (!ready) {
                    //Buffer padding
                    Arrays.fill(resBufferShorts, (byte) 0);
//                    System.out.println("PCM: fill 0 " + System.nanoTime());
                } else {
                    //Convert array of ints to array of shorts
                    System.out.println("EXP:PCM " + System.nanoTime());
                    ready = false;
                }
//                System.out.println("LogSt: " + System.nanoTime());
                Pointer p = fivmRuntime.getByteElements(resBufferShorts);
                pcmNativeWrite(p);
//                System.out.println("LogEnd: " + System.nanoTime());
            }
        }
    }

    private class RecordThread implements Repeatable {
        FileOutputStream fs;

        public RecordThread() {
            try {
                fs = new FileOutputStream(new File("/sdcard/file.wav"));
            } catch (java.io.IOException e) {
                System.out.println("Open failed");
            }
        }

        @Override
        public void onRelease(Context context) {
            synchronized (lock) {
                try {
                    byte[] audRec = new byte[4096];
                    Pointer data = pcmNativeRead();
                    fivmRuntime.returnByteElements(audRec, data, 1);
                    fs.write(audRec);
                    double power1 = checkFrequencyPower(audRec, 20000, 4096);
                    double power2 = checkFrequencyPower(audRec, 18000, 4096);
                    int signal = 0;
                    if (power1 > 0.05) {
                        System.out.println("Device1 " + System.nanoTime());
                        RealtimeIntent intent = new RealtimeIntent("sound.record.callback");
                        int[] arr = intent.getInts();
                        sendRealtimeBroadcast(intent);
                    }
                    if (power2 > 0.05) {
                        //Signal device 2.
//                        System.out.println("Device2 " + System.nanoTime());
//                        AudioTrack.recordCallback(0);
                        RealtimeIntent intent = new RealtimeIntent("sound.record.callback");

//                        intent.getByteBuffer().clear();
//                        intent.getByteBuffer().putInt(1);
                        sendRealtimeBroadcast(intent);
                    }
                } catch (java.io.IOException e) {
                    System.out.println("Exception!");
                }
            }
        }
    }

    /*   private double checkFrequencyPower(byte[] sample, int frequency, int size){
           double Skn, Skn1, Skn2;
           int samplerate = 44100;
           Skn = Skn1 = Skn2 = 0;
           for (int i = 0; i < size; i=i+2) {
               Skn2 = Skn1;
               Skn1 = Skn;
               double val = (double)audioByteToShort((short) sample[i], (short) sample[i+1]);
               Skn = 2 * Math.cos(2 * Math.PI * frequency / samplerate) * Skn1 - Skn2 + val;
           }
           double WNk = Math.exp(-2 * Math.PI * frequency / samplerate);
           return 20 * Math.log10(Math.abs((Skn - WNk * Skn1)));
       }*/
    private double checkFrequencyPower(byte[] sample, int frequency, int size) {
        try {
            float[] powers = new float[size];
            double power = 0.0;
            for (int i = 0; i < size; i = i + 2) {
                short audD = audioByteToShort((short) sample[i + 1], (short) sample[i]);
//                short audD = (short) 50;
                float val = ((float) audD) / (float) 32768;
                if (val > 1) val = 1;
                if (val < -1) val = -1;
                powers[i / 2] = val;
            }
            double sPrev = 0.0;
            double sPrev2 = 0.0;
            double normalizedfreq = (double) frequency / (double) SIGNAL_SAMPLE_RATE;
            double coeff = 2 * Math.cos(2 * Math.PI * normalizedfreq);
            for (int i = 0; i < size / 2; i++) {
                double s = powers[i] + coeff * sPrev - sPrev2;
                sPrev2 = sPrev;
                sPrev = s;
            }
            power = sPrev2 * sPrev2 + sPrev * sPrev - coeff * sPrev * sPrev2;
//        System.out.println("Power " + power);
            return power;
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace(System.out);
            return (double) 0.0;
        }
    }


    short audioByteToShort(short msb, short lsb) {
        return (short) ((msb << 8) | (lsb & 0xff));
    }

    @Import
    private static native int pcmNativeWrite(Pointer p);

    @Import
    private static native int pcmNativeOpen(Pointer p);

    @Import
    private static native void pcmNativeRecOpen(Pointer p);

    @Import
    private static native void pcmNativeCloseRec();

    @Import
    private static native Pointer pcmNativeRead();

    @Import
    private static native void pcmNativeClose();
}
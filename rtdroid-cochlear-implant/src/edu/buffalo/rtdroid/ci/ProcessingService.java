package edu.buffalo.rtdroid.ci;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import edu.buffalo.rtdroid.FFTHelper.*;
import edu.buffalo.rtdroid.app.RealtimeService;
import edu.buffalo.rtdroid.app.Repeatable;
import edu.buffalo.rtdroid.content.RealtimeIntent;
import edu.buffalo.rtdroid.os.RealtimeHandler;
import edu.buffalo.rtdroid.os.RealtimeMessage;
import java.util.*;

public class ProcessingService extends RealtimeService {
    public static final int MESSAGE_AUDIO_SAMPLE = 1;
    public static final int MESSAGE_CONFIG_CHANGE = 2;
    public static final int NUM_CHANNELS = 22;
    private static final int windowSize = 128;

    private int volume = 40;
    private double window[] = new double[windowSize];
    private boolean ready = false;
    private int prcIndex = 0;
    private int num = RecorderService.bufferSize / windowSize;
    private int[][] tmpArray = new int[num][windowSize];
    private FFT fftClass = new FFT(128);
    private MessageBufferHandler handler = new MessageBufferHandler("msg.buf.handler");;
    private ProcessTask processTask = new ProcessTask();
    private Object lock = new Object();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        System.out.println("ProcessingService onCreate...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("ProcessingService onStartCommand...");
        executeInPersistent(new Runnable() {
            @Override
            public void run() {
                for(int n=0;n<windowSize; n++) {
                    window[n] = 0.49656 * Math.cos((2 * Math.PI * n) / (windowSize - 1))
                            + 0.076849 * Math.cos((4 * Math.PI * n) / (windowSize - 1));
                }
            }
        });
        registerRealtimeHandler("msg.buf.handler", handler);
        registerPeriodicTask("processTask", processTask);
        return 0;
    }

    @Override
    public void onPause(Intent intent) {
        System.out.println("ProcessingService onPause...");
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
            switch (msg.getWhat()){
                case MESSAGE_AUDIO_SAMPLE:
                    int[] audioData = msg.getInts();
                    synchronized (lock) {
                    for (int i = 0; i < num; i++) {
                    //Object src, int srcPos, Object dest, int destPos, int length
                    System.arraycopy(audioData, i * windowSize, tmpArray[i], 0, windowSize);
                    }
                    ready = true;
                    prcIndex = 0;
                    }
                    break;
                case MESSAGE_CONFIG_CHANGE:
                    //TODO ...
                    break;
                default:
            }
        }
    }

    private class ProcessTask implements Repeatable{

        @Override
        public void onRelease(Context context) {
            //config msg change checking
            synchronized (lock){
                if( ready && prcIndex < num ){
                    long timeProcStart = System.nanoTime();
                    int[] tmp = tmpArray[prcIndex];
                    double[] fftReal = new double[windowSize];
                    double[] fftIm = new double[windowSize];
                    double[] scaledValues = new double[windowSize];
                    ArrayList<Double> channelMagnitude = new ArrayList<Double>();

                    //Apply volume
                    for(int i=0; i<tmp.length; i++){
                        double volMultiplier = (double)volume/10;
                        tmp[i] = (int) (volMultiplier * tmp[i]);
                    }
                    //Blackman window
                    for(int n=0; n<windowSize; n++){
                        if(n<tmp.length) {
                            scaledValues[n] = ((double) tmp[n] / Short.MAX_VALUE);
                            scaledValues[n] = scaledValues[n] * window[n];
                        }
                        else
                            scaledValues[n] = 0.0;
                    }

                    //FFT
                    for(int i=0; i<windowSize; i++){
                        fftIm[i] = 0.0;
                        if(i<tmp.length) {
                            fftReal[i] = scaledValues[i];
                        }
                        else {
                            fftReal[i] = 0.0d;
                        }
                    }
                    long timeFFTStart= System.nanoTime();
                    fftClass.fft(fftReal, fftIm);
                    long timeFFTEnd = System.nanoTime();

                    //Bandpass filter
                    for(int channels = 0; channels<NUM_CHANNELS; channels++){
                        //8000hz max freq, 22 channels each channel has 8000/22 = 364hz
                        // 7.8125 (8000/1024) hz per bin, number of bins for 364 hz = 47
                        double magnitude = 0.0;
                        for(int bin = channels*47; bin<(channels+1)*47 && bin<(windowSize/2); bin++){
                            magnitude+= (fftReal[bin]*fftReal[bin]) + (fftIm[bin]*fftIm[bin]);
                        }
                        magnitude = Math.sqrt(magnitude);
                        channelMagnitude.add(magnitude);
                    }

                    Collections.sort(channelMagnitude);
                    //22 doubles + 4 timestamp --> bytes tmp
                    RealtimeIntent intent = new RealtimeIntent("audio.output");
                    intent.getByteBuffer().clear();
                    for( double d : channelMagnitude ){
                        intent.getByteBuffer().putDouble(d);
                    }
                    long timeProcEnd = System.nanoTime();

                    long[] times = new long[4];
                    times[0] = timeProcStart;
                    times[1] = timeFFTStart;
                    times[2] = timeFFTEnd;
                    times[3] = timeProcEnd;

                    for(long l : times){
                        intent.getByteBuffer().putLong(l);
                    }
                    sendRealtimeBroadcast(intent);
                }
                prcIndex++;
            }
        }
    }
}


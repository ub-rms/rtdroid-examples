package edu.buffalo.rtdroid.demo;

import android.content.Intent;
import android.os.IBinder;
import edu.buffalo.rtdroid.app.ComponentManager;
import edu.buffalo.rtdroid.app.RealtimeService;
import edu.buffalo.rtdroid.content.RealtimeIntent;
import edu.buffalo.rtdroid.os.BroadcastChannel;
import edu.buffalo.rtdroid.os.RealtimeMessage;
import edu.buffalo.rtdroid.os.RealtimeMsgPassingChannel;
import edu.buffalo.rtdroid.util.experiments.MicrobenchmarkConf;

import javax.realtime.*;

public class NoisyService extends RealtimeService{
    public static final int NoisyThreadPriority = 11;
    public static final long NoisyThreadPeriodic = 200; //ms

    public enum NOISY_TYPE {
        COMPUTATIONAL, GARBAGE_PRESSURE, MULTI_MSG_SENDERS, MULTI_INTENT_SENDER;

        public static NoisyService.NOISY_TYPE getEnum(String val) {
            if (val.equals("COMP")) {
                return COMPUTATIONAL;
            } else if (val.equals("GC")) {
                return GARBAGE_PRESSURE;
            } else if (val.equals("MULTI_MSG")) {
                return MULTI_MSG_SENDERS;
            }else if(val.equals("MULTI_INTENT")) {
                return MULTI_INTENT_SENDER;
            } else {
                throw new IllegalArgumentException("illegal noisy type:" + val);
            }
        }
    }

    private NOISY_TYPE type;
    private int numNoise;
    private RealtimeThread[] noisyThreads;
    private RealtimeMsgPassingChannel msgChannel;
    private BroadcastChannel brChannel;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        System.out.println("NoisyService onCreate()...");
        type = NOISY_TYPE.getEnum(MicrobenchmarkConf.noisyType);
        numNoise = MicrobenchmarkConf.noisyNum;
        msgChannel = (RealtimeMsgPassingChannel) ComponentManager.getInstance().
                getChannels().get(MsgPrcService.RT_MSG_CHANNEL);
        brChannel = (BroadcastChannel) ComponentManager.getInstance().
                getChannels().get(IntentRcvr.RT_BROADCAST_CHANNEL);
        if( numNoise >0 ){
            noisyThreads = new RealtimeThread[numNoise];
            for( int i=0; i<numNoise; i++ ){
                noisyThreads[i] = new RealtimeThread(new NoisyLogic());
                noisyThreads[i].setSchedulingParameters(
                        new PriorityParameters(NoisyThreadPriority));
                noisyThreads[i].setReleaseParameters(
                        new PeriodicParameters(new RelativeTime(NoisyThreadPeriodic, 0)));
            }
        }
    }                                                                           

    public int onStartCommand(Intent intent, int flags, int startId) {          
        System.out.println("NoisyService onStartCommand()...");
        if( numNoise >0 ) {
            for (int i = 0; i < numNoise; i++) {
                noisyThreads[i].start();
            }
        }
        return 0;
    }

    @Override
    public void onPause(Intent intent) {
        System.out.println("PeriodicService onPause()...");
    }

    private class NoisyLogic implements Runnable {

        @Override
        public void run() {
            do{
                switch (type){
                    case COMPUTATIONAL:
                        //Gregory-Leibniz series for 2000 iterations
                        double pi = 0.0f;
                        double denominator = 1;
                        int count = 2000;
                        for (int x = 0; x < count; x++) {
                            if (x % 2 == 0) {
                                pi = pi + (1 / denominator);
                            } else {
                                pi = pi - (1 / denominator);
                            }
                            denominator = denominator + 2;
                        }
                        pi = pi * 4;
                        break;
                    case GARBAGE_PRESSURE:
                        //allocate 512 * 4 bytes
                        int[] localArrary = new int[512];
                        for (int i=0; i<localArrary.length; i++)
                            localArrary[i] = 100;
                    break;
                    case MULTI_MSG_SENDERS:
                        if( msgChannel != null ){
                            RealtimeMessage msg = msgChannel.obtain();
                            msg.setWhat(MsgPrcService.NOISY_MSG);
                            msg.getLongs()[0]=System.nanoTime();
                            msgChannel.enqueue(msg);
                        }
                    break;
                    case MULTI_INTENT_SENDER:
                        RealtimeIntent rcvrIntent = new RealtimeIntent(
                                IntentRcvr.RT_BROADCAST_CHANNEL);
                        rcvrIntent.getByteBuffer().putInt(0);
                        sendRealtimeBroadcast(rcvrIntent);
                        break;
                }
            }while(RealtimeThread.currentRealtimeThread().waitForNextPeriod());
        }
    }
}

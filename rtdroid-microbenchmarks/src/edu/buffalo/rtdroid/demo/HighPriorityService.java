package edu.buffalo.rtdroid.demo;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import edu.buffalo.rtdroid.app.ComponentManager;
import edu.buffalo.rtdroid.app.RealtimeService;
import edu.buffalo.rtdroid.app.Repeatable;
import edu.buffalo.rtdroid.content.RealtimeIntent;
import edu.buffalo.rtdroid.os.BroadcastChannel;
import edu.buffalo.rtdroid.os.RealtimeMessage;
import edu.buffalo.rtdroid.os.RealtimeMsgPassingChannel;
import edu.buffalo.rtdroid.util.experiments.MicrobenchmarkConf;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;

public class HighPriorityService extends RealtimeService{
    public enum EXP_TYPE {
        RT_MSG, RT_INTENT;

        public static HighPriorityService.EXP_TYPE getEnum(String val) {
            if (val.equals("RT_MSG")) {
                return RT_MSG;
            } else if (val.equals("RT_INTENT")) {
                return RT_INTENT;
            } else {
                throw new IllegalArgumentException("illegal exp val:" + val);
            }
        }
    }

    private EXP_TYPE type;
    private RealtimeMsgPassingChannel msgChannel;
    private BroadcastChannel brChannel;

    Repeatable task001 = new Repeatable() {
        @Override
        public void onRelease(Context context) {
            switch (type){
                case RT_MSG:
                    RealtimeMessage msg = msgChannel.obtain();
                    msg.setWhat(MsgPrcService.RT_HIGHT_MSG);
                    msg.getLongs()[0] = System.nanoTime();
                    msgChannel.enqueue(msg);
                    break;
                case RT_INTENT:
                    long start = System.nanoTime();
                    RealtimeIntent rcvrIntent = new RealtimeIntent(
                            IntentRcvr.RT_BROADCAST_CHANNEL);
                    rcvrIntent.getByteBuffer().putInt(1);
                    rcvrIntent.getByteBuffer().putLong(start);
                    sendRealtimeBroadcast(rcvrIntent);
                    break;
            }
        }
    };

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        System.out.println("HighPriorityService onCreate()...");
        type = EXP_TYPE.getEnum(MicrobenchmarkConf.expType);
        msgChannel = (RealtimeMsgPassingChannel) ComponentManager.getInstance().
                getChannels().get(MsgPrcService.RT_MSG_CHANNEL);
        brChannel = (BroadcastChannel) ComponentManager.getInstance().
                getChannels().get(IntentRcvr.RT_BROADCAST_CHANNEL);
    }                                                                           

    public int onStartCommand(Intent intent, int flags, int startId) {          
        System.out.println("HighPriorityService onStartCommand()...");
        registerPeriodicTask("task001", task001);
        return 0;
    }

    @Override
    public void onPause(Intent intent) {
        System.out.println("PeriodicService onPause()...");
    }
}

package edu.buffalo.rtdroid.demo;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import edu.buffalo.rtdroid.app.ComponentManager;
import edu.buffalo.rtdroid.app.RealtimeService;
import edu.buffalo.rtdroid.app.Repeatable;
import edu.buffalo.rtdroid.content.BulkMsgClosure;
import edu.buffalo.rtdroid.content.RealtimeIntent;
import edu.buffalo.rtdroid.os.BroadcastChannel;
import edu.buffalo.rtdroid.os.RealtimeMessage;
import edu.buffalo.rtdroid.os.RealtimeMsgPassingChannel;
import edu.buffalo.rtdroid.util.experiments.MicrobenchmarkConf;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;

public class BulkIntentSender extends RealtimeService{

    Repeatable task001 = new Repeatable() {
        @Override
        public void onRelease(Context context) {
            BulkMsgClosure closure = new BulkMsgClosure() {

                @Override
                public RealtimeIntent allocateRealtimeIntent(RealtimeIntent intent) {
                    intent.getByteBuffer().clear();
                    intent.getByteBuffer().putLong(System.nanoTime());
                    return intent;
                }

                @Override
                public String getAction() {
                    return BulkIntentRcvr.RT_BROADCAST_CHANNEL;
                }
            };

            sendRealtimeBroadcast(closure);
        }
    };

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        System.out.println("BulkIntentSender onCreate()...");
    }                                                                           

    public int onStartCommand(Intent intent, int flags, int startId) {          
        System.out.println("BulkIntentSender onStartCommand()...");
        registerPeriodicTask("task001", task001);
        return 0;
    }

    @Override
    public void onPause(Intent intent) {
        System.out.println("BulkIntentSender onPause()...");
    }
}

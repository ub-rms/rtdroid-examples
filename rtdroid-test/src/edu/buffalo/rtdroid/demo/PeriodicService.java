package edu.buffalo.rtdroid.demo;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import edu.buffalo.rtdroid.app.ComponentManager;
import edu.buffalo.rtdroid.app.RealtimeService;
import edu.buffalo.rtdroid.app.Repeatable;
import edu.buffalo.rtdroid.content.RealtimeIntent;
import edu.buffalo.rtdroid.content.RealtimeReceiver;
import edu.buffalo.rtdroid.os.RealtimeHandler;
import edu.buffalo.rtdroid.os.RealtimeMessage;
import edu.buffalo.rtdroid.os.RealtimeMsgPassingChannel;

import javax.realtime.MemoryArea;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

/**
 * Created by gassa on 2/18/16.
 */
public class PeriodicService extends RealtimeService{
    
    RealtimeReceiver rcvr = new RealtimeReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("PeriodicService internal receiver onReceive()...");
        }
    };

    Repeatable task001 = new Repeatable() {
        @Override
        public void onRelease(Context context) {
            System.out.println("periodic task 001");
            RealtimeMsgPassingChannel channel = (RealtimeMsgPassingChannel)
                    ComponentManager.getInstance().getChannels().get("handler001");
            RealtimeMessage msg = channel.obtain();
            channel.enqueue(msg);
        }
    };

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        registerRealtimeReceiver("edu.buffalo.rtdroid.action99", rcvr);
        registerPeriodicTask("task001", task001);
        System.out.println("PeriodicService onStart()...");               
    }                                                                           

    public int onStartCommand(Intent intent, int flags, int startId) {          
        System.out.println("PeriodicService onStartCommand()...");
        //((ScopedMemory) RealtimeThread.getCurrentMemoryArea()).dumpTypeDisplay();
        return 0;
    }

    @Override
    public void onPause(Intent intent) {
        System.out.println("PeriodicService onPause()...");
    }
}

package edu.buffalo.rtdroid.demo;

import android.content.Intent;
import android.os.IBinder;
import edu.buffalo.rtdroid.app.RealtimeService;
import edu.buffalo.rtdroid.content.RealtimeIntent;
import edu.buffalo.rtdroid.os.RealtimeHandler;
import edu.buffalo.rtdroid.os.RealtimeMessage;

import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

/**
 * Created by gassa on 2/18/16.
 */
public class SingleRealtimeService extends RealtimeService {

    public IBinder onBind(Intent intent) {
        return null;
    }

    private RealtimeHandler handler = new RealtimeHandler("handler001") {
        @Override
        public void handleMessage(RealtimeMessage msg) {
            System.out.println("msg handling ...");
        }
    };

    public void onCreate() {
        System.out.println("SingleRealtimeService onCreate()...");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("SingleRealtimeService onStartCommand()...");
        registerRealtimeHandler("handler001", handler);
        //((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).dumpTypeDisplay();
        RealtimeIntent rcvrIntent = new RealtimeIntent("edu.buffalo.rtdroid.action99");
        sendRealtimeBroadcast(rcvrIntent);
        return 0;
    }

    @Override
    public void onPause(Intent intent) {
        System.out.println("SingleRealtimeService onPause()...");
    }
}

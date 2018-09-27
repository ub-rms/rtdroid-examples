package edu.buffalo.rtdroid.demo;

import android.content.Intent;
import android.os.IBinder;
import edu.buffalo.rtdroid.app.RealtimeService;
import edu.buffalo.rtdroid.content.RealtimeIntent;
import edu.buffalo.rtdroid.os.RealtimeHandler;
import edu.buffalo.rtdroid.os.RealtimeMessage;

import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;


public class MsgPrcService extends RealtimeService {

    public static final int NOISY_MSG = 10;
    public static final int RT_HIGHT_MSG = 11;
    public static final String RT_MSG_CHANNEL = "micro.benchmark.msg";

    private int counter = 0;

    public IBinder onBind(Intent intent) {
        return null;
    }

    private RealtimeHandler handler = new RealtimeHandler(RT_MSG_CHANNEL) {
        @Override
        public void handleMessage(RealtimeMessage msg) {
            if( msg.getWhat() == RT_HIGHT_MSG ){
                long rcvTime = System.nanoTime();
                System.out.println( "TIME: " + msg.getLongs()[0] + " " +rcvTime);
                if( counter++ > 2000 ){
                    System.exit(0);
                }
            }
        }
    };

    public void onCreate() {
        System.out.println("MsgPrcService onCreate()...");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("MsgPrcService onStartCommand()...");
        registerRealtimeHandler(RT_MSG_CHANNEL, handler);
        return 0;
    }

    @Override
    public void onPause(Intent intent) {
        System.out.println("MsgPrcService onPause()...");
    }
}

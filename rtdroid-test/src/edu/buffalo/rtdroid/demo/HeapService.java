package edu.buffalo.rtdroid.demo;

import android.content.Intent;
import android.os.IBinder;
import edu.buffalo.rtdroid.app.RealtimeService;

/**
 * Created by gassa on 2/18/16.
 */
public class HeapService extends RealtimeService{

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onStart(Intent intent, int startId) {                           
        System.out.println("HeapService onStart()...");               
    }                                                                           

    public int onStartCommand(Intent intent, int flags, int startId) {          
        System.out.println("HeapService onStartCommand()...");
        return 0;
    }

    @Override
    public void onPause(Intent intent) {
        System.out.println("HeapService onPause()...");
    }
}

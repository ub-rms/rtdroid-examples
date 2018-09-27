package edu.buffalo.rtdroid.demo;

import android.content.Context;
import android.content.Intent;
import edu.buffalo.rtdroid.app.ComponentManager;
import edu.buffalo.rtdroid.content.RealtimeReceiver;

/**
 * Created by gassa on 2/18/16.
 */
public class ReceiverExample extends RealtimeReceiver {

    public void onReceive(Context context, Intent intent) {
        System.out.println("ReceiverExample onReceive()...");
        //ComponentManager.getInstance().dump();
    }
}

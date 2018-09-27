package edu.buffalo.rtdroid.demo;

import android.content.Context;
import android.content.Intent;
import edu.buffalo.rtdroid.content.RealtimeIntent;
import edu.buffalo.rtdroid.content.RealtimeReceiver;

import java.nio.ByteBuffer;

/**
 * Created by gassa on 2/18/16.
 */
public class BulkIntentRcvr extends RealtimeReceiver {

    public static final String RT_BROADCAST_CHANNEL="bulkintent.broadcast";
    public int counter = 0;

    public void onReceive(Context context, Intent intent) {
        RealtimeIntent rvIntent = (RealtimeIntent) intent;
        ByteBuffer bb = rvIntent.getByteBuffer();
        bb.flip();
        long end = System.nanoTime();
        System.out.println("TIME: " + bb.getLong() + " " + end);
        if( counter++ > 2000 ){
            System.exit(0);
        }

    }
}

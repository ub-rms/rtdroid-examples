package edu.buffalo.rtdroid.ci;

import android.content.Context;
import android.content.Intent;
import edu.buffalo.rtdroid.content.RealtimeIntent;
import edu.buffalo.rtdroid.content.RealtimeReceiver;

import java.nio.ByteBuffer;


public class ResultReceiver extends RealtimeReceiver {
    private ByteBuffer bb = null;
    private int count = 0;

    public ResultReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long end = System.nanoTime();
        bb = ((RealtimeIntent)intent).getByteBuffer();
        bb.flip();
        //System.out.println("pos"+bb.position()+", limit:" + bb.limit());
        for( int i=0; i<ProcessingService.NUM_CHANNELS; i++ ){
            bb.getDouble();
        }
        long[] time = new long[4];
        for( int i=0; i<4; i++){
            time[i] = bb.getLong();
        }
        System.out.println(time[0] + " " + time[1] + " " + time[2] + " " +
                time[3] + " " + end);
        if ( count++ > 4000){
            System.exit(0);
        }
    }

}

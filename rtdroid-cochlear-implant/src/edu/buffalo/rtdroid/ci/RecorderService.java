package edu.buffalo.rtdroid.ci;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import edu.buffalo.rtdroid.app.ComponentManager;
import edu.buffalo.rtdroid.app.RealtimeService;
import edu.buffalo.rtdroid.app.Repeatable;
import edu.buffalo.rtdroid.os.RealtimeMessage;
import edu.buffalo.rtdroid.os.RealtimeMsgPassingChannel;

import java.util.Arrays;

public class RecorderService extends RealtimeService {
    public static final int bufferSize = 1792;
    private PeriodicRecrod recrodTask;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        System.out.println("RecorderService onCreate...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("RecorderService onStartCommand...");
        recrodTask = new PeriodicRecrod();
        registerPeriodicTask("recordTask", recrodTask);
        return 0;
    }

    @Override
    public void onDestroy(){
        System.out.println("RecorderService onDestroy...");
    }

    @Override
    public void onPause(Intent intent) {
        System.out.println("RecorderService onDestroy...");
    }

    private class PeriodicRecrod implements Repeatable{

        @Override
        public void onRelease(Context context) {
            //read from device
            RealtimeMsgPassingChannel channel = (RealtimeMsgPassingChannel) ComponentManager.getInstance().
                                            getChannels().get("msg.buf.handler");
            // int * 1792
            RealtimeMessage msg = channel.obtain();
            msg.setWhat(ProcessingService.MESSAGE_AUDIO_SAMPLE);
            Arrays.fill(msg.getInts(), 100);
            channel.enqueue(msg);
        }
    }
}

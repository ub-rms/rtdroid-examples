package edu.buffalo.rtdroid.sound;

import android.content.Context;
import android.content.Intent;
import edu.buffalo.rtdroid.app.ComponentManager;
import edu.buffalo.rtdroid.content.RealtimeIntent;
import edu.buffalo.rtdroid.content.RealtimeReceiver;
import edu.buffalo.rtdroid.os.RealtimeMessage;
import edu.buffalo.rtdroid.os.RealtimeMsgPassingChannel;
import java.io.File;
import java.io.FileInputStream;


import java.nio.ByteBuffer;

public class RecCallback extends RealtimeReceiver {
    FileInputStream fs;
    private ByteBuffer bb = null;
    int bufferSize = 960;
    boolean init;

    @Override
    public void onReceive(Context context, Intent intent) {
//        bb = ((RealtimeIntent)intent).getByteBuffer();
//        bb.flip();
//        RealtimeIntent rvIntent = (RealtimeIntent) intent;
        System.out.println("Receive Intent");
        try{
            if(!init){
                fs = new FileInputStream(new File("/sdcard/signal.wav"));
                init = true;
            }
            if(fs!=null && fs.available()>0){
                RealtimeMsgPassingChannel channel = (RealtimeMsgPassingChannel) ComponentManager.getInstance().
                        getChannels().get("msg.buf.handler");
                RealtimeMessage msg = channel.obtain();
                msg.setWhat(AudioManager.MESSAGE_AUDIO_SAMPLE);
                byte[] buffer = new byte[bufferSize];
                fs.read(buffer);
                msg.getByteBuffer().clear();
                msg.getByteBuffer().put(buffer);
                channel.enqueue(msg);
            }
        } catch (java.io.IOException e) {
            System.out.println("Exception!");
        }
    }
}
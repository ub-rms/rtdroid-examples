package edu.buffalo.rtdroid.FFTHelper;

/**
 * Created by girish on 3/3/16.
 */
public class ConfClass {
    public int volumeChange;
    //
    public int BandGains;
    public int QValue;
    public short volume;
    ConfClass(){
        BandGains = 0;
        QValue = 0;
        volumeChange = 0;
        volume = 0;
    }
    public String toString(){
        return volumeChange + ":" + BandGains + ":" + QValue + ":"  + volume + ":";
    }
    public String print(){
        return "BandGain = " + BandGains + "\nQValue = " + QValue + "\nVolume = " + volume;
    }
    public ConfClass(String values){
        String[] msgs = values.split(":");
        int offset = 0;
        int sign = 1;
        if(msgs[0].contains("-")) {
            offset++;
            sign = -1;
        }
        volumeChange = sign*Integer.parseInt(msgs[0].substring(offset));
        BandGains = Integer.parseInt(msgs[1]);
        QValue = Integer.parseInt(msgs[2]);
        volume = (short)Integer.parseInt(msgs[3]);
    }

    public ConfClass(int volChange){
        volumeChange = volChange;
        volume = (short)volChange;
    }
}

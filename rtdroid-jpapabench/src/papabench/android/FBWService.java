package papabench.android;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.buffalo.rtdroid.app.RealtimeService;
import edu.buffalo.rtdroid.content.RealtimeIntent;
import edu.buffalo.rtdroid.content.RealtimeReceiver;
import papabench.core.autopilot.tasks.handlers.StabilizationTaskHandler;
import papabench.core.commons.conf.FBWMode;
import papabench.core.commons.conf.RadioConf;
import papabench.core.commons.data.InterMCUMsg;
import papabench.core.commons.data.RadioCommands;
import papabench.core.commons.data.impl.RadioCommandsImpl;
import papabench.core.fbw.conf.PapaBenchFBWConf;
import papabench.core.fbw.modules.FBWModule;
import papabench.core.fbw.tasks.handlers.CheckFailsafeTaskHandler;
import papabench.core.fbw.tasks.handlers.CheckMega128ValuesTaskHandler;
import papabench.core.fbw.tasks.handlers.SendDataToAutopilotTaskHandler;
import papabench.core.fbw.tasks.handlers.TestPPMTaskHandler;
import papabench.pj.PapaBenchPJFactory;
import papabench.pj.commons.tasks.PJPeriodicTask;

import javax.realtime.RealtimeThread;

public class FBWService extends RealtimeService {
    private FBWModule fbwModule;
    private PJPeriodicTask[] fbwTasks;
    private static final int FBW_TASKS_COUNT = 3; //original value: 4
    private int counterSinceLastMega128 = 0; /*for checkMega128ValueTaskHandler*/

    /**
     * checkMega128ValueTaskHandler Logic
     */
    private final RealtimeReceiver autopilotStatusRcvr = new RealtimeReceiver() {
        RadioCommands radioCommands = new RadioCommandsImpl();

        @Override
        public void onReceive(Context context, Intent intent) {
            RealtimeIntent rtIntent = (RealtimeIntent)intent;
            if( rtIntent.getInts()[4] == 1){
                counterSinceLastMega128 = 0;
                fbwModule.setMega128OK(true);
                if (fbwModule.getFBWMode() == FBWMode.AUTO) {
                    radioCommands.setPitch(rtIntent.getInts()[0]);
                    radioCommands.setRoll(rtIntent.getInts()[1]);
                    radioCommands.setThrottle(rtIntent.getInts()[2]);
                    radioCommands.setGain1(rtIntent.getInts()[3]);

                    //System.out.println("fbw rcvr input==>" + Arrays.toString(rtIntent.getInts()));

                    fbwModule.getServosController().setServos(radioCommands);

                    RealtimeIntent sentIntent = new RealtimeIntent("SimulatorFlightModelTask");
                    sentIntent.getInts()[0] = rtIntent.getInts()[0];
                    sentIntent.getInts()[1] = rtIntent.getInts()[1];
                    sentIntent.getInts()[2] = rtIntent.getInts()[2];
                    sentIntent.getInts()[3] = rtIntent.getInts()[3];
                    sentIntent.getInts()[4] = rtIntent.getInts()[4];
                    //System.out.println("fbw rcvr output==>" + Arrays.toString(sentIntent.getInts()));
                    sendRealtimeBroadcast(sentIntent);
                }else{
                    fbwModule.setMega128OK(false);
                }

            }
        }
    };

    @Override
    public void onCreate() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        System.out.println("FBWService onStartCommand");
        final RealtimeService rtService = this;
        executeInPersistent(new Runnable() {
            @Override
            public void run() {
                fbwModule = PapaBenchPJFactory.createFBWModule();
                fbwModule.setService(rtService);
                fbwModule.init();
                createFBWTasks(fbwModule);
            }
        });
        registerComponents();
        return 0;
    }

    @Override
    public void onPause(Intent intent) {

    }

    @Override
    public void onDestroy() {
    }


    private void registerComponents() {
        registerRealtimeReceiver("AutopilotStatusRcvr", autopilotStatusRcvr);
        for (int i = 0; i < fbwTasks.length; i++) {
            registerPeriodicTask(fbwTasks[i].getTaskName(), fbwTasks[i]);
        }
    }

    private void createFBWTasks(FBWModule fbwModule) {
        fbwTasks = new PJPeriodicTask[FBW_TASKS_COUNT];
        fbwTasks[0] = new PJPeriodicTask(new CheckFailsafeTaskHandler(fbwModule),
                PapaBenchFBWConf.CheckFailsafeTaskConf.NAME);
//        fbwTasks[1] = new PJPeriodicTask(
//            new CheckMega128ValuesTaskHandler(fbwModule),
//                PapaBenchFBWConf.CheckMega128ValuesTaskConf.NAME);
        fbwTasks[1] = new PJPeriodicTask(new SendDataToAutopilotTaskHandler(fbwModule),
                PapaBenchFBWConf.SendDataToAutopilotTaskConf.NAME);
        fbwTasks[2] = new PJPeriodicTask(new TestPPMTaskHandler(fbwModule),
                PapaBenchFBWConf.TestPPMTaskConf.NAME);
    }
}

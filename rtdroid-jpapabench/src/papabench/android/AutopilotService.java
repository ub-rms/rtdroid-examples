package papabench.android;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


import edu.buffalo.rtdroid.app.RealtimeService;
import edu.buffalo.rtdroid.content.RealtimeIntent;
import edu.buffalo.rtdroid.content.RealtimeReceiver;
import papabench.core.autopilot.conf.PapaBenchAutopilotConf;
import papabench.core.autopilot.modules.AutopilotModule;

import papabench.core.autopilot.tasks.handlers.AltitudeControlTaskHandler;
import papabench.core.autopilot.tasks.handlers.ClimbControlTaskHandler;
import papabench.core.autopilot.tasks.handlers.LinkFBWSendTaskHandler;
import papabench.core.autopilot.tasks.handlers.NavigationTaskHandler;
import papabench.core.autopilot.tasks.handlers.RadioControlTaskHandler;
import papabench.core.autopilot.tasks.handlers.ReportingTaskHandler;
import papabench.core.autopilot.tasks.handlers.StabilizationTaskHandler;
import papabench.core.commons.data.RadioCommands;
import papabench.core.commons.data.impl.*;

import papabench.core.simulator.conf.PapaBenchSimulatorConf;
import papabench.core.simulator.devices.SimulatedDevice;
import papabench.core.simulator.model.FlightModel;
import papabench.core.simulator.tasks.handlers.SimulatorFlightModelTaskHandler;
import papabench.core.simulator.tasks.handlers.SimulatorGPSTaskHandler;
import papabench.core.simulator.tasks.handlers.SimulatorIRTaskHandler;
import papabench.pj.PapaBenchPJFactory;
import papabench.pj.commons.tasks.PJPeriodicTask;

import javax.realtime.RealtimeThread;

public class AutopilotService extends RealtimeService{
    private AutopilotModule autopilotModule;
    private FlightModel flightModel;
    private PJPeriodicTask[] autopilotTasks;
    private static final int AUTOPILOT_TASKS_COUNT = 7;
    private PJPeriodicTask[] simulatorTasks;
    private static final int SIMULATOR_TASKS_COUNT = 3;

    /**
     * SimulatorFlightModelTaskHandler Logic
     */
    public final RealtimeReceiver simServoRcvr = new RealtimeReceiver() {
        RadioCommands radioCommands = new RadioCommandsImpl();

        @Override
        public void onReceive(Context context, Intent intent) {
            RealtimeIntent rtIntent = (RealtimeIntent)intent;
            if( rtIntent.getInts()[4] == 1){
                radioCommands.setPitch(rtIntent.getInts()[0]);
                radioCommands.setRoll(rtIntent.getInts()[1]);
                radioCommands.setThrottle(rtIntent.getInts()[2]);
                radioCommands.setGain1(rtIntent.getInts()[3]);

                //System.out.println("sim rcvr input==>" +Arrays.toString(rtIntent.getInts()));
                flightModel.processCommands(radioCommands);
                //flightModel.updateState();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("AutoPilotService onStartCommand");
        final RealtimeService service = this;
        executeInPersistent(new Runnable() {
            @Override
            public void run() {
                autopilotModule = PapaBenchPJFactory.createAutopilotModule();
                autopilotModule.setService(service);
                flightModel = PapaBenchPJFactory.createSimulator();
                //set flight plan
                autopilotModule.getNavigator().setFlightPlan(new RoundTripFlightPlan());
                autopilotModule.init();
                flightModel.init();
                createAutopilotTasks();
                createSimulatorTasks();
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
        registerRealtimeReceiver("SimulatorFlightModelTask", simServoRcvr);
        for (int i = 0; i < simulatorTasks.length; i++) {
        registerPeriodicTask(simulatorTasks[i].getTaskName(), simulatorTasks[i]);
        }
        for (int i = 0; i < autopilotTasks.length; i++) {
        registerPeriodicTask(autopilotTasks[i].getTaskName(), autopilotTasks[i]);
        }
    }

    private void createAutopilotTasks() {
        autopilotTasks = new PJPeriodicTask[AUTOPILOT_TASKS_COUNT];
        autopilotTasks[0] = new PJPeriodicTask(new AltitudeControlTaskHandler(autopilotModule),
                PapaBenchAutopilotConf.AltitudeControlTaskConf.NAME);
        autopilotTasks[1] = new PJPeriodicTask(new ClimbControlTaskHandler(autopilotModule),
                PapaBenchAutopilotConf.ClimbControlTaskConf.NAME);
        autopilotTasks[2] = new PJPeriodicTask(new LinkFBWSendTaskHandler(autopilotModule),
                PapaBenchAutopilotConf.LinkFBWSendTaskConf.NAME);
        autopilotTasks[3] = new PJPeriodicTask(new NavigationTaskHandler(autopilotModule),
                PapaBenchAutopilotConf.NavigationTaskConf.NAME);
        autopilotTasks[4] = new PJPeriodicTask(new RadioControlTaskHandler(autopilotModule),
                PapaBenchAutopilotConf.RadioControlTaskConf.NAME);
        autopilotTasks[5] = new PJPeriodicTask(new ReportingTaskHandler(autopilotModule),
                PapaBenchAutopilotConf.ReportingTaskConf.NAME);
        // StabilizationTask allocates messages which are sent to FBW unit -> allocate them in scope memory
        autopilotTasks[6] = new PJPeriodicTask(new StabilizationTaskHandler(autopilotModule, this),
                PapaBenchAutopilotConf.StabilizationTaskConf.NAME);
    }

    public void createSimulatorTasks() {
        simulatorTasks = new PJPeriodicTask[SIMULATOR_TASKS_COUNT];
        simulatorTasks[0] = new PJPeriodicTask(new
                SimulatorFlightModelTaskHandler(flightModel, autopilotModule, null),
                PapaBenchSimulatorConf.SimulatorFlightModelTaskConf.NAME);
        simulatorTasks[1] = new PJPeriodicTask(new SimulatorGPSTaskHandler(flightModel, autopilotModule),
                PapaBenchSimulatorConf.SimulatorGPSTaskConf.NAME);
        simulatorTasks[2] = new PJPeriodicTask(new SimulatorIRTaskHandler(flightModel, autopilotModule),
                PapaBenchSimulatorConf.SimulatorIRTaskConf.NAME);
    }
}

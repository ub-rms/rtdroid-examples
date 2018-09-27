/*
 * $Id: StabilizationTaskHandler.java 606 2010-11-02 19:52:33Z parizek $
 *
 * This file is a part of jPapaBench providing a Java implementation of
 * PapaBench project. Copyright (C) 2010 Michal Malohlava
 * <michal.malohlava_at_d3s.mff.cuni.cz>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package papabench.core.autopilot.tasks.handlers;

import edu.buffalo.rtdroid.content.RealtimeIntent;
import papabench.android.AutopilotService;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.tasks.pids.RollPitchPIDController;
import papabench.core.commons.conf.RadioConf;
import papabench.core.utils.PPRZUtils;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import com.fiji.fivm.Time;

/**
 * Task handler responsible for stabilization of airplane according to
 * navigation commands.
 *
 * f = ? Hz
 *
 * @author Michal Malohlava
 *
 */
// @SCJAllowed
public class StabilizationTaskHandler implements Runnable {

  public static final String AUTOPILOT_STATUS =
      "jpapabench.cse.buffalo.edu.autopilot_status";
  public static final String AUTOPILOT_PITCH =
      "jpapabench.cse.buffalo.edu.pitch";
  public static final String AUTOPILOT_ROLL = "jpapabench.cse.buffalo.edu.roll";
  public static final String AUTOPILOT_THROTTL =
      "jpapabench.cse.buffalo.edu.throttle";
  public static final String AUTOPILOT_GAIN1 =
      "jpapabench.cse.buffalo.edu.gain1";
  public static final String AUTOPILOT_VALID =
      "jpapabench.cse.buffalo.edu.valid";

  private final AutopilotModule autopilotModule;
  private final Context context;
  private final RollPitchPIDController pidController;
  private final AutopilotService service;

  public StabilizationTaskHandler(AutopilotModule autopilotModule,
      Context context) {
    this.autopilotModule = autopilotModule;
    this.pidController = new RollPitchPIDController();
    this.context = context;
    this.service = ((AutopilotService) autopilotModule.getService());
  }

  public void run() {
    long start = Time.nanoTime();
    autopilotModule.getIRDevice().update();
    autopilotModule.getEstimator().updateIRState();

    pidController.control(autopilotModule, autopilotModule.getEstimator(),
        autopilotModule.getNavigator());
   
    int curX = (int)autopilotModule.getEstimator().getPosition().x;
    int curY = (int)autopilotModule.getEstimator().getPosition().y;
    int curZ = (int) autopilotModule.getEstimator().getPosition().z;
    int tarX = (int)autopilotModule.getNavigator().getDesiredPosition().x;
    int tarY = (int)autopilotModule.getNavigator().getDesiredPosition().y;
    int tarZ = (int)autopilotModule.getNavigator().getDesiredAltitude();
    String output = "============================ Navigation cycle: " +
    autopilotModule.getEstimator().getFlightTime() + "\n [" +
            curX + "," + curY + "," + curZ +"] => [" +
            tarX + "," + tarY + "," + tarZ+"]";
    System.out.println(output);
    //////////////////////////////////////
    //int * 5 : pitch, roll, throtll, gain1, isvalid
    RealtimeIntent rtIntent = new RealtimeIntent("AutopilotStatusRcvr");
    rtIntent.getInts()[0] = autopilotModule.getElevator();
    rtIntent.getInts()[1] = autopilotModule.getAileron();
    rtIntent.getInts()[2] = autopilotModule.getGaz();
    rtIntent.getInts()[3] = (int) PPRZUtils.trimPPRZ(
            RadioConf.MAX_PPRZ / 0.75f *
                    (autopilotModule.getEstimator().getAttitude().phi));
    rtIntent.getInts()[4] = 1;
    //System.out.println("stab ints:==>" + Arrays.toString(rtIntent.getInts()));
    service.sendRealtimeBroadcast(rtIntent);
    //////////////////////////////////////
    long end = Time.nanoTime();
    //System.out.println("cycles:" +  autopilotModule.getEstimator().getFlightTime());
    System.out.println("stab: " + start + " " + end);
  }
}

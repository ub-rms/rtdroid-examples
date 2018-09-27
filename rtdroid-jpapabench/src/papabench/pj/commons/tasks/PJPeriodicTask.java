/* $Id$
 * 
 * This file is a part of jPapaBench providing a Java implementation 
 * of PapaBench project.
 * Copyright (C) 2010  Michal Malohlava <michal.malohlava_at_d3s.mff.cuni.cz>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */
package papabench.pj.commons.tasks;


import android.content.Context;
import edu.buffalo.rtdroid.app.Repeatable;
import edu.buffalo.rtdroid.util.experiments.RealTimeHelper;
import javax.realtime.PriorityParameters;
import javax.realtime.RealtimeThread;

/**
 * This is just a simple representation of plain Java periodic thread. 
 * 
 * It carries information about periodic invocation, but it does not do it.
 *
 * @author Michal Malohlava
 *
 */
public class PJPeriodicTask implements Repeatable{
	
	private Runnable taskHandler;
	private String taskName;
	int count = 0;
	int priority = 0;

	public PJPeriodicTask(Runnable taskHandler, String name) {
		this.taskHandler = taskHandler;
		this.taskName = name;
	}

	public Runnable getTaskHandler() {
		return taskHandler;
	}

	public String getTaskName(){
		return taskName;
	}

	@Override
	public void onRelease(Context context) {
		if( count++ == 0 ){
			priority = RealTimeHelper.fiji2RTSJ(((PriorityParameters)
					RealtimeThread.currentRealtimeThread()
							.getSchedulingParameters()).getPriority());
			System.out.println("==>" + taskName + "(" + priority + ")");
		}
		taskHandler.run();
	}
}

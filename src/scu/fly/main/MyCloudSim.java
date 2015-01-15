package scu.fly.main;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudInformationService;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.DeferredQueue;
import org.cloudbus.cloudsim.core.FutureQueue;

import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.Predicate;

public class MyCloudSim extends CloudSim {

	public static double startSimulation() throws NullPointerException {
		Log.printLine("Starting CloudSim version " + CLOUDSIM_VERSION_STRING);
		try {
			double clock = run();

			// reset all static variables
			cisId = -1;
			shutdownId = -1;
			cis = null;
			calendar = null;
			traceFlag = false;

			return clock;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new NullPointerException("CloudSim.startCloudSimulation() :"
					+ " Error - you haven't initialized CloudSim.");
		}
	}

	/**
	 * Initialise the simulation for stand alone simulations. This function
	 * should be called at the start of the simulation.
	 */
	// protected static void initialize() {
	// Log.printLine("Initialising...");
	// entities = new ArrayList<SimEntity>();
	// entitiesByName = new LinkedHashMap<String, SimEntity>();
	//
	// deferred = new DeferredQueue();
	// waitPredicates = new HashMap<Integer, Predicate>();
	// clock = 0;
	// running = false;
	// }
	public static void init(int numUser, Calendar cal, boolean traceFlag) {
		try {
			initCommonVariable(cal, traceFlag, numUser);

			// create a GIS object
			cis = new CloudInformationService("CloudInformationService");

			// set all the above entity IDs
			cisId = cis.getId();
			 terminateAt = -1;//设置终止时间
			// future = (MyFutureQueue)future;//强转为自己的类型

		} catch (IllegalArgumentException s) {
			Log.printLine("CloudSim.init(): The simulation has been terminated due to an unexpected error");
			Log.printLine(s.getMessage());
		} catch (Exception e) {
			Log.printLine("CloudSim.init(): The simulation has been terminated due to an unexpected error");
			Log.printLine(e.getMessage());
		}
	}

	public static boolean runClockTick() {
		SimEntity ent;
		boolean queue_empty = false;
		int entities_size = entities.size();

		// 每个entity循环处理 processEvent
		for (int i = 0; i < entities_size; i++) {
			ent = entities.get(i);
			if (ent.getState() == SimEntity.RUNNABLE) {
				ent.run();
			}
			if (ent.getClass().getName()
					.contains("Broker")
					&& ent.getState() == SimEntity.FINISHED) {
				printMessage("得到datacenterBroker任务完成的消息！");
				queue_empty = true;
				break;
				// return queue_empty;
			}
		}
		// If there are more future events then deal with them
		if (future.size() > 0) {

			// printMessage("--------------本轮---------------\nfuture大小为："+future.size());

			List<SimEvent> toRemove = new ArrayList<SimEvent>();
			Iterator<SimEvent> it = future.iterator();
			queue_empty = false;
			SimEvent first = it.next();
			if (clock < first.eventTime()) {
				processEvent(first);
				future.remove(first);
			}

			it = future.iterator();

			// Check if next events are at same time...
			boolean trymore = it.hasNext();
			while (trymore) {
				SimEvent next = it.next();
				// printMessage("下一个事件为："+first.toString());
				if (next.eventTime() == first.eventTime()) {
					processEvent(next);
					toRemove.add(next);
					trymore = it.hasNext();
				} else {
					trymore = false;
				}
			}

			future.removeAll(toRemove);

		}

		return queue_empty;
	}

	public static double run() {
		// TODO Auto-generated method stub
		if (!running) {
			runStart();
		}
		while (true) {
			if (runClockTick() || abruptTerminate) {
				break;
			}
			// printMessage("-------------当前仿真时间为："+clock+"-------------");
			// runClockTick();
			// clock++;
			// this block allows termination of simulation at a specific time
			if (terminateAt > 0.0 && clock >= terminateAt) {
				terminateSimulation();

				clock = terminateAt;
				break;
			}

			if (pauseAt != -1
					&& ((future.size() > 0 && clock <= pauseAt && pauseAt <= future
							.iterator().next().eventTime()) || future.size() == 0
							&& pauseAt <= clock)) {
				pauseSimulation();
				clock = pauseAt;
			}

			while (paused) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		double clock = clock();

		finishSimulation();
		runStop();

		return clock;
	}

}

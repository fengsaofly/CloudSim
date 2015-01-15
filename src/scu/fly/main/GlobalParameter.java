package scu.fly.main;

import java.util.Random;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G5Xeon3075;

public class GlobalParameter {
	
	private static final int BASE = 0;
	//time slot
	public static final int SLOT =BASE+1000;
	
	//public static final int SIMULATION_TIME = 100000;
	public static final int STOP = BASE +0;
	public static final int UNIT = BASE +1000;
	
	
	public static final int RUNNING = BASE +1;
	
	public static final int WAIT = BASE +2;
	
	public static final int UNSTART = BASE +3;
	
	public static final int INTERVAL = BASE +1*UNIT;
	
	
	public static final int AlgorithmType[] = {0,1,2,3};
	
	public static final String algorithm[] = {"fly","li","random","rr"};
	
	public final static PowerModel[] HOST_POWER = {
		new PowerModelSpecPowerHpProLiantMl110G4Xeon3040(),
		new PowerModelSpecPowerHpProLiantMl110G5Xeon3075()
	};
	
	public final static int TimeUnit = 10; // means 100 milliSecs = 1 time unit，时间单位为1s
    public static long seed = 0;
	public static Random numGen = new Random(seed);

}

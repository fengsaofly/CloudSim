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
	
//	public final static int CLOUDLET_LENGTH	= 2500 * (int) SIMULATION_LIMIT;
	public final static int CLOUDLET_PES	= 1;

	/*
	 * VM instance types:
	 *   High-Memory Extra Large Instance: 3.25 EC2 Compute Units, 8.55 GB // too much MIPS
	 *   High-CPU Medium Instance: 2.5 EC2 Compute Units, 0.85 GB
	 *   Extra Large Instance: 2 EC2 Compute Units, 3.75 GB
	 *   Small Instance: 1 EC2 Compute Unit, 1.7 GB
	 *   Micro Instance: 0.5 EC2 Compute Unit, 0.633 GB
	 *   We decrease the memory size two times to enable oversubscription
	 *
	 */
	public final static int VM_TYPES	= 4;
	public final static int[] VM_MIPS	= { 2500, 2000, 1000, 500 };//CPU计算能力 
	public final static int[] VM_PES	= { 1, 1, 1, 1 };//核心数
	public final static int[] VM_RAM	= { 870,  1740, 1740, 613 };//内存大小
	public final static int VM_BW		= 100000; // 100 Mbit/s
	public final static int VM_SIZE		= 2500; // 2.5 GB

	/*
	 * Host types:
	 *   HP ProLiant ML110 G4 (1 x [Xeon 3040 1860 MHz, 2 cores], 4GB)
	 *   HP ProLiant ML110 G5 (1 x [Xeon 3075 2660 MHz, 2 cores], 4GB)
	 *   We increase the memory size to enable over-subscription (x4)
	 */
	public final static int HOST_TYPES	 = 2;
	public final static int[] HOST_MIPS	 = { 1860, 2660 };
	public final static int[] HOST_PES	 = { 2, 2 };
	public final static int[] HOST_RAM	 = { 4096, 4096 };
	public final static int HOST_BW		 = 1000000; // 1 Gbit/s
	public final static int HOST_STORAGE = 1000000; // 1 GB

	
	public final static int TimeUnit = 10; // means 100 milliSecs = 1 time unit，时间单位为1s
    public static long seed = 0;
	public static Random numGen = new Random(seed);

}

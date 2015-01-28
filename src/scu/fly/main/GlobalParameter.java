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
		new PowerModelSpecPowerHpProLiantMl110G5Xeon3075(),
		new PowerModelSpecPowerFly1()
	};
	
//	public final static int CLOUDLET_LENGTH	= 2500 * (int) SIMULATION_LIMIT;
	public final static int[] CLOUDLET_PES	= {8, 6, 20, 6};

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
	public final static int[] VM_MIPS	= { 1000, 1000, 1000, 1000 };//CPU计算能力 
	public final static int[] VM_PES	= { 8, 6, 20, 6 };//核心数
	public final static int[] VM_RAM	= { 15000,  17000, 6000, 6000 };//内存大小
	public final static int VM_BW		= 100000; // 100 Mbit/s
	public final static int[] VM_SIZE	= {1690 , 420 , 1290 , 600}; // 2.5 GB

	/*
	 * Host types:
	 *   HP ProLiant ML110 G4 (1 x [Xeon 3040 1860 MHz, 2 cores], 4GB)
	 *   HP ProLiant ML110 G5 (1 x [Xeon 3075 2660 MHz, 2 cores], 4GB)
	 *   We increase the memory size to enable over-subscription (x4)
	 */
	public final static int HOST_TYPES	 = 3;
	public final static int HOST_RESOURCES	 = 2;//考虑的资源数
	public final static int[] HOST_MIPS	 = { 1000, 1000,1000 };
	public final static int[] HOST_PES	 = { 100,60,20 };
	public final static int[] HOST_RAM	 = { 100000, 20000,50000 };
	public final static int[] HOST_STORAGE	 = { 100000, 4000,4000 };
	public final static int HOST_BW		 = 1000000; // 1 Gbit/s
	
	public final static double[] HOST_POWER_FACTOR = {25.08,50.16,16.72};
	public final static double HOST_POWER_CONSTANT = 86.495;
	
	public final static int TimeUnit = 10; // means 100 milliSecs = 1 time unit，时间单位为1s
    public static long seed = 0;
	public static Random numGen = new Random(seed);

}

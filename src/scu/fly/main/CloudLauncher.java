package scu.fly.main;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
//import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import DataCenterBrokerModified.DatacenterBrokerModifiedRealTime;

import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javax.servlet.ServletContextEvent;
//import javax.servlet.ServletContextListener;

import jxl.write.WriteException;

/**
 * An example showing how to pause and resume the simulation, and create
 * simulation entities (a DatacenterBroker in this example) dynamically.
 */
public class CloudLauncher {

	public static DatacenterBroker broker;
	public static Datacenter datacenter0;
	// public static int SLOTSCOUNT = 1;
	public static int UNIT = 1;
	// 物理机配置(cpu,mem,storage)
	public static int MCS[] = { 30, 30, 4000 * UNIT };// mem:30GB cpu:30GHz
														// storage:4000GB
	public static int HOSTNUM = 100;
	public static int vmId = 0;
	// public static int curVmId = 0;
	public static int mutiples = 1;
	public static Double variance = 0.0;
	public static Double averageWaitingTime = 0.0;
	public static int WriteNums = 20;
	public static Date StartTime = new Date();
	public static boolean hasCloudlet = false;//判断是否有任务
	public static int  CloudletsCount = 10;
	public static int usedHosts = 0;
	public static double energy = 0;
	public static double A = 333.21;
	public static double B = 86.495;
	
	/** The cloudlet list. */
	private static CopyOnWriteArrayList<woCloudlet> cloudletList = new CopyOnWriteArrayList<woCloudlet>();
	/** The vmlist. */
	private  static CopyOnWriteArrayList<myVm> vmlist = new CopyOnWriteArrayList<myVm>();

	public static SimpleExcelWrite excelWrite = SimpleExcelWrite.getInstance();
	public static Thread posterT  = null;

	private static List<myVm> createVM(int userId, int vms, int idShift) {
		// Creates a container to store VMs. This list is passed to the broker
		// later
		LinkedList<myVm> list = new LinkedList<myVm>();

		// VM Parameters
		long size = 10000; // image size (MB)
		int ram = 512; // vm memory (MB)
		int mips = 250;
		long bw = 1000;
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name

		// create VMs
		myVm[] vm = new myVm[vms];

		for (int i = 0; i < vms; i++) {
			vm[i] = new myVm(idShift + i, userId, mips, pesNumber, ram, bw,
					size, vmm, new CloudletSchedulerTimeShared());
			list.add(vm[i]);
		}

		return list;
	}

	private static List<myVm> createVM(int userId, int vms, int idShift,
			int type) {
		// Creates a container to store VMs. This list is passed to the broker
		// later

		LinkedList<myVm> list = new LinkedList<myVm>();
		double memSize = 0, cpuSize = 0;
		int storageSize = 0;
		String error = null;
		switch (type) {
		case 0:
			memSize = 15;
			cpuSize = 8;
			storageSize = 1690;
			break;
		case 1:
			memSize = 17;
			cpuSize = 6;
			storageSize = 420;
			break;
		case 2:
			memSize = 7;
			cpuSize = 20;
			storageSize = 1690;
			break;
		default:
			error = "type类型有误，不属于[0,2]!";

		}
		if ("".equals(error))
			return null;

		else {
			// VM Parameters
			long size = storageSize * UNIT; // image size (MB)
			int ram = (int) (1000 * memSize); // vm memory (MB)
			int mips = 250;// 相当于1GHz,因为1mips = 4MHz
			long bw = 1000;
			int pesNumber = (int) (cpuSize); // number of cpus
			String vmm = "Xen"; // VMM name

			// create VMs
			myVm[] vm = new myVm[vms];

			for (int i = 0; i < vms; i++) {
				vm[i] = new myVm(idShift + i, userId, mips, pesNumber, ram, bw,
						size, vmm, new CloudletSchedulerTimeShared(), type);
				list.add(vm[i]);
			}

			return list;
		}
	}

//	private static List<woCloudlet> createCloudlet(int userId, int cloudlets,
//			int idShift) {
//		// Creates a container to store Cloudlets
//		LinkedList<woCloudlet> list = new LinkedList<woCloudlet>();
//
//		// cloudlet parameters
//		long length = 40000;
//		long fileSize = 50;
//		long outputSize = 50;
//		int pesNumber = 1;
//		UtilizationModel utilizationModel = new UtilizationModelFull();
//
//		woCloudlet[] cloudlet = new woCloudlet[cloudlets];
//
//		for (int i = 0; i < cloudlets; i++) {
//			cloudlet[i] = new woCloudlet(idShift + i, length, pesNumber,
//					fileSize, outputSize, utilizationModel, utilizationModel,
//					utilizationModel);
//			// setting the owner of these Cloudlets
//			cloudlet[i].setUserId(userId);
//			list.add(cloudlet[i]);
//		}
//
//		return list;
//	}

	private static List<woCloudlet> createCloudlet(int userId, int cloudlets,
			int idShift, int size, int type) {
		// Creates a container to store Cloudlets
		LinkedList<woCloudlet> list = new LinkedList<woCloudlet>();

		// cloudlet parameters
		long length = 4000 * size;
		long fileSize = 50;
		long outputSize = 50;
		int pesNumber = 1;
		String error = null;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		switch (type) {
		case 0:
			pesNumber = 8;
			break;
		case 1:
			pesNumber = 6;
			break;
		case 2:
			pesNumber = 20;
			break;
		default:
			error = "type类型有误，不属于[0,2]!";

		}
		if ("".equals(error))
			return null;
		woCloudlet[] cloudlet = new woCloudlet[cloudlets];

		for (int i = 0; i < cloudlets; i++) {
			cloudlet[i] = new woCloudlet(idShift + i, length, pesNumber,
					fileSize, outputSize, utilizationModel, utilizationModel,
					utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}

	private static int random(double A, double B) {

		double x = Math.random();
		return (int) (x * (A - B) + B);
	}

	private static int randGenerateCloudletSize() {
		double rand = Math.random();
		int randSize;
		if (rand < 0.7)
			randSize = random(10, 50);
		else if (rand > 0.85)
			randSize = random(451, 500);
		else
			randSize = random(251, 300);
		return randSize;
	}
	private static void generateStableVmsAndCloudlets(int size) {
		vmlist.clear();
		cloudletList.clear();
		
		int[] lengthArray = {40,10,120,120,160,160,1920,540,1120,1040};



		int userId = 3;
		if (broker != null)
			userId = broker.getId();

		for (int i = 0; i < size; i++) {

			int randomCloudletSize = lengthArray[i];
			myVm curVm = createVM(userId, 1, vmId, i%2).get(0);
			vmlist.add(curVm);

			woCloudlet curCloudlet = createCloudlet(userId, 1, vmId++,
					randomCloudletSize, i%2).get(0);
			
			//绑定VM
			curCloudlet.setVmId(curVm.getId());
			curCloudlet.setVmType(curVm.getVmType());
			curCloudlet.setVm(curVm);
			
			
			cloudletList.add(curCloudlet);


		}

	}

	private static ArrayList<List<Integer>> generateVmsAndCloudlets(int size) {
		vmlist.clear();
		cloudletList.clear();
		
//		int[] lengthArray = {40,10,120,120,160,160,1920,540,1120,1040};

		double[] config = { 1, (double) 1 / 3, (double) 2 / 3 };
		double[] probabilities = new double[3];
		double arg = 0.5;
		List<List<Integer>> T = new ArrayList<List<Integer>>();

		ArrayList<Integer> vmlist1 = new ArrayList<Integer>();
		ArrayList<Integer> vmlist2 = new ArrayList<Integer>();
		ArrayList<Integer> vmlist3 = new ArrayList<Integer>();
		T.add(vmlist1);
		T.add(vmlist2);
		T.add(vmlist3);

		int userId = 3;
		if (broker != null)
			userId = broker.getId();

		// else if(simpleBroker!=null)
		// userId = simpleBroker.getId();

		double rand = 0;

		for (int i = 0; i < config.length; i++) {
			probabilities[i] = config[i] * arg / 130.5 * 100;
		}
		while (size > 0) {

			for (int vmlistId = 0; vmlistId < probabilities.length; vmlistId++) {
				rand = Math.random();
				if (rand < probabilities[vmlistId]) {
					// [countVM[pId].length] = randGenerateCloudletSize();
					int randomCloudletSize = randGenerateCloudletSize();
					T.get(vmlistId).add(randomCloudletSize);
					myVm curVm = createVM(userId, 1, vmId, vmlistId).get(0);
					vmlist.add(curVm);

					woCloudlet curCloudlet = createCloudlet(userId, 1, vmId++,
							randomCloudletSize, vmlistId).get(0);
					
					//绑定VM
					curCloudlet.setVmId(curVm.getId());
					curCloudlet.setVmType(curVm.getVmType());
					curCloudlet.setVm(curVm);
					
					
					cloudletList.add(curCloudlet);
					size--;
					if (size == 0)
						break;
				}

			}

		}

		return (ArrayList<List<Integer>>) T;

	}

	private static void recoverConfig() {
		broker = null;
		// simpleBroker = null;
		datacenter0 = null;
		vmId = 0;
		hasCloudlet = false;
		
		myHost.clearRecords();
		// cloudletList = new LinkedList<woCloudlet>();
		// vmlist = new LinkedList<myVm>();
	}

	private static void bindCloudletToVm() {

		Log.printLine("========== 共产生" + vmId + "个任务 ==========");
		for (myVm vm : vmlist) {
			int vmid = vm.getId();
			woCloudlet cloudlet = cloudletList.get(vmid);
			cloudlet.setVmId(vmid);
			cloudlet.setVmType(vm.getVmType());
		}

	}

	/**
	 * 采用name策略运行方法
	 * 
	 * @param name
	 * @param size
	 * @return
	 */
	public static Double runOneMethod(String name, int size) {

		if (name.equals("fly") || name.equals("other")) {
			int num_user = 1; // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			MyCloudSim.init(num_user, calendar, trace_flag);
			datacenter0 = createDatacenter("Datacenter_0", name);

			myHost.setMc((myDatacenter) datacenter0);
			// 设置调度算法
			if (name.equals("fly"))
				myHost.scheduleMethod = 0;
			else
				myHost.scheduleMethod = 1;
			// 云代理
			broker = createBroker("Broker_0", name);

		
			startPoster(size);
			
		
			while (!hasCloudlet) {
				Log.printLine("######等待任务出现#######");
			}
			Log.printLine("######有任务出现，启动CloudSim#######");
			MyCloudSim.startSimulation();
			
			
			// Final step: Print results when simulation is over
			List<woCloudlet> newList = broker.getCloudletReceivedList();

			MyCloudSim.stopSimulation();

			printCloudletList(newList, name);

			// Print the debt of each user to each datacenter
			datacenter0.printDebts();

			return calculateAndPrintValues(myHost.cpuValuesList,
					myHost.memValuesList,myHost.timeValuesList);
		}

		else if (name.equals("rr") || name.equals("random")
				|| name.equals("li")) {
			int num_user = 1; // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			MyCloudSim.init(num_user, calendar, trace_flag);
			datacenter0 = createDatacenter("Datacenter_0", name);

			myHost.setMc((myDatacenter) datacenter0);
			// 云代理
			broker = createBroker("Broker_0", name);

			// generateVmsAndCloudlets(size);
			//启动任务发射器
			startPoster(size);

			while (!hasCloudlet) {
				Log.printLine("######等待任务出现#######");
			}
			Log.printLine("######有任务出现，启动CloudSim#######");
			MyCloudSim.startSimulation();
			
			// Final step: Print results when simulation is over
			List<woCloudlet> newList = broker.getCloudletReceivedList();

			MyCloudSim.stopSimulation();

			printCloudletList(newList, name);

			// Print the debt of each user to each datacenter
			datacenter0.printDebts();

			return calculateAndPrintValues(myHost.cpuValuesList,
					myHost.memValuesList,myHost.timeValuesList);
		} else if (name.equals("test")) {
			int num_user = 1; // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);
			Datacenter datacenter0 = createDatacenter("Datacenter_0", name);

			// 云代理
			broker = createBroker("Broker_0", name);

			// generateVmsAndCloudlets(size);

			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);

			bindCloudletToVm();

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<woCloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

			printCloudletList(newList, name);

			// Print the debt of each user to each datacenter
			datacenter0.printDebts();

			return calculateAndPrintValues(myHost.cpuValuesList,
					myHost.memValuesList,myHost.timeValuesList);
		}
		return null;
	}

	public synchronized static void sendRandomCloudlets(int size) {


		Log.printLine("------共产生"+size+"个任务------");
		generateVmsAndCloudlets(size);
//		generateStableVmsAndCloudlets(size);
		if (cloudletList.size() > 0 && vmlist.size() > 0) {
			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);
//			bindCloudletToVm();
		}
	}

	// //////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		Log.printLine("Starting CloudSim...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.

			for (mutiples = 2; mutiples <22; mutiples +=2) {

				// Second step: Create Datacenters
				// Datacenters are the resource providers in CloudSim. We need
				// at list one of them to run a CloudSim simulation

				// 随机生成任务
				int size = 5 * mutiples;
				int totalSize = size*CloudletsCount;
				ArrayList<Double> utilizationList = new ArrayList<>();
				ArrayList<Double> varianceList = new ArrayList<>();
				ArrayList<Double> avgWaitTimeList = new ArrayList<>();
				ArrayList<Double> usedHostsList = new ArrayList<>();
				ArrayList<Double> energyList = new ArrayList<>();
				utilizationList.add((double) totalSize);
				avgWaitTimeList.add((double) totalSize);
				varianceList.add((double) totalSize);
				usedHostsList.add((double) totalSize);
				energyList.add((double) totalSize);
				// broker = createBroker("Broker_0", "fly");

				
//				generateVmsAndCloudlets(size);
				for (int type = 0; type < GlobalParameter.AlgorithmType.length; type++) {
					
					recoverConfig();
					String policy = GlobalParameter.algorithm[type];
					double utilization = 0;
					utilization = runOneMethod(policy, size);

					utilizationList.add(utilization);
					varianceList.add(variance);
					avgWaitTimeList.add(averageWaitingTime/totalSize);
					usedHostsList.add((double)usedHosts);
					energyList.add(energy);
				}

				double sys = 0.0;
//				for (Double double1 : myHost.sysUtilizationValues) {
//					Log.print(" " + double1);
//					sys += double1;
//				}
//				Log.printLine();
//				Log.printLine("总差距为：" + sys);
				try {
					// 记录利用率
					excelWrite.setCurRow(0);
					excelWrite.writeColumn(utilizationList, false);
					// 在第7行 记录方差=负载不平衡度
					excelWrite.setCurRow(6);
					excelWrite.writeColumn(varianceList, false);
					excelWrite.setCurRow(12);
					excelWrite.writeColumn(usedHostsList, false);
					excelWrite.setCurRow(18);
					excelWrite.writeColumn(energyList, false);
					excelWrite.setCurRow(24);
					excelWrite.writeColumn(avgWaitTimeList, true);
				} catch (WriteException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			excelWrite.closeExcel();
			Log.printLine("MySimulation finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}

	}

	private static void recoverVmsAndCloudlets() {
		// TODO Auto-generated method stub
		for (woCloudlet cloudlet : cloudletList) {
			cloudlet.setHostID(-1);
			cloudlet.setVmType(-1);
		}
		for (myVm vm : vmlist) {
			vm.setHost(null);
			vm.setHostID(-1);
		}
	}

	private static List<Host> createHosts(int[] peList, String policy) {
		List<Host> hostList = new ArrayList<Host>();

		int mips = 1000;

		int hostId = 0;
		int ram = MCS[0] * 1000; // host memory (MB)
		long storage = MCS[2]; // host storage
		int bw = 10000;

		for (int peNumPerHost : peList) {
			List<Pe> peNumForPerHost = new ArrayList<Pe>();
			int peId = 0;
			while (peId < peNumPerHost) {

				peNumForPerHost
						.add(new Pe(peId, new PeProvisionerSimple(mips)));
				peId++;
			}
			if (policy.equals("fly") || policy.equals("other")) {
				myHost h = new myHost(hostId++, new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw), storage, peNumForPerHost,
						new VmSchedulerTimeShared(peNumForPerHost), null);
				hostList.add(h);
			} else if (policy.equals("rr") || policy.equals("random")) {
				Host h = new myHost(hostId++, new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw), storage, peNumForPerHost,
						new VmSchedulerTimeShared(peNumForPerHost), null);
				hostList.add(h);
			} else {
				FlyHost h = new FlyHost(hostId++,
						new RamProvisionerSimple(ram), new BwProvisionerSimple(
								bw), storage, peNumForPerHost,
						new VmSchedulerTimeShared(peNumForPerHost), null);
				hostList.add(h);
			}
			// h.updateVmConfig();

		}

		Log.printLine("主机数目为：" + hostList.size());

		return hostList;

	}

	private static Datacenter createDatacenter(String name, String policy) {

		int hostNum = HOSTNUM;
		int peNum = MCS[1];

		int peList[] = new int[hostNum];
		for (int i = 0; i < hostNum; i++)
			peList[i] = peNum;
		List<Host> hostList = createHosts(peList, policy);

		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.1; // the cost of using storage in this
										// resource
		double costPerBw = 0.1; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are
																		// not
																		// adding
																		// SAN
																		// devices
																		// by
																		// now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			if (policy.equals("random"))
				datacenter = new myDatacenter(name, characteristics,
						new VmAllocationPolicyRandom(hostList), storageList, 10);
			else if (policy.equals("rr")) {
				datacenter = new myDatacenter(name, characteristics,
						new VmAllocationPolicyRoundRobin(hostList),
						storageList, 10);
			} else if (policy.equals("other")) {
				datacenter = new myDatacenter(name, characteristics,
						new VmAllocationPolicyMy(hostList), storageList, 0);
			} else if (policy.equals("fly")) {
				datacenter = new myDatacenter(name, characteristics,
						new VmAllocationPolicyMy(hostList), storageList, 0);
			} else if (policy.equals("li")) {
				datacenter = new myDatacenter(name, characteristics,
						new VmAllocationPolicyLi(hostList), storageList, 0);
			} else {
				datacenter = new Datacenter(name, characteristics,
						new VmAllocationPolicyFly(hostList), storageList, 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * 创建云代理
	 * 
	 * @param name
	 * @param policy
	 * @return
	 */
	private static DatacenterBroker createBroker(String name, String policy) {

		DatacenterBroker broker = null;
		try {
			if (policy.equals("fly") || policy.equals("other"))
				broker = new myDatacenterBroker(name);
			else if (policy.equals("rr") || policy.equals("random") ||  policy.equals("li") )
				broker = new DatacenterBrokerSimple(name);
			else
				broker = new DatacenterBrokerSimple(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	/**
	 * Prints the Cloudlet objects
	 * 
	 * @param list
	 *            list of Cloudlets
	 */
	private static void printCloudletList(List<woCloudlet> list, String name) {
		int size = list.size();
		woCloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("=========" + name + "   共完成" + size + "个任务 ==========");
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + indent
				+ "Time" + indent + "Start Time" + indent + "Finish Time"
				+ indent + "host ID" + indent + "vmType");

		DecimalFormat dft = new DecimalFormat("###.##");
		averageWaitingTime = 0.0;
		
//		Collections.sort(list);
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == woCloudlet.SUCCESS) {
				Log.print(" SUCCESS");

				Log.printLine(indent+indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent  + indent+ indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent+ indent
						+ dft.format(cloudlet.getFinishTime()) + indent
						+ indent+ indent + dft.format(cloudlet.getHostID())
						+ indent  + indent+ indent
						+ dft.format(cloudlet.getVmType()));
				averageWaitingTime += cloudlet.getExecStartTime();
			}
		}
		averageWaitingTime = averageWaitingTime / size;
		
		
	}

	public static Double calculateAndPrintValues(ArrayList<ArrayList> cpuList,
			ArrayList<ArrayList> memList,ArrayList<ArrayList> timeList) {
		DecimalFormat df = new DecimalFormat("0.0000");
		String indent = "    ";
		ArrayList<ArrayList> cpuValuesList = cpuList;
		ArrayList<ArrayList> memValuesList = memList;


		if (cpuValuesList == null || cpuValuesList.size() == 0) {
			Log.printLine("cpuValuesList is null ");
			return null;
		}
		// Log.printLine("********总共进行了" + cpuValuesList.get(0).size()
		// + "次测量*********");

		ArrayList cpuShares = new ArrayList<>();
		ArrayList memShares = new ArrayList<>();
		int usedNum = 0;
		for (int i = 0; i < cpuValuesList.size(); i++) {
			if (cpuValuesList.get(i) == null
					|| cpuValuesList.get(i).size() == 0)
				continue;
			
			usedNum++;
			
			ArrayList cpuValues = cpuValuesList.get(i);
			ArrayList memValues = memValuesList.get(i);
			ArrayList timeValues = timeList.get(i);
			// excel.createCellAndSetCellValue(1,i*2+1 , cpuValues);
			// excel.createCellAndSetCellValue(1,i*2+2 , memValues);
//			Log.printLine(indent + "主机" + i );
			ArrayList energyValues = new ArrayList<>();
			 Log.printLine(indent + i + indent + indent);
			double cpuS = 0, memS = 0,totalTime = 0;
			double power = 0;//当前时间段，一个主机的功率
			double aEnergy = 0;//计算一个主机的能耗
			for (int k = 0; k < cpuValues.size(); k++) {
				double timeSpan = (double)timeValues.get(k+1)- (double)timeValues.get(k);
				power = A*(double) cpuValues.get(k)+B;
				aEnergy+=power*timeSpan;
				cpuS +=((double) cpuValues.get(k))*timeSpan;
				
				memS +=((double) memValues.get(k))*timeSpan;
				totalTime+=timeSpan;
				 Log.printLine(indent + df.format(cpuValues.get(k))+indent + df.format(memValues.get(k))+
						 indent + df.format(timeSpan));
			}
			cpuS = cpuS /totalTime;
			memS = memS /totalTime;
			energyValues.add(aEnergy);
			cpuShares.add(cpuS);
			memShares.add(memS);
			
			energy += aEnergy;
			
		}
		energy = energy/(3600.0*1000.0);
		
//		for (Double aEnergy : energyValues) {
//			
//		}
		
		//设置本次调度使用过的主机数
		usedHosts = usedNum;
		double cpuValue = 0;
		double memValue = 0;
		double hostAvgSyn = 0;
		ArrayList<Double> hostAvgSynList = new ArrayList<Double>();
		for (int i = 0; i < cpuShares.size(); i++) {

			hostAvgSyn = ((double) cpuShares.get(i) + (double) memShares.get(i)) / 2.0;
			hostAvgSynList.add(hostAvgSyn);

			cpuValue += (double) cpuShares.get(i);
			memValue += (double) memShares.get(i);
		}

		double cpuAverage = cpuValue / cpuShares.size();
		double memAverage = memValue / memShares.size();
		double synthesize = (cpuAverage + memAverage) / 2;
		variance = 0.0;
		for (int i = 0; i < hostAvgSynList.size(); i++) {
			variance += (hostAvgSynList.get(i) - synthesize)
					* (hostAvgSynList.get(i) - synthesize);
		}
		variance = variance / hostAvgSynList.size();

		Log.printLine("cpu " + indent + "mem " + indent + "synthesize");
		Log.printLine(df.format(cpuAverage) + indent + df.format(memAverage)
				+ indent + df.format(synthesize));

		return synthesize;
	}

	public static void startPoster(final int size)
	{
		
		posterT  =  new Thread(new Runnable() {
			
			int count = CloudletsCount;
			@Override
			public void run() {
			
				
				while (count-->0) {
					try {
						
						Thread.sleep(GlobalParameter.TimeUnit
								* 10);
						sendRandomCloudlets(size);
						hasCloudlet = true;
					} catch (InterruptedException ex) {
						Logger.getLogger(
								DatacenterBrokerModifiedRealTime.class.getName())
								.log(Level.SEVERE, null, ex);
					}
				}
				if(count==0) stop();
			}
			public void stop()
			{
				CloudletsCount = 0;
			}
		});
		posterT.start();
	}

}

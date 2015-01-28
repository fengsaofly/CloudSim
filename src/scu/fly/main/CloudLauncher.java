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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
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
	 public static int curHostId = 0;
	public static int mutiples = 1;
	public static Double variance = 0.0;
	public static Double averageWaitingTime = 0.0;
	public static int WriteNums = 10;
	public static Date StartTime = new Date();
	public static boolean hasCloudlet = false;// 判断是否有任务
	public static int CloudletsCount = 10;
	public static int usedHosts = 0;
	public static double energy = 0;
	public static double A = 333.21;
	public static double B = 86.495;
	public static double curCPU = 0;
	public static double curMEM = 0;
	
	/** The cloudlet list. */
	private static CopyOnWriteArrayList<woCloudlet> cloudletList = new CopyOnWriteArrayList<woCloudlet>();
	/** The vmlist. */
	private static CopyOnWriteArrayList<myVm> vmlist = new CopyOnWriteArrayList<myVm>();

	public static SimpleExcelWrite excelWrite = SimpleExcelWrite.getInstance();
	public static Thread posterT = null;

	// private static List<myVm> createVM(int userId, int vms, int idShift) {
	// // Creates a container to store VMs. This list is passed to the broker
	// // later
	// LinkedList<myVm> list = new LinkedList<myVm>();
	//
	// // VM Parameters
	// long size = 10000; // image size (MB)
	// int ram = 512; // vm memory (MB)
	// int mips = 250;
	// long bw = 1000;
	// int pesNumber = 1; // number of cpus
	// String vmm = "Xen"; // VMM name
	//
	// // create VMs
	// myVm[] vm = new myVm[vms];
	//
	// for (int i = 0; i < vms; i++) {
	// vm[i] = new myVm(idShift + i, userId, mips, pesNumber, ram, bw,
	// size, vmm, new CloudletSchedulerTimeShared());
	// list.add(vm[i]);
	// }
	//
	// return list;
	// }

	private static Vm createVM(int userId, int idShift, int type) {
		// Creates a container to store VMs. This list is passed to the broker
		// later

		LinkedList<myVm> list = new LinkedList<myVm>();
		int ram = GlobalParameter.VM_RAM[type], pesNumber = GlobalParameter.VM_PES[type];
		int storageSize = GlobalParameter.VM_SIZE[type];

		// VM Parameters
		long size = storageSize * UNIT; // image size (MB)
		int mips = 1000;// 相当于1GHz,因为1mips = 4MHz
		long bw = 1000;
		String vmm = "Xen"; // VMM name

		// create VMs
		Vm vm;

		vm = new myVm(idShift, userId, mips, pesNumber, ram, bw, size, vmm,
				new CloudletSchedulerTimeShared(), type);

		return vm;

	}

	private static woCloudlet createCloudlet(int userId, int cloudlets,
			int idShift, int size, int type) {
		// Creates a container to store Cloudlets
		LinkedList<woCloudlet> list = new LinkedList<woCloudlet>();

		// cloudlet parameters
		long length = 4000 * size;
		long fileSize = 50;
		long outputSize = 50;
		int pesNumber = GlobalParameter.CLOUDLET_PES[type];
		UtilizationModel utilizationModel = new UtilizationModelFull();

		woCloudlet cloudlet;

		cloudlet = new woCloudlet(idShift, length, pesNumber, fileSize,
				outputSize, utilizationModel, utilizationModel,
				utilizationModel);
		// setting the owner of these Cloudlets
		cloudlet.setUserId(userId);

		return cloudlet;
	}

	private static int randGenerateCloudletSize() {
		double rand = Math.random();
		int randSize;
		if (rand < 0.7)
			randSize = MathUtil.random(1, 50);
		else if (rand > 0.85)
			randSize = MathUtil.random(451, 500);
		else
			randSize = MathUtil.random(251, 300);
		return randSize;
	}

	private static void generateStableVmsAndCloudlets(int size) {
		vmlist.clear();
		cloudletList.clear();

		int[] lengthArray = { 40, 10, 120, 120, 160, 160, 1920, 540, 1120, 1040 };

		int userId = 3;
		if (broker != null)
			userId = broker.getId();

		for (int i = 0; i < size; i++) {

			int randomCloudletSize = lengthArray[i];
			myVm curVm = (myVm) createVM(userId, vmId, i % 2);
			vmlist.add(curVm);

			woCloudlet curCloudlet = createCloudlet(userId, 1, vmId++,
					randomCloudletSize, i % 2);

			// 绑定VM
			curCloudlet.setVmId(curVm.getId());
			curCloudlet.setVmType(curVm.getVmType());
			curCloudlet.setVm(curVm);

			cloudletList.add(curCloudlet);

		}

	}

	private static void generateVmsAndCloudlets(int size) {
		vmlist.clear();
		cloudletList.clear();

		int userId = 3;
		if (broker != null)
			userId = broker.getId();


		while (size > 0) {

			int type = MathUtil.random(0, 3);

			int randomCloudletSize = randGenerateCloudletSize();

			myVm curVm = (myVm) createVM(userId, vmId, type);
			vmlist.add(curVm);

			woCloudlet curCloudlet = createCloudlet(userId, 1, vmId++,
					randomCloudletSize, type);

			// 绑定VM
			curCloudlet.setVmId(curVm.getId());
			curCloudlet.setVmType(curVm.getVmType());
			curCloudlet.setVm(curVm);

			cloudletList.add(curCloudlet);
			size--;
			if (size == 0)
				break;

		}

	}

	// private static ArrayList<List<Integer>> generateVmsAndCloudlets(int size)
	// {
	// vmlist.clear();
	// cloudletList.clear();
	//
	// double[] config = new double[GlobalParameter.VM_TYPES];
	//
	// double[] config = { 1, (double) 1 / 3, (double) 2 / 3 };
	// double[] probabilities = new double[3];
	// double arg = 0.5;
	// List<List<Integer>> T = new ArrayList<List<Integer>>();
	//
	// ArrayList<Integer> vmlist1 = new ArrayList<Integer>();
	// ArrayList<Integer> vmlist2 = new ArrayList<Integer>();
	// ArrayList<Integer> vmlist3 = new ArrayList<Integer>();
	// T.add(vmlist1);
	// T.add(vmlist2);
	// T.add(vmlist3);
	//
	// int userId = 3;
	// if (broker != null)
	// userId = broker.getId();
	//
	// // else if(simpleBroker!=null)
	// // userId = simpleBroker.getId();
	//
	// double rand = 0;
	//
	// for (int i = 0; i < config.length; i++) {
	// probabilities[i] = config[i] * arg / 130.5 * 100;
	// }
	// while (size > 0) {
	//
	// for (int vmlistId = 0; vmlistId < probabilities.length; vmlistId++) {
	// rand = Math.random();
	// if (rand < probabilities[vmlistId]) {
	//
	// int randomCloudletSize = randGenerateCloudletSize();
	// T.get(vmlistId).add(randomCloudletSize);
	// myVm curVm = (myVm) createVM(userId, vmId, vmlistId);
	// vmlist.add(curVm);
	//
	// woCloudlet curCloudlet = createCloudlet(userId, 1, vmId++,
	// randomCloudletSize, vmlistId).get(0);
	//
	// //绑定VM
	// curCloudlet.setVmId(curVm.getId());
	// curCloudlet.setVmType(curVm.getVmType());
	// curCloudlet.setVm(curVm);
	//
	//
	// cloudletList.add(curCloudlet);
	// size--;
	// if (size == 0)
	// break;
	// }
	//
	// }
	//
	// }
	//
	// return (ArrayList<List<Integer>>) T;
	//
	// }

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
				myHost.scheduleMethod = 2;
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

			calculateAndPrintValues(myHost.cpuValuesList,
					myHost.memValuesList, myHost.timeValuesList,myHost.powerValuesList);
			return printCloudletList(newList, name);
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
			// 启动任务发射器
			startPoster(size);

			while (!hasCloudlet) {
				Log.printLine("######等待任务出现#######");
			}
			Log.printLine("######有任务出现，启动CloudSim#######");
			MyCloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<woCloudlet> newList = broker.getCloudletReceivedList();

			MyCloudSim.stopSimulation();

			calculateAndPrintValues(myHost.cpuValuesList,
					myHost.memValuesList, myHost.timeValuesList,myHost.powerValuesList);
			
			datacenter0.printDebts();
			
			return printCloudletList(newList, name);

			// Print the debt of each user to each datacenter
			

			
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

			
			calculateAndPrintValues(myHost.cpuValuesList,
					myHost.memValuesList, myHost.timeValuesList, myHost.powerValuesList);
			// Print the debt of each user to each datacenter
			datacenter0.printDebts();

			return printCloudletList(newList, name);
		}
		return null;
	}

	public synchronized static void sendRandomCloudlets(int size) {

		Log.printLine("------共产生" + size + "个任务------");
		generateVmsAndCloudlets(size);
		// generateStableVmsAndCloudlets(size);
		if (cloudletList.size() > 0 && vmlist.size() > 0) {
			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);
			// bindCloudletToVm();
		}
	}

	// //////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		Log.printLine("Starting CloudSim...");

		try {
			
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.

			for (mutiples = 1; mutiples < 11; mutiples ++) {

				// Second step: Create Datacenters
				// Datacenters are the resource providers in CloudSim. We need
				// at list one of them to run a CloudSim simulation

				// 随机生成任务
				int size = 10 * mutiples;
				int totalSize = size * CloudletsCount;
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
				ArrayList<Double> cpuList = new ArrayList<>();
				cpuList.add((double) totalSize);
				ArrayList<Double> memList = new ArrayList<>();
				memList.add((double) totalSize);
				// broker = createBroker("Broker_0", "fly");
 
				// generateVmsAndCloudlets(size);
				for (int type = 0; type < GlobalParameter.algorithm.length; type++) {

					recoverConfig();
					String policy = GlobalParameter.algorithm[type];
					double utilization = 0;
					utilization = runOneMethod(policy, size);

					utilizationList.add(utilization);
					varianceList.add(variance);
					avgWaitTimeList.add(averageWaitingTime / totalSize);
					usedHostsList.add((double) usedHosts);
					energyList.add(energy);
					cpuList.add(curCPU);
					memList.add(curMEM);
				}

				try {
					// 记录利用率
					excelWrite.setCurRow(0);
					excelWrite.writeColumn(varianceList, false);
					excelWrite.setCurRow(6);
					excelWrite.writeColumn(usedHostsList, false);
					excelWrite.setCurRow(12);
					excelWrite.writeColumn(energyList, false);
					excelWrite.setCurRow(18);
					excelWrite.writeColumn(utilizationList, false);
					excelWrite.setCurRow(24);
					excelWrite.writeColumn(cpuList, false);
					excelWrite.setCurRow(30);
					excelWrite.writeColumn(memList, false);
					excelWrite.setCurRow(36);
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

//	private static void recoverVmsAndCloudlets() {
//		// TODO Auto-generated method stub
//		for (woCloudlet cloudlet : cloudletList) {
//			cloudlet.setHostID(-1);
//			cloudlet.setVmType(-1);
//		}
//		for (myVm vm : vmlist) {
//			vm.setHost(null);
//			vm.setHostID(-1);
//		}
//	}

	private static List<Host> createHosts(int type,int hostNum, String policy) {
		
		if(hostNum <= 0 ) return null;
		List<Host> hostList = new ArrayList<Host>();

		
		int peList[] = new int[hostNum];
		for (int i = 0; i < hostNum; i++)
			peList[i] = GlobalParameter.HOST_PES[type];

		int mips = GlobalParameter.HOST_MIPS[type];

		int ram = GlobalParameter.HOST_RAM[type]; // host memory (MB)
		long storage = GlobalParameter.HOST_STORAGE[type]; // host storage
		int bw =  GlobalParameter.HOST_BW; 

		for (int peNumPerHost : peList) {
			List<Pe> peNumForPerHost = new ArrayList<Pe>();
			int peId = 0;
			while (peId < peNumPerHost) {

				peNumForPerHost
						.add(new Pe(peId, new PeProvisionerSimple(mips)));
				peId++;
			}
			myHost h = null;
			if (policy.equals("fly") || policy.equals("other")) {
				h = new myHost(curHostId++, new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw), storage, peNumForPerHost,
						new VmSchedulerTimeShared(peNumForPerHost), GlobalParameter.HOST_POWER[type],type);
			} else if (policy.equals("rr") || policy.equals("random")) {
				h = new myHost(curHostId++, new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw), storage, peNumForPerHost,
						new VmSchedulerTimeShared(peNumForPerHost),  GlobalParameter.HOST_POWER[type],type);
			} else {
				h = new myHost(curHostId++, new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw), storage, peNumForPerHost,
						new VmSchedulerTimeShared(peNumForPerHost),  GlobalParameter.HOST_POWER[type],type);
				
			}
			h.r = (ram+mips*GlobalParameter.HOST_PES[type])*1.0/2;
			hostList.add(h);
		}

		Log.printLine("创建主机数目为：" + hostList.size());
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		return hostList;

	}

	private static Datacenter createDatacenter(String name, String policy) {
		
		curHostId = 0;
		
		
		List<Host> hostList = new ArrayList<Host>(HOSTNUM);
		
		int[] hostNums = new int[GlobalParameter.HOST_TYPES];
		int num = 0;
		for (int i = 0; i < GlobalParameter.HOST_TYPES; i++) {
			hostNums[i] = HOSTNUM/(hostNums.length+1);
			hostList.addAll(createHosts(i,hostNums[i], policy));
			num+=hostNums[i];
		}
		hostList.addAll(createHosts(1,HOSTNUM-num, policy));
		
		for (Host host : hostList) {
			myHost host1 = (myHost)host;
			myHost.hostTypeList.add(host1.getHostType());
		}
		
		List<Host> sortedHostList = new ArrayList<Host>(HOSTNUM);
		
		int number = 0;
		while (hostList.size()>0) {
			int i = MathUtil.random(0, hostList.size()-1);
			Host host = hostList.remove(i);
			host.setId(number++);
			sortedHostList.add(host);
		}
		
		System.out.println(hostList.size());
		
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
				arch, os, vmm, sortedHostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			if (policy.equals("random"))
				datacenter = new myDatacenter(name, characteristics,
						new VmAllocationPolicyRandom(sortedHostList), storageList, 0);
			else if (policy.equals("rr")) {
				datacenter = new myDatacenter(name, characteristics,
						new VmAllocationPolicyRoundRobin(sortedHostList),
						storageList, 0);
			} else if (policy.equals("other")) {
				datacenter = new myDatacenter(name, characteristics,
						new VmAllocationPolicyMy(sortedHostList), storageList, 0);
			} else if (policy.equals("fly")) {
				datacenter = new myDatacenter(name, characteristics,
						new VmAllocationPolicyMy(sortedHostList), storageList, 0);
			} else if (policy.equals("li")) {
				datacenter = new myDatacenter(name, characteristics,
						new VmAllocationPolicyLi(sortedHostList), storageList, 0);
			} else {
				datacenter = new myDatacenter(name, characteristics,
						new VmAllocationPolicySimple(sortedHostList), storageList, 0);
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
			else if (policy.equals("rr") || policy.equals("random")
					|| policy.equals("li"))
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
	private static double printCloudletList(List<woCloudlet> list, String name) {
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
				+ indent+  indent + "Wait Time"	+ indent+ indent+ "host ID" + indent + "vmType");

		DecimalFormat dft = new DecimalFormat("###.##");
		averageWaitingTime = 0.0;

		 Collections.sort(list);


		
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == woCloudlet.SUCCESS) {
				Log.print(" SUCCESS");

				Log.printLine(indent + indent + indent
						+ cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getVmId() + indent + indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + indent
						+ dft.format(cloudlet.getExecStartTime()) + indent
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime())+ indent + indent
						+ dft.format(cloudlet.getWaitTime()) + indent
						+ indent + indent + dft.format(cloudlet.getHostID())
						+ indent + indent + indent
						+ dft.format(cloudlet.getVmType()));
				averageWaitingTime += cloudlet.getWaitTime();
			}
		}
		averageWaitingTime = averageWaitingTime / size;
		
		
		 HashMap<Integer, List<woCloudlet>> map = new HashMap<>();
			
		 for (woCloudlet clt : list) {
			 //查看是否已存在
			 List<woCloudlet> l =  map.get(clt.getHostID());
			 //如果没有对应的host，加入
			 if(l == null)
			 {
				 l = new ArrayList<woCloudlet>();
				 l.add(clt);
				 map.put(clt.getHostID(), l);
			 }
			 else{
				 l.add(clt);
			 }


		}
		 ArrayList<Double> cpus = new ArrayList<Double>();
		 ArrayList<Double> mems = new ArrayList<Double>();
		 for (Entry<Integer, List<woCloudlet>> entry : map.entrySet()) {
			   List<woCloudlet> l = entry.getValue();

				double time = 0;
				double aHostCpuS = 0;
				double aHostMemS = 0;
				double aEnergy = 0;
				for (woCloudlet selectedClt : l) {
					aHostCpuS+=selectedClt.getCpuS()*selectedClt.getActualCPUTime();
					aHostMemS+=selectedClt.getMemS()*selectedClt.getActualCPUTime();
					aEnergy+=(selectedClt.getCpuS()*GlobalParameter.HOST_POWER_FACTOR[selectedClt.getHostType()]+
													GlobalParameter.HOST_POWER_CONSTANT)*selectedClt.getActualCPUTime();;
//					time+=selectedClt.getActualCPUTime();
				}
				time = l.get(l.size()-1).getFinishTime()-l.get(0).getExecStartTime();
				aHostCpuS = aHostCpuS / time;
				aHostMemS = aHostMemS / time;
				if(aHostCpuS > 1 || aHostMemS > 1 )
					System.out.println("出错了~~~~~~");
				cpus.add(aHostCpuS);
				mems.add(aHostMemS);
		}
		double 	totalCpuS = 0;
		double 	totalMemS = 0;
		ArrayList<Double> hostAvgSynList = new ArrayList<Double>();
		for (int i = 0; i < cpus.size(); i++) {
			totalCpuS+=cpus.get(i);
			totalMemS+=mems.get(i);
			double hostAvgSyn = (cpus.get(i) + mems.get(i)) / 2.0;
			hostAvgSynList.add(hostAvgSyn);
		}
		totalCpuS= totalCpuS / cpus.size();
		totalMemS= totalMemS / cpus.size();
		
		curCPU = totalCpuS;
		curMEM = totalMemS;
		
		double synthesize = (totalCpuS+totalMemS)/2.0;

		variance = 0.0;
		for (int i = 0; i < hostAvgSynList.size(); i++) {
			variance += (hostAvgSynList.get(i) - synthesize)
					* (hostAvgSynList.get(i) - synthesize);
		}
		variance = variance / hostAvgSynList.size();

		Log.printLine("cpu " + indent + "mem " + indent + "synthesize");
		DecimalFormat df = new DecimalFormat("0.0000");
		Log.printLine(df .format(totalCpuS) + indent + df.format(totalMemS)
				+ indent + df.format(synthesize));
		
		return synthesize;

	}

	public static void calculateAndPrintValues(ArrayList<ArrayList> cpuList,
			ArrayList<ArrayList> memList, ArrayList<ArrayList> timeList,ArrayList<ArrayList> powerList) {
		DecimalFormat df = new DecimalFormat("0.0000");
		String indent = "    ";
		ArrayList<ArrayList> cpuValuesList = cpuList;
		ArrayList<ArrayList> memValuesList = memList;

		if (cpuValuesList == null || cpuValuesList.size() == 0) {
			Log.printLine("cpuValuesList is null ");
			return ;
		}
		// Log.printLine("********总共进行了" + cpuValuesList.get(0).size()
		// + "次测量*********");

		ArrayList cpuShares = new ArrayList<>();
		ArrayList memShares = new ArrayList<>();
		ArrayList<Integer> usedHostTypeList = new ArrayList<>();
		int usedNum = 0;
		int totalPes = 0;
		int totalMem = 0;
		for (int i = 0; i < cpuValuesList.size(); i++) {
			if (cpuValuesList.get(i) == null
					|| cpuValuesList.get(i).size() == 0)
				continue;

			usedNum++;
			usedHostTypeList.add(myHost.hostTypeList.get(i));
			totalPes += GlobalParameter.HOST_PES[myHost.hostTypeList.get(i)];
			totalMem += GlobalParameter.HOST_RAM[myHost.hostTypeList.get(i)];
			ArrayList cpuValues = cpuValuesList.get(i);
			ArrayList memValues = memValuesList.get(i);
			ArrayList timeValues = timeList.get(i);
			ArrayList powerValues = powerList.get(i);
			// excel.createCellAndSetCellValue(1,i*2+1 , cpuValues);
			// excel.createCellAndSetCellValue(1,i*2+2 , memValues);
			// Log.printLine(indent + "主机" + i );
			ArrayList energyValues = new ArrayList<>();
			Log.printLine(indent + i + indent + indent);
			double cpuS = 0, memS = 0, totalTime = 0;
			double power = 0;// 当前时间段，一个主机的功率
			double aEnergy = 0;// 计算一个主机的能耗
			for (int k = 0; k < cpuValues.size(); k++) {
				double timeSpan = (double) timeValues.get(k + 1)
						- (double) timeValues.get(k);
				power = (double)powerValues.get(k);
				aEnergy += power * timeSpan;
				cpuS += ((double) cpuValues.get(k)) * timeSpan;

				memS += ((double) memValues.get(k)) * timeSpan;
				totalTime += timeSpan;
				Log.printLine(indent + df.format(cpuValues.get(k)) + indent
						+ df.format(memValues.get(k)) + indent
						+ df.format(timeSpan));
			}
			cpuS = cpuS / totalTime;
			
			memS = memS / totalTime;

			
			energyValues.add(aEnergy);
			cpuShares.add(cpuS);
			memShares.add(memS);

			energy += aEnergy;

		}
		energy = energy / (3600.0 * 1000.0);

		// for (Double aEnergy : energyValues) {
		//
		// }

		// 设置本次调度使用过的主机数
		usedHosts = usedNum;
//		double cpuValue = 0;
//		double memValue = 0;
//		double hostAvgSyn = 0;
//		ArrayList<Double> hostAvgSynList = new ArrayList<Double>();
//		for (int i = 0; i < cpuShares.size(); i++) {
//			//主机的利用率*主机的资源数
//			double cpuS = (double) cpuShares.get(i) * GlobalParameter.HOST_PES[usedHostTypeList.get(i)];
//			double memS = (double) memShares.get(i) * GlobalParameter.HOST_RAM[usedHostTypeList.get(i)];
//			
//			hostAvgSyn = ((double) cpuShares.get(i) + (double) memShares.get(i)) / 2.0;
//			hostAvgSynList.add(hostAvgSyn);
//
//			cpuValue += cpuS;
//			memValue += memS;
//		}
//
//		double cpuAverage = cpuValue / totalPes;
//		double memAverage = memValue / totalMem;
//		double synthesize = (cpuAverage + memAverage) / 2;


	}

	public static void startPoster(final int size) {

		posterT = new Thread(new Runnable() {

			int count = CloudletsCount;

			@Override
			public void run() {

				while (count-- > 0) {
					try {

						Thread.sleep(GlobalParameter.TimeUnit * 10);
						sendRandomCloudlets(size);
						hasCloudlet = true;
					} catch (InterruptedException ex) {
						Logger.getLogger(
								DatacenterBrokerModifiedRealTime.class
										.getName()).log(Level.SEVERE, null, ex);
					}
				}
				if (count == 0)
					stop();
			}

			public void stop() {
				CloudletsCount = 0;
			}
		});
		posterT.start();
	}

}

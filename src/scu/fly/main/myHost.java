package scu.fly.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

import jxl.write.WriteException;

public class myHost extends PowerHost implements Runnable {

	private ArrayList<int[]> vmTotalConfigs;
	protected ArrayList<int[]> curVmAvailableConfig;

	public void setCurVmAvailableConfig(ArrayList<int[]> curVmAvailableConfig) {
		this.curVmAvailableConfig = curVmAvailableConfig;
	}

	protected ArrayList<double[]> vmMCSList;
	protected List<Integer> curQueueLenList;
	protected List<List<myVm>> waitVmsQueue;
	protected List<myVm> curChosenQueue;
	private Timer timer = null;
	private int processNum = 0;
	private boolean timerStarted = false;
	public static SimpleExcelWrite excelWrite = SimpleExcelWrite.getInstance();
	private int count = 1;
	public static boolean firstNotification = true;
	private static myDatacenter mc = null;
	private static double curSynUtilization1 = 0.0;
	private static double curSynUtilization2 = 0.0;
	public static int scheduleMethod = 0;

	public int row = 1;

	public static ArrayList<ArrayList> cpuValuesList;
	public static ArrayList<ArrayList> memValuesList;
	public static ArrayList<Double> sysUtilizationValues;
	
	public  double IR = 0;
	public  double ILB = 0;
	public  double cpuU = 0;
	public  double memU = 0;

	// private myThread thread = new myThread(this);

	public myHost(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler,
			PowerModel powerModel) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler,
				powerModel);

		vmTotalConfigs = new ArrayList<int[]>();
		vmTotalConfigs.add(new int[] { 2, 0, 0 });
		vmTotalConfigs.add(new int[] { 0, 1, 1 });
		vmTotalConfigs.add(new int[] { 1, 0, 1 });

		curVmAvailableConfig = new ArrayList<int[]>(vmTotalConfigs.size());
		curVmAvailableConfig = (ArrayList<int[]>) vmTotalConfigs.clone();

		curChosenQueue = new ArrayList<myVm>();

		vmMCSList = new ArrayList<double[]>();
		vmMCSList.add(new double[] { 15, 17.1, 7 });
		vmMCSList.add(new double[] { 8, 6, 20 });

		this.initialQueue();
		this.initialQueueLenth();

		if (cpuValuesList == null || cpuValuesList.size() == 0) {
			cpuValuesList = new ArrayList<ArrayList>();
			for (int i = 0; i < 100; i++) {
				cpuValuesList.add(new ArrayList());
			}
		}
		if (memValuesList == null || memValuesList.size() == 0) {
			memValuesList = new ArrayList<ArrayList>();
			for (int i = 0; i < 100; i++) {
				memValuesList.add(new ArrayList());
			}
		}

		sysUtilizationValues = new ArrayList<Double>();

	}

	private void initialQueueLenth() {

		curQueueLenList = new ArrayList<Integer>(waitVmsQueue.size());
		for (int i = 0; i < waitVmsQueue.size(); i++) {
			curQueueLenList.add(0);
		}
	}

	private void initialQueue() {

		waitVmsQueue = new ArrayList<List<myVm>>(3);

		ArrayList<myVm> vmlist1 = new ArrayList<myVm>();
		// waitVmsQueue.set(0, vmlist1);
		ArrayList<myVm> vmlist2 = new ArrayList<myVm>();
		ArrayList<myVm> vmlist3 = new ArrayList<myVm>();
		waitVmsQueue.add(vmlist1);
		waitVmsQueue.add(vmlist2);
		waitVmsQueue.add(vmlist3);
	}

	public ArrayList<int[]> getVmConfig() {
		return vmTotalConfigs;

	}

	public void resetCurVmConfig() {
		curVmAvailableConfig.clear();
		curVmAvailableConfig = (ArrayList<int[]>) vmTotalConfigs.clone();
	}

	public void updateVmAvailableConfig(List<Vm> vmList) {
		int[] curVmList = new int[] { 0, 0, 0 };
		ArrayList<int[]> tempVmConfig = new ArrayList<int[]>();
		int distance = 0;

		for (Vm vm : vmList) {
			myVm curVm = (myVm) vm;
			curVmList[curVm.getVmType()]++;
		}
		// curVmList =new int[]{2,0,0};
		for (int index = 0; index < curVmAvailableConfig.size(); index++) {

			int[] tempAvailableConfig = curVmAvailableConfig.get(index).clone();
			// 如果curVmList

			for (int vmId = 0; vmId < tempAvailableConfig.length; vmId++) {

				tempAvailableConfig[vmId] = tempAvailableConfig[vmId]
						+ curVmList[vmId];

			}
			for (int[] oneTopConfig : vmTotalConfigs) {
				if (Arrays.equals(oneTopConfig, tempAvailableConfig)) {
					curVmAvailableConfig.clear();
					curVmAvailableConfig = (ArrayList<int[]>) vmTotalConfigs
							.clone();
					return;
				}
			}

			tempVmConfig.add(tempAvailableConfig);

		}
		curVmAvailableConfig.clear();
		curVmAvailableConfig = (ArrayList<int[]>) tempVmConfig.clone();

	}

	public void updateVmAvailableConfig() {
		int[] curVmList = new int[] { 0, 0, 0 };
		ArrayList<int[]> tempVmConfig = new ArrayList<int[]>();
		int distance = 0;

		for (Vm vm : getVmList()) {
			myVm curVm = (myVm) vm;
			curVmList[curVm.getVmType()]++;
		}
		// curVmList =new int[]{2,0,0};
		for (int index = 0; index < vmTotalConfigs.size(); index++) {

			int[] tempTotalConfig = vmTotalConfigs.get(index).clone();
			if (tempTotalConfig.equals(curVmList)) {
				curVmAvailableConfig.clear();
				curVmAvailableConfig.add(new int[] { 0, 0, 0 });
				return;
			}
			for (int vmId = 0; vmId < tempTotalConfig.length; vmId++) {

				distance = tempTotalConfig[vmId] - curVmList[vmId];
				if (distance < 0)
					break;
				tempTotalConfig[vmId] = distance;
			}
			if (distance >= 0)
				tempVmConfig.add(tempTotalConfig);

		}
		curVmAvailableConfig.clear();
		curVmAvailableConfig = (ArrayList<int[]>) tempVmConfig.clone();

	}

	public ArrayList<int[]> getCurVmAvailableConfig() {
		return curVmAvailableConfig;

	}

	public boolean setCurQueueLenList(List<Integer> curQueueLenList) {
		if (curQueueLenList != null && !curQueueLenList.equals("")) {
			this.curQueueLenList = curQueueLenList;
			return true;
		}
		return false;
	}

	// 将Vm加入等待队列
	public boolean addVm(myVm vm) {
		int typeId = vm.getVmType();
		boolean successUpdateQueneLen = false;
		boolean successCreateVm = true;
		waitVmsQueue.get(typeId).add(vm);

		// vm.setHost(this);
		successUpdateQueneLen = updateCurQueueLenList(typeId);
		if (!successUpdateQueneLen) {
			Log.printLine("updateCurQueueLenList error!");
			return successUpdateQueneLen;
		}
		// beginVMsAllocation();
		// if(!timerStarted)
		// {
		// Log.printLine("主机"+this.getId()+"启动了");
		// timer = new Timer();
		// timer.schedule(this,(long)0,
		// (long)GlobalParameter.SLOT);//延后三秒钟进行vm的分配
		//
		// timerStarted = true;
		// }
		// thread.start();

		return successCreateVm;

	}

	public static synchronized void sendMsgToDC(myVm vm) {
		Log.printLine("-----创建vm" + vm.getId() + "成功-------");
		getMc().getVmExcuteThread().setCurCreatedVm(vm);
		getMc().getVmExcuteThread().run();
	}

	private void beginVMsAllocation() {
		boolean successGetConfig = false;
		List<List<myVm>> finalWQ = new ArrayList<List<myVm>>(waitVmsQueue);
		List<myVm> finalCQ = new ArrayList<myVm>(curChosenQueue);
		List<List<myVm>> waitVmsQueue1 = new ArrayList<List<myVm>>(
				waitVmsQueue.size());

		for (int i = 0; i < waitVmsQueue.size(); i++) {

			List<myVm> list = new ArrayList<>(waitVmsQueue.get(i).size());
			List<myVm> list2 = waitVmsQueue.get(i);
			for (myVm myVm : list2) {
				try {
					list.add(myVm.clone());
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			waitVmsQueue1.add(list);
		}
		List<myVm> curChosenQueue1 = new ArrayList<myVm>(curChosenQueue.size());

		for (myVm myVm : curChosenQueue) {
			try {
				curChosenQueue1.add(myVm.clone());
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		switch (scheduleMethod) {
		case 0:
			successGetConfig = getConfigFromMySchedulingAlgorithm();
			finalWQ = new ArrayList<List<myVm>>(waitVmsQueue);
			finalCQ = new ArrayList<myVm>(curChosenQueue);
			waitVmsQueue = new ArrayList<List<myVm>>(waitVmsQueue1);
			curChosenQueue = new ArrayList<myVm>(curChosenQueue1);
			getConfigFromStochasticSchedulingAlgorithm();

			waitVmsQueue = finalWQ;
			curChosenQueue = finalCQ;
			compareMethod();
			break;
		case 1:
			successGetConfig = getConfigFromStochasticSchedulingAlgorithm();
			break;
		case 2:
			successGetConfig = getConfigFroMIUS();
			break;	
		default:
			break;
		}

		if (!successGetConfig) {
			Log.printLine("在主机" + this.getId() + "上获取可用配置失败！");
			return;
		}

		// 如果成功获取配置,则创建选择的vms，并在等待队列中删除选中的vms
		for (myVm vm : curChosenQueue) {
			if (vmCreate(vm))
				sendMsgToDC(vm);

		}
		// 创建vm之后，更新可用的vm配置——curVmAvailableConfigs
		updateVmAvailableConfig();

		// Log.printLine("主机"+this.getId()+"可用的配置为:");
		// 更新可用的虚拟机配置——curVmAvailableConfig

		// synchronized (curVmAvailableConfig) {
		// for(int[] config:getCurVmAvailableConfig())
		// {
		// if(config==null || Arrays.equals(new int[]{0,0,0},config))
		// {
		// Log.printLine("空");
		// break;
		// }
		// printArray(config);
		//
		//
		// }
		// }
		//
		// Log.printLine();

	}

	// 将Vm移出等待队列，Vm进入PM运行
	public void removeVm(myVm vm) {
		int typeId = vm.getVmType();
		if (waitVmsQueue.get(typeId).contains(vm)) {
			waitVmsQueue.get(typeId).remove(vm);
			updateCurQueueLenList(typeId);
		}
		// if(waitVmsQueue.isEmpty() || waitVmsQueue.get(0).isEmpty())
		// {
		// //等待队列为空
		// }

	}

	public List<Integer> getCurQueueLenList() {
		return this.curQueueLenList;
	}

	public boolean updateCurQueueLenList(int queueIndex) {
		if (curQueueLenList.size() > queueIndex) {
			curQueueLenList
					.set(queueIndex, waitVmsQueue.get(queueIndex).size());
			return true;
		} else {
			Log.printLine("队列索引出错！");
			return false;
		}
	}

	@Override
	// 将vm从等待队列放入PM
	public boolean vmCreate(Vm vm) {
		return super.vmCreate(vm);
	}

	public boolean getConfigFromMySchedulingAlgorithm() {

		int multiple = 2;
		double maxWeight = Integer.MIN_VALUE;
		int[] chosenConfig = new int[] { 0, 0, 0 };

		// 获取可用配置
		@SuppressWarnings("unchecked")
		ArrayList<int[]> actualAvailableConfig = new ArrayList<int[]>();
		for (int j = 0; j < curVmAvailableConfig.size(); j++) {
			int[] oneConfig = curVmAvailableConfig.get(j).clone();
			for (int i = 0; i < oneConfig.length; i++) {
				oneConfig[i] = Math.min(oneConfig[i], curQueueLenList.get(i));
			}
			actualAvailableConfig.add(oneConfig);
		}

		// 可用配置去重
		actualAvailableConfig = (ArrayList<int[]>) removeDuplicate(actualAvailableConfig);
		if (actualAvailableConfig.contains(new int[] { 0, 0, 0 }))
			return false;

		double maxWeightCPU = 0, maxWeightMEM = 0;
		boolean needHandleLimit = false;
		for (int i = 0; i < actualAvailableConfig.size(); i++) {
			int[] curAvailableConfig = actualAvailableConfig.get(i);
			// if(Arrays.equals(curAvailableConfig, new int[]{0,0,0})) return
			// false;
			double tempWeightCPU = 0, tempWeightMEM = 0, tempWeight = 0;
			for (int j = 0; j < curAvailableConfig.length; j++) {
				/**
				 * 阈值处理
				 * 如果某一队列达到阈值，则选取可用配置中包含该队列最多的一个配置——maxWeightConfigForVMType*
				 * */
				int threshold = multiple * maxWeightConfigForVMType(j)[j];
				// if(curAvailableConfig[j]>threshold ){
				//
				// needHandleLimit = true;
				//
				// chosenConfig = maxWeightConfigForVMType(j);
				//
				// break;
				// }
				// 否则，选取综合利用率最大的
				if (curAvailableConfig[j] == 0)
					continue;
				tempWeightMEM += curAvailableConfig[j] * vmMCSList.get(0)[j];
				tempWeightCPU += curAvailableConfig[j] * vmMCSList.get(1)[j];

			}
			// 需要处理阈值，直接跳出
			if (needHandleLimit) {
				maxWeightCPU = 0;
				maxWeightMEM = 0;
				for (int vmType = 0; vmType < chosenConfig.length; vmType++) {
					int choseNum = chosenConfig[vmType];
					for (int num = 0; num < choseNum; num++) {
						maxWeightCPU += vmMCSList.get(0)[vmType];
						maxWeightMEM += vmMCSList.get(1)[vmType];
					}
				}
				break;
			}
			// 选择最大的权重配置
			tempWeight = (tempWeightCPU / this.getNumberOfPes() + tempWeightMEM
					/ this.getRam() * 1000) / 2.0;
			if (tempWeight > maxWeight) {
				maxWeightCPU = tempWeightCPU;
				maxWeightMEM = tempWeightMEM;
				maxWeight = tempWeight;
				chosenConfig = curAvailableConfig.clone();
			}
		}
		maxWeightMEM = maxWeightMEM * 1000;// GB->MB
		for (Vm vm : this.getVmList()) {
			maxWeightCPU += vm.getNumberOfPes();
			maxWeightMEM += vm.getRam();
		}
		double cpuShare = maxWeightCPU / this.getNumberOfPes();

		double memShare = maxWeightMEM / this.getRam();// GB->MB
		double sysShare = (cpuShare + memShare) / 2.0;
		Log.printLine("Fly:当前主机" + this.getId() + "的cpu利用率为：" + cpuShare
				+ ",mem利用率为：" + memShare + "综合利用率为：" + sysShare);
		// 记录当前综合利用率
		curSynUtilization1 = sysShare;
		final int column = this.getId() * 2;
		if (column > 200)
			Log.printLine("column >200 error!");

		cpuValuesList.get(getId()).add(cpuShare);
		memValuesList.get(getId()).add(memShare);

		// 若chosenConfig不为空，则根据配置进行VM选择
		curChosenQueue.clear();

		for (int vmType = 0; vmType < chosenConfig.length; vmType++) {

			for (int numOfvmType = 0; numOfvmType < chosenConfig[vmType]; numOfvmType++) {
				int waitTypeLen = waitVmsQueue.get(vmType).size();
				if (waitTypeLen > 0) {
					curChosenQueue.add(waitVmsQueue.get(vmType).get(
							waitTypeLen - 1));
					removeVm(waitVmsQueue.get(vmType).get(waitTypeLen - 1));
				}

			}
		}
		// update available vm config ---curVmAvailableConfig
		// for(int[] curAvailableConfig : curVmAvailableConfig){
		// int tempWeight = 0;
		// for(int tempVmAvailableIndex = 0 ;tempVmAvailableIndex <
		// curAvailableConfig.length ; tempVmAvailableIndex++){
		//
		// }
		// }
		Log.printLine("chosenConfig = " + printArray(chosenConfig));
		// 查看是否有等待任务
		// for(List<myVm> vm:waitVmsQueue)
		// {
		// if(vm.size()!=0){
		// return true;
		// }
		// else continue;
		// }
		// Log.printLine("主机"+this.getId()+"暂时没有新任务！");
		return true;
	}

	public boolean getConfigFroMIUS() {

		double maxWeight = Integer.MIN_VALUE;
		int[] chosenConfig = new int[] { 0, 0, 0 };
		for (int[] curAvailableConfig : curVmAvailableConfig) {
			if (Arrays.equals(curAvailableConfig, new int[] { 0, 0, 0 }))
				return false;
			double tempWeight = 0;
			for (int tempVmAvailableIndex = 0; tempVmAvailableIndex < curAvailableConfig.length; tempVmAvailableIndex++) {
				tempWeight += curAvailableConfig[tempVmAvailableIndex]
						* curQueueLenList.get(tempVmAvailableIndex)*getVMImpactFactor(tempVmAvailableIndex);
			}
			if (tempWeight > maxWeight) {
				maxWeight = tempWeight;
				chosenConfig = curAvailableConfig.clone();
			}
		}
		for (int i = 0; i < chosenConfig.length; i++) {
			chosenConfig[i] = Math.min(chosenConfig[i], waitVmsQueue.get(i)
					.size());
		}
		double WeightCPU = 0, WeightMEM = 0;
		for (int i = 0; i < chosenConfig.length; i++) {
			for (int j = 0; j < chosenConfig[i]; j++) {
				WeightMEM += vmMCSList.get(0)[i];
				WeightCPU += vmMCSList.get(1)[i];
			}

		}

		WeightMEM = WeightMEM * 1000;
		// 若chosenConfig不为空，则根据配置进行VM选择
		for (Vm vm : this.getVmList()) {
			WeightCPU += vm.getNumberOfPes();
			WeightMEM += vm.getRam();
		}
		double cpuShare = WeightCPU / this.getNumberOfPes();
		double memShare = WeightMEM / this.getRam();// GB->MB
		double sysShare = (cpuShare + memShare) / 2.0;
		Log.printLine("other:当前主机" + this.getId() + "的cpu利用率为：" + cpuShare
				+ ",mem利用率为：" + memShare + "综合利用率为：" + sysShare);
		curSynUtilization2 = sysShare;
		cpuValuesList.get(getId()).add(cpuShare);
		memValuesList.get(getId()).add(memShare);

		curChosenQueue.clear();

		for (int vmType = 0; vmType < chosenConfig.length; vmType++) {

			for (int numOfvmType = 0; numOfvmType < chosenConfig[vmType]; numOfvmType++) {
				int waitTypeLen = waitVmsQueue.get(vmType).size();
				curChosenQueue.add(waitVmsQueue.get(vmType)
						.get(waitTypeLen - 1));
				removeVm(waitVmsQueue.get(vmType).get(waitTypeLen - 1));

			}
		}

		Log.printLine("chosenConfig = " + printArray(chosenConfig));

		return true;
	}

	private double getVMImpactFactor(int tempVmAvailableIndex) {
		double IF = 0;
		switch (tempVmAvailableIndex) {
		case 0:
			IF =  0.383;
			break;
		case 1:
			IF =  0.393;
			break;
		case 2:
			IF =  0.450;
			break;
		default:
			break;
		}
		return IF;
	}

	private boolean getConfigFromStochasticSchedulingAlgorithm() {

		int maxWeight = Integer.MIN_VALUE;
		int[] chosenConfig = new int[] { 0, 0, 0 };
		for (int[] curAvailableConfig : curVmAvailableConfig) {
			if (Arrays.equals(curAvailableConfig, new int[] { 0, 0, 0 }))
				return false;
			int tempWeight = 0;
			for (int tempVmAvailableIndex = 0; tempVmAvailableIndex < curAvailableConfig.length; tempVmAvailableIndex++) {
				tempWeight += curAvailableConfig[tempVmAvailableIndex]
						* curQueueLenList.get(tempVmAvailableIndex);
			}
			if (tempWeight > maxWeight) {
				maxWeight = tempWeight;
				chosenConfig = curAvailableConfig.clone();
			}
		}
		for (int i = 0; i < chosenConfig.length; i++) {
			chosenConfig[i] = Math.min(chosenConfig[i], waitVmsQueue.get(i)
					.size());
		}
		double WeightCPU = 0, WeightMEM = 0;
		for (int i = 0; i < chosenConfig.length; i++) {
			for (int j = 0; j < chosenConfig[i]; j++) {
				WeightMEM += vmMCSList.get(0)[i];
				WeightCPU += vmMCSList.get(1)[i];
			}

		}

		WeightMEM = WeightMEM * 1000;
		// 若chosenConfig不为空，则根据配置进行VM选择
		for (Vm vm : this.getVmList()) {
			WeightCPU += vm.getNumberOfPes();
			WeightMEM += vm.getRam();
		}
		double cpuShare = WeightCPU / this.getNumberOfPes();
		double memShare = WeightMEM / this.getRam();// GB->MB
		double sysShare = (cpuShare + memShare) / 2.0;
		Log.printLine("other:当前主机" + this.getId() + "的cpu利用率为：" + cpuShare
				+ ",mem利用率为：" + memShare + "综合利用率为：" + sysShare);
		curSynUtilization2 = sysShare;
		cpuValuesList.get(getId()).add(cpuShare);
		memValuesList.get(getId()).add(memShare);

		curChosenQueue.clear();

		for (int vmType = 0; vmType < chosenConfig.length; vmType++) {

			for (int numOfvmType = 0; numOfvmType < chosenConfig[vmType]; numOfvmType++) {
				int waitTypeLen = waitVmsQueue.get(vmType).size();
				curChosenQueue.add(waitVmsQueue.get(vmType)
						.get(waitTypeLen - 1));
				removeVm(waitVmsQueue.get(vmType).get(waitTypeLen - 1));

			}
		}

		Log.printLine("chosenConfig = " + printArray(chosenConfig));

		return true;
	}

	@Override
	public void run() {
		boolean needConfig = false;
		boolean hasVmsForWaiting = false;

		// Log.printLine("waitVmsQueue---"+processNum++);
		// 可用配置为空
		if (curVmAvailableConfig == null
				|| curVmAvailableConfig.size() == 0
				|| Arrays.equals(curVmAvailableConfig.get(0), new int[] { 0, 0,
						0 })) {
			if (firstNotification) {
				Log.printLine(this.getId() + "主机正在等待任务完成...");
				firstNotification = false;
			}
			return;
		}
		for (List<myVm> vm : waitVmsQueue) {
			// Log.print(vm.size()+" ");
			if (vm.size() > 0) {
				hasVmsForWaiting = true;
				break;// 判断waitVmsQueue是否为空
			}

		}
		if (!hasVmsForWaiting)
			return;// 如果没有等待任务
		// 如果有等待任务
		for (int[] oneAvailableConfig : curVmAvailableConfig) {

			for (int i = 0; i < curQueueLenList.size(); i++) {

				if (curQueueLenList.get(i) > 0 && oneAvailableConfig[i] > 0) {
					needConfig = true;
					break;
				}

			}
			if (needConfig)
				break;
		}

		// 等待任务可以完成，或者完成一部分
		if (needConfig) {

			beginVMsAllocation();
		}

	}

	public String printArray(int[] array) {
		// System.out.println("第一种方法： ");
		// 用泛型，1.5后可以这样使用：
		String arrayResult = "";
		for (int i : array) {
			arrayResult += i + " ";
		}
		// arrayResult+="\n";
		// Log.print(" [ "+arrayResult+" ] ");
		return arrayResult;
	}

	@Override
	/**
	 * Destroys a VM running in the host.
	 * 
	 * @param vm the VM
	 * @pre $none
	 * @post $none
	 */
	public void vmDestroy(Vm vm) {
		if (vm != null) {
			vmDeallocate(vm);
			getVmList().remove(vm);
			recoverHostConfig(vm);
			vm.setHost(null);

		}
	}

	// 更新主机的可用vm配置
	public void recoverHostConfig(Vm vm) {

		myHost host = (myHost) vm.getHost();
		if (host != null) {
			ArrayList<Vm> curVmList = new ArrayList<Vm>();
			curVmList.add(vm);
			host.updateVmAvailableConfig(curVmList);
			Log.printLine("主机" + host.getId() + "可用的配置已经更新!AvailableConfigs="
					+ host.getCurVmAvailableConfig());
		}
	}

	public int[] maxWeightConfigForVMType(int type) {
		int[] targetConfig = null;
		switch (type) {
		case 0:
			targetConfig = vmTotalConfigs.get(0).clone();
			break;
		case 1:
			targetConfig = vmTotalConfigs.get(1).clone();
			break;
		case 2:
			targetConfig = vmTotalConfigs.get(2).clone();
			break;
		}

		for (int i = 0; i < targetConfig.length; i++) {
			targetConfig[i] = Math.min(targetConfig[i], curQueueLenList.get(i));
		}
		return targetConfig;
	}

	public static List<int[]> removeDuplicate(List<int[]> list) {
		for (int i = 0; i < list.size(); i++) {
			int[] oneArray = list.get(i).clone();
			for (int j = i + 1; j < list.size(); j++) {
				int[] anotherArray = list.get(j).clone();
				if (Arrays.equals(oneArray, anotherArray)) {
					list.remove(j);
				}
			}
		}

		return list;

	}

	public void timerStop() {
		// Log.printLine("主机"+getId());
		if (timerStarted) {
			timer.cancel();
			// Log.printLine("主机"+getId()+"的timer关闭了·");
		}
	}

	public void compareMethod() {
		sysUtilizationValues.add(curSynUtilization1 - curSynUtilization2);
	}

	@Override
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;
		Iterator<Vm> iterator = new CopiedIterator(getVmList().iterator());

		while (iterator.hasNext()) {
			Vm vm = iterator.next();
			double time = vm.updateVmProcessing(currentTime, getVmScheduler()
					.getAllocatedMipsForVm(vm));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}

		}

		return smallerTime;
	}

	public static myDatacenter getMc() {
		return mc;
	}

	public static void setMc(myDatacenter mc) {
		myHost.mc = mc;
	}
	
	public boolean canCreate(Vm vm)
	{
		if (!getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam())) {
			return false;
		}
		if (!getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw())) {
			return false;
		}
		if (!getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
			return false;
		}
		return true;
	}

}

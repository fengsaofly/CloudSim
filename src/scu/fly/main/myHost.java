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
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

import jxl.write.WriteException;

public class myHost extends PowerHost implements Runnable {

//	private ArrayList<int[]> vmTotalConfigs;
	protected ArrayList<int[]> curVmAvailableConfig;
//	protected ArrayList<double[]> vmMCSList;
	protected List<Integer> curQueueLenList;
	protected List<List<myVm>> waitVmsQueue;
	protected List<myVm> curChosenQueue;

	public static SimpleExcelWrite excelWrite = SimpleExcelWrite.getInstance();

	public static boolean firstNotification = true;
	private static myDatacenter mc = null;
	private static double curSynUtilization1 = 0.0;
	private static double curSynUtilization2 = 0.0;
	public static int scheduleMethod = 0;
	public int row = 1;
	private int hostType = -1;

	public static ArrayList<ArrayList> cpuValuesList;
	public static ArrayList<ArrayList> memValuesList;
	public static ArrayList<ArrayList> powerValuesList;
	public static ArrayList<ArrayList> timeValuesList;
	public static ArrayList<Double> sysUtilizationValues;
	public static ArrayList<Integer> hostTypeList = new ArrayList<>();;
	
	public  double IR = 0;
	public  double ILB = 0;
	public  double cpuU = 0;
	public  double memU = 0;
	public 	double r = -1;//队列因子

	// private myThread thread = new myThread(this);

	public myHost(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler,
			PowerModel powerModel) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler,
				powerModel);

//		vmTotalConfigs = new ArrayList<int[]>();
//		vmTotalConfigs.add(new int[] { 2, 0, 0 });
//		vmTotalConfigs.add(new int[] { 0, 1, 1 });
//		vmTotalConfigs.add(new int[] { 1, 0, 1 });

//		curVmAvailableConfig = new ArrayList<int[]>(vmTotalConfigs.size());
//		curVmAvailableConfig = (ArrayList<int[]>) vmTotalConfigs.clone();
		
		curVmAvailableConfig = new ArrayList<int[]>();

		curChosenQueue = new ArrayList<myVm>();


		this.initialQueue();
		this.initialQueueLenth();

		if (cpuValuesList == null || cpuValuesList.size() == 0) {
			cpuValuesList = new ArrayList<ArrayList>();
			for (int i = 0; i < 100; i++) {
				cpuValuesList.add(new ArrayList());
			}
		}
		
		if (powerValuesList == null || powerValuesList.size() == 0) {
			powerValuesList = new ArrayList<ArrayList>();
			for (int i = 0; i < 100; i++) {
				powerValuesList.add(new ArrayList());
			}
		}
		
		if (timeValuesList == null || timeValuesList.size() == 0) {
			timeValuesList = new ArrayList<ArrayList>();
			for (int i = 0; i < 101; i++) {
				timeValuesList.add(new ArrayList());
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
	public myHost(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler,
			PowerModel powerModel,int type) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler,
				powerModel);
		
		curVmAvailableConfig = new ArrayList<int[]>();

		curChosenQueue = new ArrayList<myVm>();


		this.initialQueue();
		this.initialQueueLenth();

		if (cpuValuesList == null || cpuValuesList.size() == 0) {
			cpuValuesList = new ArrayList<ArrayList>();
			for (int i = 0; i < 100; i++) {
				cpuValuesList.add(new ArrayList());
			}
		}
		if (powerValuesList == null || powerValuesList.size() == 0) {
			powerValuesList = new ArrayList<ArrayList>();
			for (int i = 0; i < 100; i++) {
				powerValuesList.add(new ArrayList());
			}
		}
		if (timeValuesList == null || timeValuesList.size() == 0) {
			timeValuesList = new ArrayList<ArrayList>();
			for (int i = 0; i < 101; i++) {
				timeValuesList.add(new ArrayList());
			}
		}
		if (memValuesList == null || memValuesList.size() == 0) {
			memValuesList = new ArrayList<ArrayList>();
			for (int i = 0; i < 100; i++) {
				memValuesList.add(new ArrayList());
			}
		}

		sysUtilizationValues = new ArrayList<Double>();
		
		hostType = type;

		
		
		updateConfig();

	}

	private void initialQueueLenth() {

		curQueueLenList = new ArrayList<Integer>(GlobalParameter.VM_TYPES);
		for (int i = 0; i < GlobalParameter.VM_TYPES; i++) {
			curQueueLenList.add(0);
		}
	}

	private void initialQueue() {

		waitVmsQueue = new ArrayList<List<myVm>>(GlobalParameter.VM_TYPES);
		
		
		for (int i = 0; i < GlobalParameter.VM_TYPES; i++) {
			ArrayList<myVm> vmlist = new ArrayList<myVm>();
			waitVmsQueue.add(vmlist);
		}

	}

	public int getHostType() {
		return hostType;
	}

	public void setHostType(int hostType) {
		this.hostType = hostType;
	}
	
	public void setCurVmAvailableConfig(ArrayList<int[]> curVmAvailableConfig) {
		this.curVmAvailableConfig = curVmAvailableConfig;
	}
//	public ArrayList<int[]> getVmConfig() {
//		return vmTotalConfigs;
//
//	}
//
//	public void resetCurVmConfig() {
//		curVmAvailableConfig.clear();
//		curVmAvailableConfig = (ArrayList<int[]>) vmTotalConfigs.clone();
//	}

//	public void updateVmAvailableConfig(List<Vm> vmList) {
//		int[] curVmList = new int[] { 0, 0, 0 };
//		ArrayList<int[]> tempVmConfig = new ArrayList<int[]>();
//		int distance = 0;
//
//		for (Vm vm : vmList) {
//			myVm curVm = (myVm) vm;
//			curVmList[curVm.getVmType()]++;
//		}
//		// curVmList =new int[]{2,0,0};
//		for (int index = 0; index < curVmAvailableConfig.size(); index++) {
//
//			int[] tempAvailableConfig = curVmAvailableConfig.get(index).clone();
//			// 如果curVmList
//
//			for (int vmId = 0; vmId < tempAvailableConfig.length; vmId++) {
//
//				tempAvailableConfig[vmId] = tempAvailableConfig[vmId]
//						+ curVmList[vmId];
//
//			}
//			for (int[] oneTopConfig : vmTotalConfigs) {
//				if (Arrays.equals(oneTopConfig, tempAvailableConfig)) {
//					curVmAvailableConfig.clear();
//					curVmAvailableConfig = (ArrayList<int[]>) vmTotalConfigs
//							.clone();
//					return;
//				}
//			}
//
//			tempVmConfig.add(tempAvailableConfig);
//
//		}
//		curVmAvailableConfig.clear();
//		curVmAvailableConfig = (ArrayList<int[]>) tempVmConfig.clone();
//
//	}

//	public void updateVmAvailableConfig() {
//		int[] curVmList = new int[] { 0, 0, 0 };
//		ArrayList<int[]> tempVmConfig = new ArrayList<int[]>();
//		int distance = 0;
//
//		for (Vm vm : getVmList()) {
//			myVm curVm = (myVm) vm;
//			curVmList[curVm.getVmType()]++;
//		}
//		// curVmList =new int[]{2,0,0};
//		for (int index = 0; index < vmTotalConfigs.size(); index++) {
//
//			int[] tempTotalConfig = vmTotalConfigs.get(index).clone();
//			if (tempTotalConfig.equals(curVmList)) {
//				curVmAvailableConfig.clear();
//				curVmAvailableConfig.add(new int[] { 0, 0, 0 });
//				return;
//			}
//			for (int vmId = 0; vmId < tempTotalConfig.length; vmId++) {
//
//				distance = tempTotalConfig[vmId] - curVmList[vmId];
//				if (distance < 0)
//					break;
//				tempTotalConfig[vmId] = distance;
//			}
//			if (distance >= 0)
//				tempVmConfig.add(tempTotalConfig);
//
//		}
//		curVmAvailableConfig.clear();
//		curVmAvailableConfig = (ArrayList<int[]>) tempVmConfig.clone();
//
//	}

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
//		Log.printLine("-----创建vm" + vm.getId() + "成功-------");
		getMc().getVmExcuteThread().setCurCreatedVm(vm);
		getMc().getVmExcuteThread().run();
	}

	private void beginVMsAllocation() {
		boolean success = false;


		switch (scheduleMethod) {
		case 0:
			success = getConfigFromMySchedulingAlgorithm();
			break;
		case 1:
			success = getConfigFromStochasticSchedulingAlgorithm();
			break;
		case 2:
			success = getConfigFroMIUS();
			break;	
		default:
			break;
		}

		if (!success) {
			Log.printLine("在主机" + this.getId() + "上获取可用配置失败！");
			return;
		}
		
		// 如果成功获取配置,则创建选择的vms，并在等待队列中删除选中的vms
		for (myVm vm : curChosenQueue) {
			if ( (success=vmCreate(vm)) ){
				sendMsgToDC(vm);
			}
				

		}
		//更新配置
		if(success)
			updateConfig();

	}

	// 将Vm移出等待队列，Vm进入PM运行
	public void removeVm(myVm vm) {
		int typeId = vm.getVmType();
		if (waitVmsQueue.get(typeId).contains(vm)) {
			waitVmsQueue.get(typeId).remove(vm);
			updateCurQueueLenList(typeId);
		}

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
	public boolean wqEmpty()
	{
		for(int q : getCurQueueLenList())
		{
			if(q!=0) return false;
		}
		return true;
	}


	@Override
	// 将vm从等待队列放入PM
	public boolean vmCreate(Vm vm) {
		
		if(canCreate(vm))
			return super.vmCreate(vm);
		
		return  false;
		
		
	}

	public boolean getConfigFromMySchedulingAlgorithm() {

		int multiple = 2;//阈值，表示队列长度的两倍就到达阈值
		double maxWeight = Integer.MIN_VALUE;
		int[] chosenConfig = new int[GlobalParameter.VM_TYPES];

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
//		if (actualAvailableConfig.contains(new int[] { 0, 0, 0 }))
//			return false;

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
				tempWeightMEM += curAvailableConfig[j] * GlobalParameter.VM_RAM[j];
				tempWeightCPU += curAvailableConfig[j] * GlobalParameter.VM_PES[j];

			}
			// 需要处理阈值，直接跳出
			if (needHandleLimit) {
				maxWeightCPU = 0;
				maxWeightMEM = 0;
				for (int vmType = 0; vmType < chosenConfig.length; vmType++) {
					int choseNum = chosenConfig[vmType];
					for (int num = 0; num < choseNum; num++) {
						maxWeightCPU += GlobalParameter.VM_PES[vmType];
						maxWeightMEM += GlobalParameter.VM_RAM[vmType];
					}
				}
				break;
			}
			// 选择最大的权重配置
			tempWeight = (tempWeightCPU / this.getNumberOfPes() + tempWeightMEM
					/ this.getRam()  ) / 2.0;
			if (tempWeight > maxWeight) {
				maxWeightCPU = tempWeightCPU;
				maxWeightMEM = tempWeightMEM;
				maxWeight = tempWeight;
				chosenConfig = curAvailableConfig.clone();
			}
		}
//		maxWeightMEM = maxWeightMEM * 1000;// GB->MB
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
		timeValuesList.get(getId()).add(CloudSim.clock());
		powerValuesList.get(getId()).add(getPower(cpuShare));
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
		Log.printLine("chosenConfig = " + printArray(chosenConfig));

		return true;
	}

	public boolean getConfigFroMIUS() {

		double maxWeight = Integer.MIN_VALUE;
		int[] chosenConfig = new int[GlobalParameter.VM_TYPES];
		for (int[] curAvailableConfig : curVmAvailableConfig) {
			
			int[] actConfig = new int[curAvailableConfig.length];
			for (int i = 0; i < actConfig.length; i++) {
				actConfig[i] = Math.min(curAvailableConfig[i], curQueueLenList.get(i));
			}

			double tempWeight = 0;
			for (int i = 0; i < curAvailableConfig.length; i++) {
				tempWeight += actConfig[i]* curQueueLenList.get(i)*getVMImpactFactor(i);
			}
			if (tempWeight > maxWeight) {
				maxWeight = tempWeight;
				chosenConfig = actConfig.clone();
			}
		}

		double WeightCPU = 0, WeightMEM = 0;
		for (int i = 0; i < chosenConfig.length; i++) {
			for (int j = 0; j < chosenConfig[i]; j++) {
				WeightMEM += GlobalParameter.VM_RAM[i];
				WeightCPU += GlobalParameter.VM_PES[i];
			}

		}

//		WeightMEM = WeightMEM * 1000;
		// 若chosenConfig不为空，则根据配置进行VM选择
		for (Vm vm : this.getVmList()) {
			WeightCPU += vm.getNumberOfPes();
			WeightMEM += vm.getRam();
		}
		
		Log.printLine("chosenConfig = " + printArray(chosenConfig));
		
		
		double cpuShare = WeightCPU / this.getNumberOfPes();
		double memShare = WeightMEM / this.getRam();// GB->MB
		double sysShare = (cpuShare + memShare) / 2.0;
		
		double now = CloudSim.clock();
		
		Log.printLine(now+"--- MIUS:当前主机" + this.getId() + "的cpu利用率为：" + cpuShare
				+ ",mem利用率为：" + memShare + "综合利用率为：" + sysShare);
		curSynUtilization2 = sysShare;
		if(cpuShare > 1 || memShare > 1 ){
			Log.printLine(now+"--- MIUS:当前主机" + this.getId() + "的cpu利用率为：" + cpuShare
					+ ",mem利用率为：" + memShare + "综合利用率为：" + sysShare);
		}
		
		
		cpuValuesList.get(getId()).add(cpuShare);
		memValuesList.get(getId()).add(memShare);
		timeValuesList.get(getId()).add(now);
		powerValuesList.get(getId()).add(getPower(cpuShare));
		
		
		
		curChosenQueue.clear();
		//更新curChosenQueue与waitVmsQueue
		for (int vmType = 0; vmType < chosenConfig.length; vmType++) {

			for (int numOfvmType = 0; numOfvmType < chosenConfig[vmType]; numOfvmType++) {
				int waitTypeLen = waitVmsQueue.get(vmType).size();
				curChosenQueue.add(waitVmsQueue.get(vmType)
						.get(waitTypeLen - 1));
				removeVm(waitVmsQueue.get(vmType).get(waitTypeLen - 1));

			}
		}

		

		return true;
	}
	/**
	 * 获得vmi类VM对于Host的影响因子
	 * @param vmi
	 * @return
	 */
	public double getVMImpactFactor(int vmi) {

		double imFactor = 0;
		for (int i = 0; i < GlobalParameter.HOST_RESOURCES; i++) {
			if(i==0)
				imFactor+=GlobalParameter.VM_PES[vmi]*1.0/getNumberOfPes();
			else if(i==1)
				imFactor+=GlobalParameter.VM_RAM[vmi]*1.0/getRamProvisioner().getAvailableRam();
		}
		return imFactor;
		
	}

	private boolean getConfigFromStochasticSchedulingAlgorithm() {

		double maxWeight = Integer.MIN_VALUE;
		int[] chosenConfig = new int[GlobalParameter.VM_TYPES];
		for (int[] curAvailableConfig : curVmAvailableConfig) {
			
			int[] actConfig = new int[curAvailableConfig.length];
			for (int i = 0; i < actConfig.length; i++) {
				actConfig[i] = Math.min(curAvailableConfig[i], curQueueLenList.get(i));
			}

			double tempWeight = 0;
			for (int i = 0; i < curAvailableConfig.length; i++) {
				tempWeight += actConfig[i]* curQueueLenList.get(i);
			}
			if (tempWeight > maxWeight) {
				maxWeight = tempWeight;
				chosenConfig = actConfig.clone();
			}
		}

		double WeightCPU = 0, WeightMEM = 0;
		for (int i = 0; i < chosenConfig.length; i++) {
			for (int j = 0; j < chosenConfig[i]; j++) {
				WeightMEM += GlobalParameter.VM_RAM[i];
				WeightCPU += GlobalParameter.VM_PES[i];
			}

		}

//		WeightMEM = WeightMEM * 1000;
		// 若chosenConfig不为空，则根据配置进行VM选择
		for (Vm vm : this.getVmList()) {
			WeightCPU += vm.getNumberOfPes();
			WeightMEM += vm.getRam();
		}
		
		Log.printLine("chosenConfig = " + printArray(chosenConfig));
		
		
		double cpuShare = WeightCPU / this.getNumberOfPes();
		double memShare = WeightMEM / this.getRam();// GB->MB
		double sysShare = (cpuShare + memShare) / 2.0;
		
		double now = CloudSim.clock();
		
		Log.printLine(now+"    Other:当前主机" + this.getId() + "的cpu利用率为：" + cpuShare
				+ ",mem利用率为：" + memShare + "综合利用率为：" + sysShare);
		curSynUtilization2 = sysShare;
		
		
		
		cpuValuesList.get(getId()).add(cpuShare);
		memValuesList.get(getId()).add(memShare);
		timeValuesList.get(getId()).add(now);
		powerValuesList.get(getId()).add(getPower(cpuShare));
		
		
		
		curChosenQueue.clear();
		//更新curChosenQueue与waitVmsQueue
		for (int vmType = 0; vmType < chosenConfig.length; vmType++) {

			for (int numOfvmType = 0; numOfvmType < chosenConfig[vmType]; numOfvmType++) {
				int waitTypeLen = waitVmsQueue.get(vmType).size();
				curChosenQueue.add(waitVmsQueue.get(vmType)
						.get(waitTypeLen - 1));
				removeVm(waitVmsQueue.get(vmType).get(waitTypeLen - 1));

			}
		}

		

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
			updateConfig();

			vm.setHost(null);
			Log.printLine("主机" + getId() + "可用的配置已经更新!AvailableConfigs="
					+ getCurVmAvailableConfig());
		}
	}

	// 更新主机的可用vm配置
//	public void recoverHostConfig(Vm vm) {
//
//		myHost host = (myHost) vm.getHost();
//		if (host != null) {
//			ArrayList<Vm> curVmList = new ArrayList<Vm>();
//			curVmList.add(vm);
//			host.updateConfig();
//			Log.printLine("主机" + host.getId() + "可用的配置已经更新!AvailableConfigs="
//					+ host.getCurVmAvailableConfig());
//		}
//	}
	/**
	 * 选择类型为type的VM最大的配置，其他配置都为0
	 * @param type
	 * @return
	 */
	public int[] maxWeightConfigForVMType(int type) {
		int[] targetConfig = new int[GlobalParameter.VM_TYPES];

		int [] curHostConfig = new int[GlobalParameter.HOST_RESOURCES];
		


		
		for (int i = 0; i < curHostConfig.length; i++) {
			if(i==0)
				curHostConfig[i] = getNumberOfPes() ;
			else if(i==1)
				curHostConfig[i] = getRamProvisioner().getAvailableRam();
			else if(i==2)
				curHostConfig[i] = (int) getStorage();
		}

			
		int max = Integer.MAX_VALUE;
		
		int cpu = GlobalParameter.VM_PES[type];
		int mem = GlobalParameter.VM_RAM[type];
		for(int i=0 ; i<GlobalParameter.HOST_RESOURCES;i++)
		{
			int n = 0 ;
			if(i==0)
				n = curHostConfig[i] / cpu;
			else if(i==1)
				n = curHostConfig[i] / mem;
			if(n<max)
			{
				max = n;
			}
				
		}
		targetConfig[type] = max;

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
	
	public boolean canRun()
	{
		if(wqEmpty())
		{
			return false;
		}
		int [] waitQList = MathUtil.listToArray(curQueueLenList) ;
		//没有等待任务
	
		boolean canRun = false;
	
		for (int i = 0; i < curVmAvailableConfig.size(); i++) {
			int [] oneConfig = curVmAvailableConfig.get(i) ;
			
			if(MathUtil.canHold(oneConfig, waitQList)){
				canRun = true;break;
			}
		}
		return canRun;
		
	}
	public boolean canCreate(Vm vm)
	{
		
//		int usedPes = 0;
//		for (Vm myvm : getVmList()) {
//			usedPes+=myvm.getNumberOfPes();
//		}
		
		if (getStorage() < vm.getSize()) {
			return false;
		}

		if (getRamProvisioner().getAvailableRam() < vm.getCurrentRequestedRam()) {
			return false;
		}

		if (getBwProvisioner().getAvailableBw()<vm.getCurrentRequestedBw()) {
			return false;
		}

//		if ( getVmScheduler().getAvailableMips() < vm.getCurrentRequestedTotalMips()|| 
//						(getNumberOfPes()-usedPes)<vm.getNumberOfPes() ) {
		if(getVmScheduler().getAvailableMips() < vm.getCurrentRequestedTotalMips()){
			return false;
		}
		return true;
	}

	public static void clearRecords() {
		// TODO Auto-generated method stub
		if(cpuValuesList!=null)
		{
			for (List<Double> list : cpuValuesList) {
				list.clear();
			}
		}
		if(memValuesList!=null)
		{
			for (List<Double> list : memValuesList) {
				list.clear();
			}
		}
		if(timeValuesList!=null)
		{
			for (List<Double> list : timeValuesList) {
				list.clear();
			}
		}
	}
	public  void updateConfig(){
		
		curVmAvailableConfig.clear();
		
		int [] curHostConfig = new int[GlobalParameter.HOST_RESOURCES];
		
		int usedPes = 0;
		for (Vm myVm : getVmList()) {
			usedPes += myVm.getNumberOfPes();
		}

		
		for (int i = 0; i < curHostConfig.length; i++) {
			if(i==0)
				curHostConfig[i] = getNumberOfPes() - usedPes;
			else if(i==1)
				curHostConfig[i] = getRamProvisioner().getAvailableRam();
			else if(i==2)
				curHostConfig[i] = (int) getStorage();
		}
		//最后一位记录访问数
		int[] maxN = new int[GlobalParameter.VM_TYPES+1];
		
		for(int j=0;j<GlobalParameter.VM_TYPES;j++)
		{
			
			
			int max = Integer.MAX_VALUE;
			
			int cpu = GlobalParameter.VM_PES[j];
			int mem = GlobalParameter.VM_RAM[j];
			for(int i=0 ; i<GlobalParameter.HOST_RESOURCES;i++)
			{
				int n = 0 ;
				if(i==0)
					n = curHostConfig[i] / cpu;
				else if(i==1)
					n = curHostConfig[i] / mem;
				if(n<max)
				{
					max = n;
				}
					
			}
			maxN[j] = max;
		}

		


		int[] res = new int[maxN.length-1];
	   	int size = 1;
    	for (int k=0 ;k< maxN.length-1;k++) {
			size*=(maxN[k]+1);
		}
    	ArrayList<int []> list = new ArrayList<int[]>();
    	
		dfs(list,res, maxN, 0, size);
		
		if(list.size()==0) {
			
			return;
		}
	
		int []lastConfig = new int[res.length];
		lastConfig[0] = Integer.MAX_VALUE;
		for (int[] config : list) {
			
			if(MathUtil.arrayCompare(config,lastConfig)){
				continue;
			}
			
			int cpu = 0;
			int mem = 0;
			for (int k = 0; k < config.length; k++) {
				cpu+=config[k]*GlobalParameter.VM_PES[k];
				mem+=config[k]*GlobalParameter.VM_RAM[k];
			}
			if( (cpu <= curHostConfig[0]) &&  (mem <= curHostConfig[1])  )
			{
				curVmAvailableConfig.add(config);
				lastConfig[0] = Integer.MAX_VALUE;
			}
			else
				lastConfig = config;
			
		}

		
			
	
		
		
	}
	/**
	 * 深度优先搜索
	 * @param result
	 * @param res
	 * @param a
	 * @param cnt
	 * @param size
	 */
	public  void dfs(ArrayList<int []> result, int[] res,int[] a,int cnt,int size)
	{
		if(a[a.length-1]==size) return;
		if(cnt == a.length-1) {
				
//				MathUtil.printArray(res,res.length);
				
				int count = 0;
				for(int i=0;i<res.length;i++)
				{
					count+=res[i];
				}
				if(count>0){
					result.add(res.clone());
				}
				
				a[a.length-1]++;
				return;
		}
		
		for(int i=0;i<=a[cnt];i++){
			res[cnt] = i;
			dfs(result, res, a, cnt+1, size);
		}
	}

}

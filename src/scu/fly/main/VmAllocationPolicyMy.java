package scu.fly.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudInformationService;

import common.Assert;

public class VmAllocationPolicyMy extends VmAllocationPolicySimple {
	public long usingTime = 0;
	private int curVmNums = -1;
	public int selectedHostIDs[] = { -1, -1, -1 };
	// private static int count=0;
	private List<myVm> curVms = new CopyOnWriteArrayList<>();
	int vmI = 0;

	public int getCurVmNums() {
		return curVmNums;
	}

	public void setCurVmNums(int curVmNums) {
		this.curVmNums = curVmNums;
	}

	public VmAllocationPolicyMy(List<? extends Host> list) {
		super(list);
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {

		myVm myVm = (myVm) vm;
		
		boolean result = true;
		myHost chosenHost;
		/***已分配***/
		if(myVm.getHostID()!=-1){
			chosenHost = (myHost)getHostList().get(myVm.getHostID());
			if (chosenHost.canRun()) {
				chosenHost.run();// 继续执行下一个任务
			}
			return result;
		}
		/***未分配***/
		int vmType = myVm.getVmType();
		
		
		int shortestQueue = Integer.MAX_VALUE;
		int hostIndex = 0;
		for (int index = 0; index < getHostList().size(); index++) {
			myHost myHost = (myHost) getHostList().get(index);
			if ((shortestQueue > myHost.getCurQueueLenList().get(vmType))) {
				shortestQueue = myHost.getCurQueueLenList().get(vmType);
				hostIndex = index;
			}
		}
		chosenHost = (myHost) getHostList().get(hostIndex);

		result = chosenHost.addVm(myVm);// 将vm加入vmList，更新可用存储资源，分配cpu和mem给该vm
//		selectedHostIDs[vmType] = hostIndex; // 将某一vm类型放入的host记录下来
		myVm.setHostID(hostIndex);
		
		Log.printLine("---Host" + chosenHost.getId() + ":VM" + myVm.getId()
				+ ",Type" + myVm.getVmType() + "---");
		
		if (chosenHost.canRun()) {
			chosenHost.run();
		}

		return result;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		myHost host = (myHost) getVmTable().remove(vm.getUid());

		// if (host != null) {
		int idx = host.getId();
		int pes = getUsedPes().remove(vm.getUid());
		host.vmDestroy(vm);
//		if (host.canRun()) {
//			// System.out.println(host.getId() + "主机可以执行新任务了。。。");
//		host.run();// 继续执行下一个任务
//		}
		getFreePes().set(idx, getFreePes().get(idx) + pes);

	}

	public void recoverConfig() {
		for (int i = 0; i < selectedHostIDs.length; i++) {
			selectedHostIDs[i] = -1;
		}
		curVmNums = -1;
		vmI = 0;
	}

}

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
	private int curNeedHandleVms = -1;
	public int selectedHostIDs[] = { -1, -1, -1 };
	private int count = 0;// 记录当前vm处理数
	private List<myVm> curVms = new CopyOnWriteArrayList<>();
	int vmI = 0;

	public int getCurVmNums() {
		return curNeedHandleVms;
	}

	public void setCurVmNums(int curVmNums) {
		recoverConfig();
		this.curNeedHandleVms = curVmNums;

	}

	public VmAllocationPolicyMy(List<? extends Host> list) {
		super(list);
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		curNeedHandleVms--;
		myVm myVm = (myVm) vm;

		boolean result = true;
		myHost chosenHost = null;
		int vmType = myVm.getVmType();
		int hostIndex = -1;

		/*** 已分配 ***/
		if (myVm.getHostID() != -1) {
			Log.printLine("存在已分配现象。。。。。");
			chosenHost = (myHost) getHostList().get(myVm.getHostID());
//			if (chosenHost.canRun()) {
//				chosenHost.run();// 继续执行下一个任务
//			}
			return result;
		}
		/** 类型已选择主机 **/
//		else if (selectedHostIDs[vmType] != -1) {
//			hostIndex = selectedHostIDs[vmType];
//		}

		/*** 未分配 ***/
		else {
			int shortestQueue = Integer.MAX_VALUE;

			for (int index = 0; index < getHostList().size(); index++) {
				myHost myHost = (myHost) getHostList().get(index);
				if ((shortestQueue > myHost.getCurQueueLenList().get(vmType))) {
					shortestQueue = myHost.getCurQueueLenList().get(vmType);
					hostIndex = index;
				}
			}
		}
		chosenHost = (myHost) getHostList().get(hostIndex);

		result = chosenHost.addVm(myVm);// 将vm加入vmList，更新可用存储资源，分配cpu和mem给该vm
		selectedHostIDs[vmType] = hostIndex; // 将某一vm类型放入的host记录下来
		myVm.setHostID(hostIndex);

		Log.printLine("---Host" + hostIndex + ":VM" + myVm.getId()
				+ ",Type" + myVm.getVmType() + "---");
		// 处理当前时间片的最后一个vm
		if (curNeedHandleVms==0) {
			int [] hosts = new int[100];
			int i=0;
			for (Host host : getHostList()) {
				myHost oneHost = (myHost) host;
				if(oneHost.canRun()){
					hosts[i++] = oneHost.getId();
					System.out.println("有任务在身的主机："+oneHost.getId());
					
					if (oneHost.canRun()) {
						oneHost.run();
					}
				}
			}
			System.out.println("有任务在身的主机："+hosts);
		}

		return result;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		myHost host = (myHost) getVmTable().remove(vm.getUid());

		int idx = host.getId();
		int pes = getUsedPes().remove(vm.getUid());
		host.vmDestroy(vm);
		System.out.println(host.getId()+":"+host.getCurQueueLenList().get(0)+"-"+host.getCurQueueLenList().get(1)+"-"+host.getCurQueueLenList().get(2));
		if (host.canRun()) {
			// System.out.println(host.getId() + "主机可以执行新任务了。。。");
			host.run();// 继续执行下一个任务
		}
		getFreePes().set(idx, getFreePes().get(idx) + pes);

	}

	public void recoverConfig() {
		for (int i = 0; i < selectedHostIDs.length; i++) {
			selectedHostIDs[i] = -1;
		}
		count=0;
		vmI = 0;
		curNeedHandleVms = -1;
	}

}

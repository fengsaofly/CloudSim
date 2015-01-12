package scu.fly.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;

public class VmAllocationPolicyMy extends VmAllocationPolicySimple {
	public long usingTime = 0;

	// private static int count=0;
	// public static Timer timer = new Timer();

	public VmAllocationPolicyMy(List<? extends Host> list) {
		super(list);
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {

		int requiredPes = vm.getNumberOfPes();

		List<Integer> freePesTmp = new ArrayList<Integer>();
		for (Integer freePes : getFreePes()) {
			freePesTmp.add(freePes);
		}

		boolean result = true;

		myVm myVm = (myVm) vm;
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
		myHost chosenHost = (myHost) getHostList().get(hostIndex);

		result = chosenHost.addVm(myVm);// 将vm加入vmList，更新可用存储资源，分配cpu和mem给该vm
		chosenHost.run();
		Log.printLine("本次选中的host为：" + chosenHost.getId() + "，对应的VM为:"
				+ myVm.getId() + ",vm类型为：" + myVm.getVmType());

		return result;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		myHost host = (myHost) getVmTable().remove(vm.getUid());

		int idx = getHostList().indexOf(host);
		int pes = getUsedPes().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
			if (host.getCurVmAvailableConfig().size() > 1) {
				System.out.println("主机任务空了");
			}
			getFreePes().set(idx, getFreePes().get(idx) + pes);
			host.run();// 继续执行下一个任务
		}
	}
}

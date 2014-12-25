package scu.fly.main;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;

public class VmAllocationPolicyFly extends VmAllocationPolicySimple {

	public VmAllocationPolicyFly(List<? extends Host> list) {
		super(list);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {

		int requiredPes = vm.getNumberOfPes();

//		List<Integer> freePesTmp = new ArrayList<Integer>();
//		for (Integer freePes : getFreePes()) {
//			freePesTmp.add(freePes);
//		}

		boolean result = false;

		myVm myVm = (myVm) vm;
		FlyHost chosenHost;
		int hostID = -1;
		// vm已经分配给了host
		if (myVm.getHostID() != -1) {
			hostID = myVm.getHostID();
			chosenHost = (FlyHost) getHostList().get(hostID);
			List<myVm> list = chosenHost.getConfigFromFlySchedulingAlgorithm();
			// 如果该VM应该被创建
			if (list != null && list.contains(myVm)) {
				result = chosenHost.vmCreate(myVm);
				// 将VM从等待队列移出
				chosenHost.removeVm(myVm);
			}
		}
		// vm还未分配
		else {

			int vmType = myVm.getVmType();
			int shortestQueue = Integer.MAX_VALUE;
			for (int index = 0; index < getHostList().size(); index++) {
				FlyHost flyHost = (FlyHost) getHostList().get(index);
				if ((shortestQueue > flyHost.getCurQueueLenList().get(vmType))) {
					shortestQueue = flyHost.getCurQueueLenList().get(vmType);
					hostID = index;
				}
			}
			chosenHost = (FlyHost) getHostList().get(hostID);
			myVm.setHostID(hostID);
			// vm属于最大配置
			if (chosenHost.belongMaxConfig(myVm)) {
				result = chosenHost.vmCreate(myVm);
				chosenHost.removeVm(myVm);
			}
			// 添加到等待队列，后面再进行分配
			else {
				chosenHost.addVm(myVm);
			}


		}

		// for (Integer freePes : getFreePes()) {
		// freePesTmp.add(freePes);
		// }

		if (result) { // if vm were succesfully created in the host
			getVmTable().put(vm.getUid(), chosenHost);
			getUsedPes().put(vm.getUid(), requiredPes);
			getFreePes().set(hostID, getFreePes().get(hostID) - requiredPes);
			double usedPes = 0;
			for (Vm curVM : chosenHost.getVmList()) {
				usedPes += curVM.getNumberOfPes();
			}
			//如果不能创建
			if(chosenHost.cannotProvision())
			{
				double cpuUtilization = usedPes / chosenHost.getNumberOfPes() * 1.0;
				double memUtilization = (chosenHost.getRamProvisioner().getAvailableRam() * 1.0)
						/ chosenHost.getRam() * 1.0;
				FlyHost.cpuValuesList.get(chosenHost.getId()).add(cpuUtilization);
				FlyHost.memValuesList.get(chosenHost.getId()).add(memUtilization);
			}

		} 

		return result;
	}
	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = getVmTable().remove(vm.getUid());
		int idx = getHostList().indexOf(host);
		int pes = getUsedPes().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
			getFreePes().set(idx, getFreePes().get(idx) + pes);
			host.run();//继续执行下一个任务
		}
	}

}

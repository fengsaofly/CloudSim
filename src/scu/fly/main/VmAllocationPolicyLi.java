package scu.fly.main;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;

public class VmAllocationPolicyLi extends VmAllocationPolicySimple {

	protected List<myVm> needCreateVMs;
	public static double cpuTotal = 0;
	public static double memTotal = 0;

	public VmAllocationPolicyLi(List<? extends Host> list) {
		super(list);
		// TODO Auto-generated constructor stub
		needCreateVMs = new ArrayList<myVm>();
	}

	@Override
	/**
	 * 随机选择VM的主机
	 */
	public boolean allocateHostForVm(Vm vm) {
		int requiredPes = vm.getNumberOfPes();
		boolean result = false;

		List<Integer> freePesTmp = new ArrayList<Integer>();
		// List<myHost> IRHostList = new ArrayList<myHost>();
		ArrayList<List<Integer>> array = new ArrayList<List<Integer>>();// 分为10个部分的数组，每部分装若干的主机
		List<Integer> list = new ArrayList<Integer>();
		//初始化array
		for (int i = 0; i < 10; i++) {
			array.add(list);
		}
		
		for (Integer freePes : getFreePes()) {
			freePesTmp.add(freePes);
		}
		//如果没有创建vm
		if (!getVmTable().containsKey(vm.getUid())) {
			
			double totalCPUUsed = 0, totalMemUsed = 0;
			double totalCPU = 0, totalMem = 0;
			for (Host myHost : getHostList()) {
				myHost oneHost = (myHost) myHost;

				double usedPes = 0, usedRam = 0;

				for (Vm curVM : oneHost.getVmList()) {
					usedPes += curVM.getNumberOfPes();
				}

				usedRam = oneHost.getRamProvisioner().getAvailableRam() * 1.0;
				double cpuUtilization = usedPes / oneHost.getNumberOfPes()
						* 1.0;
				double memUtilization = usedRam / oneHost.getRam() * 1.0;

				double IR = (cpuUtilization + memUtilization) / 2.0;

				oneHost.IR = IR;
				oneHost.cpuU = cpuUtilization;
				oneHost.memU = memUtilization;
				totalCPUUsed += cpuUtilization * oneHost.getNumberOfPes();
				totalMemUsed += memUtilization * oneHost.getRam();
				totalCPU += oneHost.getNumberOfPes();
				totalMem += oneHost.getRam();
				//根据利用率，将各个主机分别放入到10个段
				for (int i = 0; i < 10; i++) {
					if (IR <= (i + 1) * 0.1) {
						array.get(i).add(oneHost.getId());
					}
				}


			}
			cpuTotal = totalCPU;
			memTotal = totalMem;
			double totalCPUPercent = totalCPUUsed / cpuTotal;
			double totalMEMPercent = totalMemUsed / memTotal;

			// if this vm was not created

			// 随机选择一个host
			int idx = -1;
			myHost host = null;

			double maxILB = Integer.MAX_VALUE;
			//计算各个PM的不平衡度
			for (Host myHost : getHostList()) {
				myHost oneHost = (myHost) myHost;
				oneHost.ILB = ((oneHost.IR - totalCPUPercent)
						* (oneHost.IR - totalCPUPercent) + (oneHost.IR - totalMEMPercent)
						* (oneHost.IR - totalMEMPercent)) / 2.0;
			}
			//选择利用率低，不平衡度低的PM
			for (int i = 0; i < array.size(); i++) {
				List<Integer> myHosts = array.get(i);
				// 在对应的利用率阶段找出ILB最小的PM
				for (Integer id : myHosts) {
					myHost myHost = (myHost) getHostList().get(id);
					if (myHost.ILB < maxILB && myHost.canCreate(vm)) {
						maxILB = myHost.ILB;
						idx = id;
					}
				}
				// 找到合适的PM
				if (idx != -1)
					break;

			}

			// 找到合适的主机
			if (idx != -1)
				host = (myHost) getHostList().get(idx);

			if (host != null)
				result = host.vmCreate(vm);

			if (result) { // if vm were succesfully created in the host
				getVmTable().put(vm.getUid(), host);
				getUsedPes().put(vm.getUid(), requiredPes);
				getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
				double usedPes = 0,usedRam=0;
				usedRam = host.getRamProvisioner().getAvailableRam() * 1.0;
				for (Vm curVM : host.getVmList()) {
					usedPes += curVM.getNumberOfPes();
					
				}
				double cpuUtilization = usedPes / host.getNumberOfPes()* 1.0;
				double memUtilization = usedRam / host.getRam() * 1.0;
				
				myHost.cpuValuesList.get(idx).add(cpuUtilization);
				myHost.memValuesList.get(idx).add(memUtilization);

				myHost.sendMsgToDC((myVm) vm);

				result = true;
	
			} else {
				freePesTmp.set(idx, Integer.MIN_VALUE);
			}

			// 没有创建成功
			if (!result) {
				needCreateVMs.add((myVm) vm);
			}

		}

		return result;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {

		myHost host = (myHost) getVmTable().remove(vm.getUid());
		// int id = getHostList().indexOf(host);
		// int pes = getUsedPes().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
			// getFreePes().set(id, getFreePes().get(id) + pes);
		}

		ArrayList<myVm> copyNeedVMs = new ArrayList<>(needCreateVMs);
		ArrayList<myVm> createdVMs = new ArrayList<myVm>();

		while (copyNeedVMs.size() > 0) {
			int idx = GlobalParameter.random(0, copyNeedVMs.size() - 1);
			myVm myVm = copyNeedVMs.get(idx);
			if (allocateHostForVm(myVm, host)) {
				myHost.sendMsgToDC(myVm);
				createdVMs.add(myVm);

			}
			copyNeedVMs.remove(myVm);
		}
		for (myVm myVm : createdVMs) {
			if (needCreateVMs.contains(myVm))
				needCreateVMs.remove(myVm);
		}

	}

}

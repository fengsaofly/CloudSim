package scu.fly.main;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;

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
	public boolean allocateHostForVm(Vm vm1) {
		myVm vm =	(myVm) vm1;
		int requiredPes = vm.getNumberOfPes();
		boolean result = false;

		List<Integer> freePesTmp = new ArrayList<Integer>();
		// List<myHost> IRHostList = new ArrayList<myHost>();
		ArrayList<List<Integer>> array = new ArrayList<List<Integer>>();// 分为10个部分的数组，每部分装若干的主机

		//初始化array
		for (int i = 0; i < 10; i++) {
			array.add(new ArrayList<Integer>());
		}
		
		for (Integer freePes : getFreePes()) {
			freePesTmp.add(freePes);
		}
		//如果没有创建vm
		if (!getVmTable().containsKey(vm.getUid())) {
			
			double totalCPUUsed = 0, totalMemUsed = 0;
			double totalCPU = 0, totalMem = 0;

			/**根据利用率，将各个主机按照利用率归类，一共10类，[0-0,1][0,1-0.2]...[0.9-1]***/
			for (Host myHost : getHostList()) {
				myHost oneHost = (myHost) myHost;

				double usedPes = 0, usedRam = 0;

				for (Vm curVM : oneHost.getVmList()) {
					usedPes += curVM.getNumberOfPes();
				}

				usedRam =(oneHost.getRam() - oneHost.getRamProvisioner().getAvailableRam()) * 1.0;
				double cpuUtilization = usedPes / oneHost.getNumberOfPes()* 1.0;
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

						break;
					}
		
				}
	

			}
			/***计算各个主机的不平衡度***/
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
			/****选择利用率低，不平衡度低的PM*****/
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

			// 找到主机
			if (idx != -1){
				host = (myHost) getHostList().get(idx);
				result = host.vmCreate(vm);
			}
				
			if (result) { // if vm were succesfully created in the host
				getVmTable().put(vm.getUid(), host);
				getUsedPes().put(vm.getUid(), requiredPes);
				getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
				double usedPes = 0;
				for (Vm curVM : host.getVmList()) {
					usedPes += curVM.getNumberOfPes();
				}
				double cpuUtilization = usedPes / host.getNumberOfPes()* 1.0;
				double memUtilization =(host.getRam()- (host.getRamProvisioner().getAvailableRam() * 1.0)) / host.getRam() * 1.0;

				myHost.cpuValuesList.get(host.getId()).add(cpuUtilization);
				myHost.memValuesList.get(host.getId()).add(memUtilization);
				myHost.timeValuesList.get(host.getId()).add(CloudSim.clock());
				myHost.powerValuesList.get(host.getId()).add(host.getPower(cpuUtilization));
				
				myHost.sendMsgToDC(vm);

				result = true;
	
			}
		}

		return result;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {

		myHost host = (myHost) getVmTable().remove(vm.getUid());
		int id = getHostList().indexOf(host);
		int pes = getUsedPes().remove(vm.getUid());
		host.vmDestroy(vm);
		getFreePes().set(id, getFreePes().get(id) + pes);


	}

}

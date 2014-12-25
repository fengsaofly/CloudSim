package scu.fly.main;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;

public class VmAllocationPolicyRoundRobin extends VmAllocationPolicySimple {
	
	protected List<myVm> needCreateVMs; 
	
	public VmAllocationPolicyRoundRobin(List<? extends Host> list) {
		super(list);
		// TODO Auto-generated constructor stub
		needCreateVMs = new ArrayList<myVm>();
	}
	@Override
	public boolean allocateHostForVm(Vm vm) {
		int requiredPes = vm.getNumberOfPes();
		boolean result = false;
		int tries = 0;
		List<Integer> freePesTmp = new ArrayList<Integer>();
		for (Integer freePes : getFreePes()) {
			freePesTmp.add(freePes);
		}

		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
			do {// we still trying until we find a host or until we try all of them
				
				//随机选择一个host
				myHost host = null;	
				
				host = (myHost)getHostList().get(tries);
				if(host != null)
					result = host.vmCreate(vm);
				
				
				

				if (result) { // if vm were succesfully created in the host
					getVmTable().put(vm.getUid(), host);
					getUsedPes().put(vm.getUid(), requiredPes);
					getFreePes().set(host.getId(), getFreePes().get(host.getId()) - requiredPes);
					//记录cpu与mem使用率
					
					double usedPes = 0;
					for (Vm curVM : host.getVmList()) {
						usedPes+=curVM.getNumberOfPes();
					}
					double cpuUtilization = usedPes/host.getNumberOfPes()*1.0;
					double memUtilization = (host.getRamProvisioner().getAvailableRam() *1.0) / host.getRam() *1.0;
					myHost.cpuValuesList.get(host.getId()).add(cpuUtilization);
					myHost.memValuesList.get(host.getId()).add(memUtilization);
					
					myHost.sendMsgToDC((myVm)vm);
					
					result = true;
					break;
				} else {
					freePesTmp.set(host.getId(), Integer.MIN_VALUE);
				}
				tries++;
			} while (!result && tries < getFreePes().size());
			//没有创建成功
			if(!result)
			{
				needCreateVMs.add((myVm)vm);
			}

		}

		return result;
	}
	@Override
	public void deallocateHostForVm(Vm vm) {
	
		myHost host = (myHost)getVmTable().remove(vm.getUid());
//		int id = getHostList().indexOf(host);
//		int pes = getUsedPes().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
//			getFreePes().set(id, getFreePes().get(id) + pes);
		}
		
		ArrayList<myVm> createdVMs =new ArrayList<myVm>();
		
		for (myVm myVm : needCreateVMs) {
			if(allocateHostForVm(myVm,host)){
				myHost.sendMsgToDC(myVm);
				createdVMs.add(myVm);

			}
		}
		for (myVm myVm : createdVMs) {
			if(needCreateVMs.contains(myVm))
				needCreateVMs.remove(myVm);
		}

	}

}

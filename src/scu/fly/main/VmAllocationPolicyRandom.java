package scu.fly.main;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;

public class VmAllocationPolicyRandom extends VmAllocationPolicySimple {
	
	protected List<myVm> needCreateVMs; 

	public VmAllocationPolicyRandom(List<? extends Host> list) {
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
		int tries = 0;
		List<Integer> freePesTmp = new ArrayList<Integer>();
		for (Integer freePes : getFreePes()) {
			freePesTmp.add(freePes);
		}

		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
			do {// we still trying until we find a host or until we try all of them
				
				//随机选择一个host
				int idx = GlobalParameter.random(0, freePesTmp.size()-1);
				myHost host = null;	
				
				host = (myHost)getHostList().get(idx);
				if(host != null)
					result = host.vmCreate(vm);
			
				

				if (result) { // if vm were succesfully created in the host
					getVmTable().put(vm.getUid(), host);
					getUsedPes().put(vm.getUid(), requiredPes);
					getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
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
					freePesTmp.set(idx, Integer.MIN_VALUE);
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
		
		ArrayList<myVm> copyNeedVMs = new ArrayList<>(needCreateVMs);
		ArrayList<myVm> createdVMs =new ArrayList<myVm>();
		
		while(copyNeedVMs.size()>0)
		{
			int idx = GlobalParameter.random(0, copyNeedVMs.size()-1);
			myVm myVm = copyNeedVMs.get(idx);
			if(allocateHostForVm(myVm,host)){
				myHost.sendMsgToDC(myVm);
				createdVMs.add(myVm);
				
			}
			copyNeedVMs.remove(myVm);
		}
		for (myVm myVm : createdVMs) {
			if(needCreateVMs.contains(myVm))
				needCreateVMs.remove(myVm);
		}
//			if(allocateHostForVm(myVm,host))
				
//		}
	}

}

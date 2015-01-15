package scu.fly.main;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;

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

					double usedPes = 0;
					for (Vm curVM : host.getVmList()) {
						usedPes+=curVM.getNumberOfPes();
					}
					int idx = host.getId();
					double cpuUtilization = usedPes/host.getNumberOfPes()*1.0;
					double memUtilization = (host.getRam()-host.getRamProvisioner().getAvailableRam() *1.0) / host.getRam() *1.0;
					
					myHost.cpuValuesList.get(host.getId()).add(cpuUtilization);
					myHost.memValuesList.get(host.getId()).add(memUtilization);
					myHost.timeValuesList.get(host.getId()).add(CloudSim.clock());
					
					myHost.sendMsgToDC((myVm)vm);
					
					break;
				} else {
					freePesTmp.set(host.getId(), Integer.MIN_VALUE);
				}
				tries++;
			} while (!result && tries < getFreePes().size());

		}

		return result;
	}
	@Override
	public void deallocateHostForVm(Vm vm) {
	
		myHost host = (myHost)getVmTable().remove(vm.getUid());
		int id = getHostList().indexOf(host);
		int pes = getUsedPes().remove(vm.getUid());
		
		host.vmDestroy(vm);
		getFreePes().set(id, getFreePes().get(id) + pes);

		

	}

}

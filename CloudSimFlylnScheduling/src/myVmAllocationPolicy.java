import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;




public class myVmAllocationPolicy extends VmAllocationPolicySimple{
	 public myVmAllocationPolicy(List<? extends Host> list) {
		 super(list);
	 }
	 
	 @Override
	 public boolean allocateHostForVm(Vm vm) {
		 
		 	
			int requiredPes = vm.getNumberOfPes();
			
			List<Integer> freePesTmp = new ArrayList<Integer>();
			for (Integer freePes : getFreePes()) {
				freePesTmp.add(freePes);
			}


		 		boolean result = false;
		 		
		 		myVm myVm = (myVm)vm;
		 		int vmType = myVm.getVmType();
		 		int shortestQueue = Integer.MAX_VALUE;
		 		int hostIndex= 0;
				for(int index=0;index<getHostList().size();index++)
				{
					myHost myHost = (myHost)getHostList().get(index);
					if( (shortestQueue > myHost.getCurQueueLenList().get(vmType)) && (freePesTmp.get(index) > requiredPes) )
					{
						shortestQueue = myHost.getCurQueueLenList().get(vmType);
						hostIndex = index;
					}
				}
				myHost chosenHost = (myHost)getHostList().get(hostIndex);
				
					result = chosenHost.addVm(myVm);//将vm加入vmList，更新可用存储资源，分配cpu和mem给该vm

					if (result) { // if vm were succesfully created in the host
						getVmTable().put(vm.getUid(), chosenHost);
						getUsedPes().put(vm.getUid(), requiredPes);
						getFreePes().set(hostIndex, getFreePes().get(hostIndex) - requiredPes);
						result = true;
					
					} else {
						freePesTmp.set(hostIndex, Integer.MIN_VALUE);
					}
				
		

			return result;
		}

 
}

package scu.fly.main;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;


public class FlyPolicy extends PowerVmAllocationPolicyAbstract{

	public FlyPolicy(List<? extends Host> list) {
		super(list);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public PowerHost findHostForVm(Vm vm) {
		if (getHostList().size() > 0) {
			return (PowerHost) getHostList().get(0);
		}
		return null;
		
	}
	
}

package scu.fly.main;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

public class SimpleHost extends Host {
	
	public static ArrayList<ArrayList> cpuValuesList;
	public static ArrayList<ArrayList> memValuesList;

	public SimpleHost(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		// TODO Auto-generated constructor stub
		
		if(cpuValuesList==null || cpuValuesList.size()==0){
			cpuValuesList= new ArrayList<ArrayList>();
			for(int i=0;i<100;i++){
				cpuValuesList.add(new ArrayList());
			}
		}
		if(memValuesList==null || memValuesList.size()==0){
			memValuesList= new ArrayList<ArrayList>();
			for(int i=0;i<100;i++){
				memValuesList.add(new ArrayList());
			}
		}
	}
	
}

package scu.fly.main;
import java.util.ArrayList;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;


public class myDatacenterBroker extends DatacenterBroker {
	
	public myDatacenterBroker(String name) throws Exception {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindCloudletToVm(int cloudletId, int vmId) {
		woCloudlet cloudlet = (woCloudlet)CloudletList.getById(getCloudletList(), cloudletId);
		cloudlet.setVmId(vmId);
		myVm myVm = (myVm)getVmList().get(vmId);
		cloudlet.setVmType(myVm.getVmType());
	}
	@Override
	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) {
		
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];
		//如果用户需要反馈信息，则打印反馈信息
		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);//将创建的vmID与datacenterId绑定
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));//将创建的VM加入VmsCreatedList
			Log.printLine(getVmsCreatedList());
			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
					+ " has been created in Datacenter #" + datacenterId + ", Host #"
					+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
		} else {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}
		//ack++
		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
			else if(getVmsAcks()>0){
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}
	@Override
	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId()
				+ " received");
		cloudletsSubmitted--;
	
	
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
			Log.printLine(CloudSim.clock() + ": " + getName() + ": 所有任务已经执行完成...");
			clearDatacenters();
			finishExecution();
			setState(FINISHED);
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();//已执行的任务列表清空
				//createVmsInDatacenter(getDatacenterIdsList().get(0));
				//createVmsInDatacenter(0);
			}

		}
	}
	@Override
	/**
	 * Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
			int datacenterId =getVmsToDatacentersMap().get(vm.getId() );
			sendNow(datacenterId, CloudSimTags.VM_DESTROY, vm);

		}

		getVmsCreatedList().clear();
	}
	public int getVmIndexInCreatedVms(int cloudletId){
		for(int index=0;index<getVmsCreatedList().size();index++){
			if(cloudletId==getVmsCreatedList().get(index).getId()){
				return index;
			}
		}
		return -1;
	}
	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
		setState(FINISHED);
	}

}

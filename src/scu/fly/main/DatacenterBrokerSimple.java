package scu.fly.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Timer;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;

import DataCenterBrokerModified.DatacenterBrokerModifiedRealTime;
import DataCenterBrokerModified.VmComparator;

public class DatacenterBrokerSimple extends DatacenterBroker {

	// ==============================================================REAL
	// TIME========================================
	ConcurrentLinkedQueue<woCloudlet> cloudletsBuffer = new ConcurrentLinkedQueue<>();// 保存并发任务队列

	ConcurrentLinkedQueue<myVm> vmsRequestBuffer = new ConcurrentLinkedQueue<>();// 保存并发任务队列
	PriorityQueue<VmComparator> checkList = new PriorityQueue<>();// 保存vm队列，vm根据cpu的大小进行排序
	int vmInd = 0, startTime = 0;
	volatile int presentTime = 0;// 当前时间
	boolean sort = false;
	final int TimeUnit = 10; // means 100 milliSecs = 1 time unit，时间单位为1s
	Timer updatePresentTime;
	protected int receivedCloudlets = 0;
	protected volatile Integer cloudletCount = 0;

	protected volatile boolean hasNewRequests =false;//判断是否有新任务

	public DatacenterBrokerSimple(String name) throws Exception {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindCloudletToVm(int cloudletId, int vmId) {
		// woCloudlet cloudlet = (woCloudlet) CloudletList.getById(
		// getCloudletList(), cloudletId);
		// cloudlet.setVmId(vmId);
		// myVm myVm = (myVm) getVmList().get(vmId);
		// cloudlet.setVmType(myVm.getVmType());
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
		// 如果用户需要反馈信息，则打印反馈信息
		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);// 将创建的vmID与datacenterId绑定
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));// 将创建的VM加入VmsCreatedList
			// Log.printLine(getVmsCreatedList());
			Log.printLine(CloudSim.clock()
					+ ": "
					+ getName()
					+ ": VM #"
					+ vmId
					+ " has been created in Datacenter #"
					+ datacenterId
					+ ", Host #"
					+ VmList.getById(getVmsCreatedList(), vmId).getHost()
							.getId());
		} else {
			Log.printLine(CloudSim.clock() + ": " + getName()
					+ ": Creation of VM #" + vmId + " failed in Datacenter #"
					+ datacenterId);
		}
		// ack++
		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size()
				- getVmsDestroyed()) {
			submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock()
							+ ": "
							+ getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			} else if (getVmsAcks() > 0) {
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock()
							+ ": "
							+ getName()
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
		woCloudlet cloudlet = (woCloudlet) ev.getData();
		Vm vm = cloudlet.getVm();
		getCloudletReceivedList().add(cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet "
				+ cloudlet.getCloudletId() + " received");
		receivedCloudlets++;
		

		
		// 提交的总任务数等于已经完成的任务数
		if (receivedCloudlets == cloudletCount) {

//			// 等待11s,查看是否有新任务
			try {
				Thread.sleep(GlobalParameter.TimeUnit * 11);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//发现有新任务
			if (receivedCloudlets < cloudletCount && receivedCloudlets > 0) {
//				clearDatacenters();// 已执行的任务列表清空
				clearVM(vm);
				createVmsInDatacenter(getDatacenterIdsList().get(0));
			} else {//没有任务了
				Log.printLine(CloudSim.clock() + ": " + getName()
						+ ": 所有任务已经执行完成...");
				clearDatacenters();
				setState(FINISHED);
				finishExecution();
				hasNewRequests = false;
			}
		}
		// 如果已经提交的任务小于总任务数，表明一部分任务已经完成
		else if (receivedCloudlets < cloudletCount && receivedCloudlets > 0) {
			clearVM(vm);// 已执行的任务列表清空
//			clearDatacenters();
			createVmsInDatacenter(getDatacenterIdsList().get(0));

		}
		
		myHost host =(myHost)vm.getHost();
		//主机无事可做
		if(host.getVmList().size()==1 && host.wqEmpty()	&& !hasNewRequests)
		{
			myHost.timeValuesList.get(host.getId()).add(CloudSim.clock());
		}

	}
	/**
	 * 提交新任务
	 */
	@Override
	public void submitVmList(List<? extends Vm> list) {
		getVmList().addAll(list);
		cloudletCount += list.size();
		hasNewRequests  = true;
		
	}

	@Override
	public void submitCloudletList(java.util.List<? extends Cloudlet> list) {
		super.submitCloudletList(list);
		//保存提交时间
//		for (Cloudlet cloudlet : list) {
//			cloudlet.setSubmissionTime(CloudSim.clock()); 
//		}
		
	}
	@Override
	public <T extends Vm> CopyOnWriteArrayList<T> getVmList() {
		return (CopyOnWriteArrayList<T>) vmList;
	}

	@Override
	public <T extends Cloudlet> CopyOnWriteArrayList<T> getCloudletList() {
		return (CopyOnWriteArrayList<T>) cloudletList;
	}

	@Override
	protected void submitCloudlets() {

		generateUnitCloudlets();// 收到提交任务的命令，随机产生时间片任务进行提交

		int vmIndex = 0;
		for (Cloudlet cloudlet : cloudletsBuffer) {
			cloudlet = (woCloudlet) cloudlet;
		
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed
			// yet
			if (cloudlet.getVmId() == -1) {
				vm = getVmsCreatedList().get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId()
							+ ": bount VM not available");
					continue;
				}
			}

			Log.printLine(CloudSim.clock() + ": " + getName()
					+ ": Sending cloudlet " + cloudlet.getCloudletId()
					+ " to VM #" + vm.getId());
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()),
					CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
		}
		// queueBuffer.clear();
		// remove submitted cloudlets from waiting list
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			cloudletsBuffer.remove(cloudlet);
		}
		// getCloudletSubmittedList().clear();
	}

	protected void clearVM(Vm vm) {

		Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #"
				+ vm.getId());
		int datacenterId = getVmsToDatacentersMap().get(vm.getId());
		sendNow(datacenterId, CloudSimTags.VM_DESTROY, vm);

		getVmsCreatedList().remove(vm);
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
			Log.printLine(CloudSim.clock() + ": " + getName()
					+ ": Destroying VM #" + vm.getId());
			int datacenterId = getVmsToDatacentersMap().get(vm.getId());
			sendNow(datacenterId, CloudSimTags.VM_DESTROY, vm);

		}

		getVmsCreatedList().clear();
	}

	public int getVmIndexInCreatedVms(int cloudletId) {
		for (int index = 0; index < getVmsCreatedList().size(); index++) {
			if (cloudletId == getVmsCreatedList().get(index).getId()) {
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
	/**
	 * 创建任何没创建的VM，包括已经尝试过的VM。
	 * （创建任务的第一步）
	 */
	@Override
	protected void createVmsInDatacenter(int datacenterId) {

		// send as much vms as possible for this datacenter before trying the
		// next one
		int requestedVms = 0;
		String datacenterName = CloudSim.getEntityName(datacenterId);
		for (Vm vm : getVmList()) {
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printLine(CloudSim.clock() + ": " + getName()
						+ ": Trying to Create VM #" + vm.getId() + " in "
						+ datacenterName);
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
		}

		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	public void generateUnitCloudlets() {
		
		

		int num = getCloudletList().size();

		// Log.printLine("#####" + (++UnitCount) + " 时间片随机产生" + num +
		// "个任务#######");
		for (int i = 0; i < num; i++) {
			woCloudlet cloudlet = (woCloudlet) getCloudletList().remove(0);
			cloudletsBuffer.add(cloudlet);
			// myVm vm = (myVm)getVmList().remove(0);

		}

	}

}

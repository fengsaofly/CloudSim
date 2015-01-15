package scu.fly.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.plaf.SliderUI;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

public class myDatacenter extends Datacenter implements MyCallInterface {
	public static int finishFlag = 0;
	private List<SimEvent> list = new ArrayList<>();
	// private vmCreateThread vmCreateThread;
	private vmExcuteThread vmExcuteThread;
	// private int writersWaiting = 0;
	// private boolean writing = false;
	// private int sychonizedSignal=1;
	private List<myVm> onceSubmittedVmList = new ArrayList<myVm>();
	private List<myVm> allSubmittedVmList = new ArrayList<myVm>();
	private List<myVm> createdVmList = new ArrayList<myVm>();
	private List<myVm> addedVmList = new ArrayList<myVm>();
	public static boolean cloudletHasBeenSubmitted = false;

	public myDatacenter(String name, DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval);
		// vmCreateThread = new vmCreateThread("vmCreateThread");
		// vmCreateThread.start();
		vmExcuteThread = new vmExcuteThread("vmExcuteThread");
		vmExcuteThread.start();
	

	}

	// public void doingVmCreating(){
	// SimEvent ev=null;
	// // Log.printLine("vmCreateThread is running !");
	// if(list.size()>0) {
	// ev = list.get(0);
	// processVmCreate(ev,true);
	//
	// }
	//
	//
	// }
	@Override
	/**
	 * 关闭数据中心，主要是关闭所有的主机
	 */
	public void shutdownEntity() {
		Log.printLine("-------关闭数据中心-------");
		for (Host host : getHostList()) {
			if(myHost.timeValuesList.get(host.getId()).size() == myHost.cpuValuesList.get(host.getId()).size() && myHost.cpuValuesList.get(host.getId()).size()>0)
				myHost.timeValuesList.get(host.getId()).add(CloudSim.clock());
		}
		vmExcuteThread.stopThread();
		vmExcuteThread = null;

	}

	// public void doingVmExcuting(myVm vm){
	//
	// curCreatedVm = vm;
	// // Log.printLine("vmCreateThread is running !");
	// vmExcuteThread.run();
	// }
	@Override
	protected void processVmCreate(SimEvent ev, boolean ack) {
		
//		VmAllocationPolicyMy policyMy = (VmAllocationPolicyMy)getVmAllocationPolicy();
//		if(policyMy.getCurVmNums() == -1){
//			policyMy.setCurVmNums(getVmList().size());
//		}
		onceSubmittedVmList.add((myVm) ev.getData());
		Vm vm = (Vm) ev.getData();
		// finishFlag++;
		addedVmList.add((myVm) vm);
//		getCharacteristics().
		boolean result = getVmAllocationPolicy().allocateHostForVm(vm);

	
		// send(vm.getUserId(), 0,CloudSimTags.VM_WaitCreate, null);
		// while(createdVmList.isEmpty()){
		// try {
		// Thread.sleep(WAITING);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
	}

	// 从host那里得知，vm创建成功
	public synchronized void VmCreatedSuccess(myVm vm) {

		createdVmList.add(vm);
		boolean ack = true;
		boolean result = true;
		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			send(vm.getUserId(), 0.1, CloudSimTags.VM_CREATE_ACK, data);
		}

		// 创建VM成功，计算价格
		double amount = 0.0;
		if (getDebts().containsKey(vm.getUserId())) {
			amount = getDebts().get(vm.getUserId());
		}
		amount += getCharacteristics().getCostPerMem() * vm.getRam();
		amount += getCharacteristics().getCostPerStorage() * vm.getSize();

		getDebts().put(vm.getUserId(), amount);

		getVmList().add(vm);

		if (vm.isBeingInstantiated()) {
			vm.setBeingInstantiated(false);
		}
		// Log.printLine("host"+getVmAllocationPolicy()
		// .getHost(vm)+"vm"+vm.getId()+"的主机为："+vm.getHost());

		VmAllocationPolicySimple myPolicy = (VmAllocationPolicySimple) getVmAllocationPolicy();
		myHost host = (myHost)vm.getHost();
		int usedPes = 0;
		for (Vm myVm : host.getVmList()) {
			usedPes+=myVm.getNumberOfPes();
		}
		myPolicy.getVmTable().put(vm.getUid(), host);
		myPolicy.getUsedPes().put(vm.getUid(), vm.getNumberOfPes());
		myPolicy.getFreePes().set(host.getId(),	host.getNumberOfPes()-usedPes);
		Log.printLine("创建vm" + vm.getId() + "成功，主机" + host.getId()
				+ "的可用PE数为：" + myPolicy.getFreePes().get(host.getId()));
		vm.updateVmProcessing(CloudSim.clock(), host.getVmScheduler().getAllocatedMipsForVm(vm));

	}

	public vmExcuteThread getVmExcuteThread() {
		return vmExcuteThread;
	}

	public boolean vmListFnished() {
		if (onceSubmittedVmList.size() > 0
				&& onceSubmittedVmList.size() == createdVmList.size()
				&& createdVmList.size() == addedVmList.size())
			return true;
		return false;
	}

	@Override
	/**
	 * Processes a Cloudlet submission.
	 * 
	 * @param ev a SimEvent object
	 * @param ack an acknowledgement
	 * @pre ev != null
	 * @post $none
	 */
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		updateCloudletProcessing();

		try {
			// gets the Cloudlet object
			woCloudlet cl = (woCloudlet) ev.getData();
			int vmID = cl.getVmId();

			// checks whether this Cloudlet has finished or not
			if (cl.isFinished()) {
				String name = CloudSim.getEntityName(cl.getUserId());
				Log.printLine(getName() + ": Warning - Cloudlet #"
						+ cl.getCloudletId() + " owned by " + name
						+ " is already completed/finished.");
				Log.printLine("Therefore, it is not being executed again");
				Log.printLine();

				// NOTE: If a Cloudlet has finished, then it won't be processed.
				// So, if ack is required, this method sends back a result.
				// If ack is not required, this method don't send back a result.
				// Hence, this might cause CloudSim to be hanged since waiting
				// for this Cloudlet back.
				if (ack) {
					int[] data = new int[3];
					data[0] = getId();
					data[1] = cl.getCloudletId();
					data[2] = CloudSimTags.FALSE;

					// unique tag = operation tag
					int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
					sendNow(cl.getUserId(), tag, data);
				}

				sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);

				return;
			}

			// process this Cloudlet to this CloudResource
			cl.setResourceParameter(getId(), getCharacteristics()
					.getCostPerSecond(), getCharacteristics().getCostPerBw());

			int userId = cl.getUserId();
			int vmId = cl.getVmId();

			// time to transfer the files
			double fileTransferTime = predictFileTransferTime(cl
					.getRequiredFiles());

			Host host = getVmAllocationPolicy().getHost(vmId, userId);
			Vm vm = host.getVm(vmId, userId);
			CloudletScheduler scheduler = vm.getCloudletScheduler();
			double estimatedFinishTime = scheduler.cloudletSubmit(cl,
					fileTransferTime);

			// 设置cloudlet的Host
			if (cl.getHostID() == -1)
				cl.setHostID(host.getId());

			// if this cloudlet is in the exec queue
			if (estimatedFinishTime > 0.0
					&& !Double.isInfinite(estimatedFinishTime)) {
				estimatedFinishTime += fileTransferTime;
				send(getId(), estimatedFinishTime,
						CloudSimTags.VM_DATACENTER_EVENT);
			}

			if (ack) {
				int[] data = new int[3];
				data[0] = getId();
				data[1] = cl.getCloudletId();
				data[2] = CloudSimTags.TRUE;

				// unique tag = operation tag
				int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
				sendNow(cl.getUserId(), tag, data);
			}
		} catch (ClassCastException c) {
			Log.printLine(getName() + ".processCloudletSubmit(): "
					+ "ClassCastException error.");
			c.printStackTrace();
		} catch (Exception e) {
			Log.printLine(getName() + ".processCloudletSubmit(): "
					+ "Exception error.");
			e.printStackTrace();
		}

		checkCloudletCompletion();
	}

	@Override
	/**
	 * Process the event for an User/Broker who wants to destroy a VM previously created in this
	 * PowerDatacenter. This PowerDatacenter may send, upon request, the status back to the
	 * User/Broker.
	 * 
	 * @param ev a Sim_event object
	 * @param ack the ack
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmDestroy(SimEvent ev, boolean ack) {
		Vm vm = (Vm) ev.getData();

		getVmAllocationPolicy().deallocateHostForVm(vm);// 更新主机的可用配置

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();
			data[2] = CloudSimTags.TRUE;

			sendNow(vm.getUserId(), CloudSimTags.VM_DESTROY_ACK, data);
		}

		getVmList().remove(vm);


	}

	@Override
	protected void checkCloudletCompletion() {
		
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
		
			for (Vm vm : host.getVmList()) {
				while (vm.getCloudletScheduler().isFinishedCloudlets()) {
					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
					if (cl != null) {
						sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
					}
				}
			}
		}
//		List<? extends Host> list = getVmAllocationPolicy().getHostList();
//		for (int i = 0; i < list.size(); i++) {
//			Host host = list.get(i);
//			Iterator<Vm> iterator = new CopiedIterator(host.getVmList()
//					.iterator());
//			while (iterator.hasNext()) {
//				Vm vm = iterator.next();
//				while (vm.getCloudletScheduler().isFinishedCloudlets()) {
//					Cloudlet cl = vm.getCloudletScheduler()
//							.getNextFinishedCloudlet();
//					if (cl != null) {
//						sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN,
//								cl);
//					}
//				}
//			}

//		}
	}

	// public synchronized void lock() {
	// writersWaiting++;
	// while (writing ) {
	// try {
	// wait();
	// } catch (InterruptedException e) {
	// // reset interrupted state but keep waiting
	// Thread.currentThread().interrupt();
	// }
	// }
	// writersWaiting--;
	// writing = true;
	// }
	//
	// public synchronized void unlock() {
	// writing = false;
	// notifyAll();
	// }
	class vmExcuteThread extends Thread {

		protected boolean canRun = true;
		private myVm curCreatedVm = null;

		public vmExcuteThread(String string) {
			// TODO Auto-generated constructor stub
			super(string);
		}

		public void setCurCreatedVm(myVm vm) {
			this.curCreatedVm = vm;
//			Log.printLine("------此时处理的vm" + curCreatedVm.getId() + ",类型"
//					+ curCreatedVm.getVmType());
		}

		@Override
		public void run() {
			if (canRun) {
				if (curCreatedVm == null)
					return;

				VmCreatedSuccess(curCreatedVm);
			}

		}

		public void stopThread() {
			canRun = false;
		}
	}

}

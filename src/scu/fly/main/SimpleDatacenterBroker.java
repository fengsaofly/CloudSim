package scu.fly.main;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;

public class SimpleDatacenterBroker extends myDatacenterBroker {

	public SimpleDatacenterBroker(String name) throws Exception {
		super(name);
		// TODO Auto-generated constructor stub
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

			// 等待11s,查看是否有新任务
			try {
				Thread.sleep(GlobalParameter.TimeUnit * 11);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//发现有新任务
			if (receivedCloudlets < cloudletCount && receivedCloudlets > 0) {
				clearVM(vm);// 已执行的任务列表清空
//				clearDatacenters();
				createVmsInDatacenter(getDatacenterIdsList().get(0));
			} else {//没有任务了
				Log.printLine(CloudSim.clock() + ": " + getName()
						+ ": 所有任务已经执行完成...");
				clearDatacenters();
				setState(FINISHED);
				finishExecution();
//				hasNewRequests = false;
			}
		}
		// 如果已经提交的任务小于总任务数，表明一部分任务已经完成
		else if (receivedCloudlets < cloudletCount && receivedCloudlets > 0) {
			clearVM(vm);// 已执行的任务列表清空
//			clearDatacenters();
			createVmsInDatacenter(getDatacenterIdsList().get(0));

		}



	}


}

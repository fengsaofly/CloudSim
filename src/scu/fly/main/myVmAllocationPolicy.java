package scu.fly.main;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;




public class myVmAllocationPolicy extends VmAllocationPolicySimple{
	public  long usingTime = 0;
//	 private static int count=0;
//	public static Timer timer = new Timer();

	public myVmAllocationPolicy(List<? extends Host> list) {
		 super(list);
	 }
//	 public static void main(String[] args){
//		myTimerTask myTask = new myTimerTask();
//		timer.schedule(myTask, 0, 1000);
//		 
//		while(count<10){
//		//	Log.printLine("count<100");
//			if(count>5)
//			{
//				Log.printLine("count>5");
//				try {
//					Log.printLine("主线程睡100s");
//					Thread.sleep(1000*2);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		timer.cancel();
//	 }
//	@Override
//	public void deallocateHostForVm(Vm vm) {
//		myHost host = (myHost) getVmTable().remove(vm.getUid());
//		int idx = getHostList().indexOf(host);
//		int pes = getUsedPes().remove(vm.getUid());
//		//recoverHostConfig(vm);
//		if (host != null) {
//			getFreePes().set(idx, getFreePes().get(idx) + pes);
//			host.vmDestroy(vm);
//			
//		}
//	}
//	

	 @Override
	 public boolean allocateHostForVm(Vm vm) {
		 
		 	
			int requiredPes = vm.getNumberOfPes();
			
			List<Integer> freePesTmp = new ArrayList<Integer>();
			for (Integer freePes : getFreePes()) {
				freePesTmp.add(freePes);
			}


		 		boolean result = true;
		 		
		 		myVm myVm = (myVm)vm;
		 		int vmType = myVm.getVmType();
		 		int shortestQueue = Integer.MAX_VALUE;
		 		int hostIndex= 0;
				for(int index=0;index<getHostList().size();index++)
				{
					myHost myHost = (myHost)getHostList().get(index);
					if( (shortestQueue > myHost.getCurQueueLenList().get(vmType))  )
					{
						shortestQueue = myHost.getCurQueueLenList().get(vmType);
						hostIndex = index;
					}
				}
				myHost chosenHost = (myHost)getHostList().get(hostIndex);
			
				//if(hostIndex == chosenHost.getId())  Log.printLine("getid与chosenHost相同");
				
					result = chosenHost.addVm(myVm);//将vm加入vmList，更新可用存储资源，分配cpu和mem给该vm
					chosenHost.run();
					Log.printLine("本次选中的host为："+chosenHost.getId()+"，对应的VM为:"+myVm.getId()+",vm类型为："+myVm.getVmType());
//					if (result) { // if vm were succesfully created in the host
//						getVmTable().put(vm.getUid(), chosenHost);
//						getUsedPes().put(vm.getUid(), requiredPes);
//						getFreePes().set(hostIndex, getFreePes().get(hostIndex) - requiredPes);
//						result = true;
//					
//					} else {
//						freePesTmp.set(hostIndex, Integer.MIN_VALUE);
//					}
				
		

			return result;
		}

		@Override
		public void deallocateHostForVm(Vm vm) {
			Host host = getVmTable().remove(vm.getUid());
			int idx = getHostList().indexOf(host);
			int pes = getUsedPes().remove(vm.getUid());
			if (host != null) {
				host.vmDestroy(vm);
				getFreePes().set(idx, getFreePes().get(idx) + pes);
				host.run();//继续执行下一个任务
			}
		}
}

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import java.util.Timer;

public class myHost extends Host {

	private ArrayList<int[]> vmConfig,curVmConfig;
	private List<Integer> curQueueLenList;
	private List<List<myVm>> waitVmsQueue;
	private List<myVm> curChosenQueue;
	private Timer timer = new Timer();
	private static boolean timerStarted = false;
	//private myThread thread = new myThread(this);
	
	public myHost(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		
		vmConfig = new ArrayList<int[]>();
		vmConfig.add(new int[]{0,1,1}); 
		vmConfig.add(new int[]{2,0,0});
		vmConfig.add(new int[]{1,0,1});
		
		curVmConfig = new ArrayList<int[]>(vmConfig.size());
		curVmConfig = (ArrayList<int[]>) vmConfig.clone();
		
		curChosenQueue = new ArrayList<myVm>();
		
		this.initialQueue();
		this.initialQueueLenth();
		
		
		
	}

	public myHost(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler,ArrayList<int[]> vmCon) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		
		vmConfig =  (ArrayList<int[]>) vmCon.clone();
		
		
		curVmConfig = new ArrayList<int[]>(vmConfig.size());
		curVmConfig = (ArrayList<int[]>) vmConfig.clone();
		
		
		curChosenQueue = new ArrayList<myVm>();
		
		this.initialQueue();
		
		this.initialQueueLenth();
		
	}
	private void initialQueueLenth(){
		
		curQueueLenList = new ArrayList<Integer>(waitVmsQueue.size());
		for(int i=0;i<waitVmsQueue.size();i++)
		{
			curQueueLenList.add(0);
		}
	}
	private void initialQueue(){
		
		waitVmsQueue  = new ArrayList<List<myVm>>(3);
		
		ArrayList<myVm> vmlist1 = new ArrayList<myVm>();
	//	waitVmsQueue.set(0, vmlist1);
		ArrayList<myVm> vmlist2 = new ArrayList<myVm>();
		ArrayList<myVm> vmlist3 = new ArrayList<myVm>();
		waitVmsQueue.add(vmlist1);
		waitVmsQueue.add(vmlist2);
		waitVmsQueue.add(vmlist3);
	}
	
	public ArrayList<int[]> getVmConfig()
	{
		return vmConfig;

	}
	public void restoreCurVmConfig(){
		curVmConfig.clear();
	}
	public void updateVmConfig()
	{
		int[] curVmList = new int[]{0,0,0};
		ArrayList<int[]> tempVmConfig = new ArrayList<int[]>();
		int distance = 0;
		
		for(Vm vm : getVmList()){
			myVm curVm = (myVm)vm;
			curVmList[curVm.getVmType()]++;
		}
//		curVmList =new int[]{1,0,0};
		for(int index=0;index<vmConfig.size();index++){
			
			int[] tempTotalConfig = vmConfig.get(index);
			if(tempTotalConfig.equals(curVmList)){
				restoreCurVmConfig();
				return;
			}
			for(int vmId=0;vmId<tempTotalConfig.length;vmId++){
				
				distance =tempTotalConfig[vmId] - curVmList[vmId];
				if(distance < 0) 	break;
				tempTotalConfig[vmId] = distance;
			}
			if(distance >= 0)	tempVmConfig.add(tempTotalConfig);
			
			
		}
		curVmConfig.clear();
		curVmConfig = (ArrayList<int[]>) tempVmConfig.clone();
		

	}
	public ArrayList<int[]> getCurVmConfig()
	{
		return curVmConfig;

	}
	public boolean setCurQueueLenList(List<Integer> curQueueLenList){
		if (curQueueLenList != null && !curQueueLenList.equals("")) {
			this.curQueueLenList = curQueueLenList;
			return true;
		}
		return false;
	}
	//将Vm加入等待队列
	public boolean addVm(myVm vm){
		int typeId = vm.getVmType();
		boolean successUpdateQueneLen = false;
		boolean successCreateVm = false;
		waitVmsQueue.get(typeId).add(vm);
		
		vm.setHost(this);
		successUpdateQueneLen =  updateCurQueueLenList(typeId);
		if(!successUpdateQueneLen)
		{
			Log.printLine("updateCurQueueLenList error!");
			return successUpdateQueneLen;
		}
		//beginVMsAllocation();
		if(!timerStarted)
		{
			timer.schedule(this, 3*1000, 10*1000);//延后三秒钟进行vm的分配
			timerStarted = true;
		}
	//	thread.start();
		
		return successCreateVm;	
		
	}
	private void sendMsgToDC(myVm vm){
		myDatacenter mc =  (myDatacenter)this.getDatacenter();
		mc.VmCreatedSuccess(vm);
	}
	
	private void beginVMsAllocation(){
		boolean successGetConfig = getConfigFromMySchedulingAlgorithm();
		boolean successCreateVM = false;
		if(!successGetConfig)  {
			Log.printLine("在主机"+this.getId()+"上获取可用配置失败！");
			return ;
			}
		//如果成功获取配置,则创建选择的vms，并在等待队列中删除选中的vms
		for( myVm vm : curChosenQueue){
			successCreateVM = this.vmCreate(vm);
			if(successCreateVM)	
				{
					removeVm((myVm)vm);
					sendMsgToDC(vm);
				}
		}
		if(!successCreateVM){
			//Log.printLine("在主机"+this.getId()+"上创建VM失败！");
		}
	}
	//将Vm移出等待队列，Vm进入PM运行
	public void removeVm(myVm vm){
		int typeId = vm.getVmType();
		waitVmsQueue.get(typeId).remove(vm);
		
//		if(waitVmsQueue.isEmpty() || waitVmsQueue.get(0).isEmpty()) 
//		{
//			timer.cancel();
//			Log.printLine("主机"+this.getId()+"的定时器已经关闭！");
//		}
		updateCurQueueLenList(typeId);
	}

	public List<Integer>  getCurQueueLenList(){
		return this.curQueueLenList;
	}
	public boolean  updateCurQueueLenList(int queueIndex){
		if(curQueueLenList.size()>queueIndex)	
		{
			curQueueLenList.set(queueIndex, waitVmsQueue.get(queueIndex).size());
			return true;
		}
		else
		{
			Log.printLine("队列索引出错！");
			return false;
		}
	}
	@Override
	//将vm从等待队列放入PM
	public boolean vmCreate(Vm vm) {
		boolean success = super.vmCreate(vm);
		return success;
	}
	private boolean getConfigFromMySchedulingAlgorithm(){
		
		curChosenQueue.clear();
		
		int maxWeight = Integer.MIN_VALUE;
		int[] chosenConfig = new int[]{0,0,0};
		for(int[] curAvailableConfig : curVmConfig){
			int tempWeight = 0;
			for(int tempVmAvailableIndex = 0 ;tempVmAvailableIndex < curAvailableConfig.length ; tempVmAvailableIndex++){
				tempWeight += curAvailableConfig[tempVmAvailableIndex]*curQueueLenList.get(tempVmAvailableIndex);
			}
			if(tempWeight>maxWeight) {
				maxWeight = tempWeight;
				chosenConfig = curAvailableConfig.clone();			
			}
		}
		for(int i=0;i<chosenConfig.length;i++)
		{
			chosenConfig[i]= Math.min(chosenConfig[i], waitVmsQueue.get(i).size());
		}
		List<Integer> firstQueueLenList = new ArrayList<Integer>(3);
		for(int i=0;i<3;i++)
		{
			firstQueueLenList.add(0);
		}
		//判断chosenConfig是否为空
		boolean isNull = true;
		for(int vmValue:chosenConfig)
		{
			if(vmValue!=0)  
			{
				isNull = false;  
				break;
			}
				
				
		}
		//若chosenConfig为空
		if(isNull){
			//Log.printLine("主机"+this.getId()+"暂时没有新任务！");
			return true;
		}
		//若chosenConfig不为空，则根据配置进行VM选择
		for(int vmType=0;vmType<chosenConfig.length;vmType++){
			int waitTypeLen = waitVmsQueue.get(vmType).size();
			for(int i=0;i<chosenConfig[vmType];i++)
			{
				curChosenQueue.add(waitVmsQueue.get(vmType).get(waitTypeLen-1));
				//removeVm(waitVmsQueue.get(vmType).get(waitTypeLen-1));
				
			}
		}
		Log.printLine("chosenConfig = "+printArray(chosenConfig));
		//查看是否有等待任务
//		for(List<myVm> vm:waitVmsQueue)
//		{
//			if(vm.size()!=0){
//				return true;
//			}
//			else continue;
//		}
		//Log.printLine("主机"+this.getId()+"暂时没有新任务！");
		return true;
	}
	@Override
	 public void run() {
		beginVMsAllocation();
	  }
	 public String printArray(int[] array){ 
//		  System.out.println("第一种方法： ");
		  // 用泛型，1.5后可以这样使用：
		 String arrayResult = "";
		  for(int i : array){
		     arrayResult+=i + " ";
		  } 
		 // arrayResult+="\n";
		  
		  return arrayResult;
	 }
	 


}
 

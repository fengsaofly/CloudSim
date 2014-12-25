package scu.fly.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

public class FlyHost extends myHost {

	public FlyHost(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler,PowerModel powerModel) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
		// TODO Auto-generated constructor stub
	}
	
	public List<myVm> getConfigFromFlySchedulingAlgorithm(){
		
		
		int multiple = 2;
		double maxWeight = Integer.MIN_VALUE;
		int[] chosenConfig = new int[]{0,0,0};
		//获取可用配置
		@SuppressWarnings("unchecked")
		ArrayList<int[]> actualAvailableConfig = new ArrayList<int[]>();
		for(int j=0;j<curVmAvailableConfig.size();j++){
			int[] oneConfig = curVmAvailableConfig.get(j).clone();
			for(int i=0;i<oneConfig.length;i++){
				oneConfig[i]= Math.min(oneConfig[i], curQueueLenList.get(i));
			}
			actualAvailableConfig.add(oneConfig);
		}
	
		//可用配置去重
		actualAvailableConfig = (ArrayList<int[]>) removeDuplicate(actualAvailableConfig);
		if(actualAvailableConfig.contains(new int[]{0,0,0}))  return null;
		
		double maxWeightCPU = 0,maxWeightMEM = 0;
		boolean needHandleLimit = false;
		for(int i=0;i< actualAvailableConfig.size();i++){
			int[] curAvailableConfig = actualAvailableConfig.get(i);
			//if(Arrays.equals(curAvailableConfig, new int[]{0,0,0})) return false;
			double tempWeightCPU = 0,tempWeightMEM = 0,tempWeight=0;
			for(int j = 0 ;j < curAvailableConfig.length ; j++){
				/**
				 * 阈值处理
				 * 如果某一队列达到阈值，则选取可用配置中包含该队列最多的一个配置——maxWeightConfigForVMType*
				 * */
				int threshold = multiple*maxWeightConfigForVMType(j)[j];
				if(curQueueLenList.get(j)>threshold ){
					
					needHandleLimit = true;
					
					chosenConfig = maxWeightConfigForVMType(j);
					
					break;
				}
				//否则，选取综合利用率最大的
				if(curAvailableConfig[j]==0) continue;
				tempWeightMEM += curAvailableConfig[j]*vmMCSList.get(0)[j];
				tempWeightCPU += curAvailableConfig[j]*vmMCSList.get(1)[j];
				
			}
			//需要处理阈值，直接跳出
			if (needHandleLimit) {
				maxWeightCPU = 0;
				maxWeightMEM = 0;
				for (int vmType = 0; vmType < chosenConfig.length; vmType++) {
					int choseNum = chosenConfig[vmType];
					for (int num = 0; num < choseNum; num++) {
						maxWeightCPU+=vmMCSList.get(0)[vmType];
						maxWeightMEM+=vmMCSList.get(1)[vmType];
					}
				}
				break;
			}
			//选择最大的权重配置
			tempWeight =(tempWeightCPU/this.getNumberOfPes()+tempWeightMEM/this.getRam()*1000)/2.0;
			if(tempWeight>maxWeight) {
				maxWeightCPU = tempWeightCPU;
				maxWeightMEM = tempWeightMEM;
				maxWeight = tempWeight;
				chosenConfig = curAvailableConfig.clone();			
			}
		}
	
		//若chosenConfig不为空，则根据配置进行VM选择
		curChosenQueue.clear();
		
		for(int vmType=0;vmType<chosenConfig.length;vmType++){
			
			for(int numOfvmType=0;numOfvmType<chosenConfig[vmType];numOfvmType++)
			{
				int waitTypeLen = waitVmsQueue.get(vmType).size();
				if (waitTypeLen>0) {
					curChosenQueue.add(waitVmsQueue.get(vmType).get(waitTypeLen-1));
					removeVm(waitVmsQueue.get(vmType).get(waitTypeLen-1));
				}
			
				
			}
		}

		Log.printLine("chosenConfig = "+printArray(chosenConfig));

		return curChosenQueue;
	}
	/**
	 * 属于cpu的最大利用率配置
	 * @param vm
	 * @return
	 */
	public boolean belongMaxConfig(myVm vm)
	{
		int type = vm.getVmType();
		if(type == 0 || type == 2) 
			return true;
		else
			return false;
	}
	/**
	 * 判断能否继续分配vm
	 * @return
	 */
	public boolean cannotProvision()
	{
		//能创建某种vm
		if(this.getNumberOfFreePes() >= 6.5 && this.getRamProvisioner().getAvailableRam() >= 17.1*1000 
				&& curQueueLenList.get(1) > 0)
			return false;
		else if (this.getNumberOfFreePes() >= 8 && this.getRamProvisioner().getAvailableRam() >= 15*1000
				&& curQueueLenList.get(0) > 0)
			return false;
		else if(this.getNumberOfFreePes() >= 20 && this.getRamProvisioner().getAvailableRam() >= 7*1000
				&& curQueueLenList.get(2) > 0)
			return false;

		return false;
	
	}
	@Override
	//将Vm加入等待队列
	public boolean addVm(myVm vm){
		int typeId = vm.getVmType();
		boolean successUpdateQueneLen = false;
		waitVmsQueue.get(typeId).add(vm);
		
		//vm.setHost(this);
		successUpdateQueneLen =  updateCurQueueLenList(typeId);
		if(!successUpdateQueneLen)
		{
			Log.printLine("updateCurQueueLenList error!");
			return false;
		}
		return true;	
		
	}


}

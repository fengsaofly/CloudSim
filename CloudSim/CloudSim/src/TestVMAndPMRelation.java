/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;


import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
//import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import java.util.Timer;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * An example showing how to pause and resume the simulation,
 * and create simulation entities (a DatacenterBroker in this example)
 * dynamically.
 */
public class TestVMAndPMRelation {

	public static DatacenterBroker broker ;
	public static  myDatacenter   datacenter0 ;
	public static int SLOTSCOUNT = 1;
	//共有常量，云中相同主机数和主机配置
	public static int MCS[] = {30,30,4000};
	public static int HOSTNUM = 100;
	public static int vmId = 0;
	public static int curVmId = 0;
	/** The cloudlet list. */
	private static List<woCloudlet> cloudletList = new LinkedList<woCloudlet>();

	/** The vmlist. */
	private static List<myVm> vmlist = new LinkedList<myVm>();

	private static List<myVm> createVM(int userId, int vms, int idShift) {
		//Creates a container to store VMs. This list is passed to the broker later
		LinkedList<myVm> list = new LinkedList<myVm>();

		//VM Parameters
		long size = 10000; //image size (MB)
		int ram = 512; //vm memory (MB)
		int mips = 250;
		long bw = 1000;
		int pesNumber = 1; //number of cpus
		String vmm = "Xen"; //VMM name

		//create VMs
		myVm[] vm = new myVm[vms];

		for(int i=0;i<vms;i++){
			vm[i] = new myVm(idShift + i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			list.add(vm[i]);
		}

		return list;
	}
	
	private static List<myVm> createVM(int userId, int vms, int idShift,int type) {
		//Creates a container to store VMs. This list is passed to the broker later
		LinkedList<myVm> list = new LinkedList<myVm>();
		double memSize=0,cpuSize=0;
		int storageSize=0;
		String error = null;
		switch(type){
			case 0:
				memSize = 15;
				cpuSize = 8;
				storageSize = 1690;
			case 1:
				memSize = 17.1;
				cpuSize = 6.5;
				storageSize = 420;
			case 2:
				memSize = 7;
				cpuSize = 20;
				storageSize = 1690;
			default:
				error = "type不符合标准!";
				
		}
		if( "".equals(error) ) return null;
		
		else
		{
			//VM Parameters
			long size = storageSize; //image size (MB)
			int ram = (int) (512*memSize); //vm memory (MB)
			int mips = 250;
			long bw = 1000;
			int pesNumber = (int) (cpuSize); //number of cpus
			String vmm = "Xen"; //VMM name
	
			//create VMs
			myVm[] vm = new myVm[vms];
	
			for(int i=0;i<vms;i++){
				vm[i] = new myVm(idShift + i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared(),type);
				list.add(vm[i]);
			}
	
			return list;
		}
	}


	private static List<woCloudlet> createCloudlet(int userId, int cloudlets, int idShift){
		// Creates a container to store Cloudlets
		LinkedList<woCloudlet> list = new LinkedList<woCloudlet>();

		//cloudlet parameters
		long length = 40000;
		long fileSize = 50;
		long outputSize = 50;  
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		woCloudlet[] cloudlet = new woCloudlet[cloudlets];

		for(int i=0;i<cloudlets;i++){
			cloudlet[i] = new woCloudlet(idShift + i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}
	private static List<woCloudlet> createCloudlet(int userId, int cloudlets, int idShift,int size){
		// Creates a container to store Cloudlets
		LinkedList<woCloudlet> list = new LinkedList<woCloudlet>();

		//cloudlet parameters
		long length = 40000*size;
		long fileSize = 50;
		long outputSize = 50;  
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		woCloudlet[] cloudlet = new woCloudlet[cloudlets];

		for(int i=0;i<cloudlets;i++){
			cloudlet[i] = new woCloudlet(idShift + i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}
	private static int random(double A,double B){
		
		double x = Math.random();
		return (int)(x*(A-B)+B);
	}
	private static  int randGenerateCloudletSize(){
		double rand = Math.random();
		int randSize;
		if(rand < 0.7) 	randSize = random(1,50);
		else if(rand >0.85)  randSize = random(451,500);
		else            randSize = random(251,300);
		return randSize;
	}
	private static  ArrayList<List<Integer>> randGenerateVmsAndCloudlets(){
		
		double[] config = {1,(double)1/3,(double)2/3};
		double[] probabilities = new double[3];
		double arg = 0.5;
		List<List<Integer>> T = new ArrayList<List<Integer>>();
		
	
		ArrayList<Integer> vmlist1 = new ArrayList<Integer>();
		ArrayList<Integer> vmlist2 = new ArrayList<Integer>();
		ArrayList<Integer> vmlist3 = new ArrayList<Integer>();
		T.add(vmlist1);
		T.add(vmlist2);
		T.add(vmlist3);
		
		int userId =  broker.getId();
		
	
		
		double rand = 0;
		for(int i=0;i<config.length;i++)
		{
			probabilities[i] = config[i]*arg/130.5*100;
		}
		for(int j=0;j<HOSTNUM;j++)
		{
			for(int vmlistId = 0; vmlistId < probabilities.length; vmlistId++)
			{
				rand = Math.random();
				if(rand < probabilities[vmlistId])
				{
					//[countVM[pId].length] = randGenerateCloudletSize();
					int randomCloudletSize = randGenerateCloudletSize();
					T.get(vmlistId).add(randomCloudletSize);
					myVm curVm = createVM(userId, 1, vmId,vmlistId).get(0);
					vmlist.add(curVm);
					
					woCloudlet curCloudlet =  createCloudlet(userId, 1, vmId++, randomCloudletSize).get(0);
					cloudletList.add(curCloudlet);
				}
				
			}
			//vmlist.add(new int[]{randGenerateCloudletSize(),randGenerateCloudletSize(),randGenerateCloudletSize()});
		}
		
		
		
		return (ArrayList<List<Integer>>) T;
		
	}
	private static void bindCloudletToVm(){
		
		Log.printLine("========== 当前产生"+(vmId-curVmId)+ "个任务 ==========");
		for(;curVmId<vmId;curVmId++)
		{
			broker.bindCloudletToVm(curVmId, curVmId);
		}
		//broker.bindCloudletToVm( curCloudlet.getCloudletId() , curVm.getId() );
	
	}
	////////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		Log.printLine("Starting CloudSim...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
			datacenter0 = createDatacenter("Datacenter_0");
		

			//Third step: Create Broker
			broker = createBroker("Broker_0");
//			int brokerId = broker.getId();

			//Fourth step: Create VMs and Cloudlets and send them to broker
//			vmlist = createVM(brokerId, 5, 0); //creating 5 vms
//			cloudletList = createCloudlet(brokerId, 10, 0); // creating 10 cloudlets
			
//			int slotsCount = SLOTSCOUNT;
//			
//			Timer timer = new Timer();
			//用于判断是或否主机上有等待任务
//			List<Integer> firstQueueLenList = new ArrayList<Integer>(3);
//			for(int i=0;i<3;i++)
//			{
//				firstQueueLenList.add(0);
//			}
			
//			while(slotsCount>0){//这个是用来停止此任务的,否则就一直循环执行此任务了
//					try {
//						
//						  //设置任务计划，启动和间隔时间
//						  timer.schedule(new RandGenerateVMsAndCloudlets(), 0, 100*1000);
//						
//					      if(slotsCount == SLOTSCOUNT)	{
//						  for(Host host :  datacenter0.getHostList()){
//								  
//								 myHost myhost = (myHost)host;
//							//	 if(!myhost.getCurQueueLenList().equals(firstQueueLenList)){
//								 timer.schedule(myhost, 0, 10*1000);
//							//	 }
//						  }
//						
//							
//					      }
//						  slotsCount--;
//						  if(slotsCount == 0)   {
//								timer.cancel();
//								break;
//						  }
//					
//						  
//					} catch (Exception e) {
//					// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//			}
		//	timer.cancel();//使用这个方法退出任务
			randGenerateVmsAndCloudlets();
			
			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);

			// A thread that will create a new broker at 200 clock time
		
			
			bindCloudletToVm();
			// Fifth step: Starts the simulation
			CloudSim.startSimulation();  

			// Final step: Print results when simulation is over
			List<woCloudlet> newList = broker.getCloudletReceivedList();
	
			CloudSim.stopSimulation();
	
			printCloudletList(newList);
	
			//Print the debt of each user to each datacenter
			datacenter0.printDebts();
	//		myHost myhost = (myHost)(datacenter0.getHostList().get(0)) ;
			
				
				
				
			Log.printLine("MySimulation finished!");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	  
	}
		
private static List<myHost> createHosts(int[] peList)
{
	List<myHost> hostList = new ArrayList<myHost>();
	
	int mips = 1000;
	
	int hostId=0;
	int ram = 250*MCS[0]; //host memory (MB)
	long storage = MCS[2]; //host storage
	int bw = 10000;
	

		for (int peNumPerHost : peList)
		{
			List<Pe> peNumForPerHost = new ArrayList<Pe>();
			int peId = 0;
			while( peId < peNumPerHost)
			{
				
				peNumForPerHost.add(new Pe(peId, new PeProvisionerSimple(mips))); 
				peId++ ;
			}
			myHost h = 		new myHost(
    				hostId++,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peNumForPerHost,
    				new VmSchedulerTimeShared(peNumForPerHost)
			);
			hostList.add(
					h
	    			)
	    		; 
//			h.updateVmConfig();
		}
	
	
	
		
	return hostList;
	
}
	private static myDatacenter createDatacenter(String name){

		
		int hostNum = HOSTNUM;
		int peNum = MCS[1] ;
		
		int peList[]=new int[hostNum];
		for(int i=0;i<hostNum;i++)
			peList[i]=peNum;
		
		List<myHost> hostList = createHosts(peList);
		
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.1;	// the cost of using storage in this resource
		double costPerBw = 0.1;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		myDatacenter datacenter = null;
		try {
			datacenter = new myDatacenter(name, characteristics, new myVmAllocationPolicy(hostList), storageList, 0);
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static DatacenterBroker createBroker(String name){

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(List<woCloudlet> list) {
		int size = list.size();
		woCloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == woCloudlet.SUCCESS){
				Log.print("SUCCESS");

				Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
						indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +
						indent + indent + dft.format(cloudlet.getExecStartTime())+ indent + indent + indent + dft.format(cloudlet.getFinishTime()));
			}
		}

	}
	public class MyListener implements ServletContextListener {
		  
		  private Timer timer = null;

		  public void contextInitialized(ServletContextEvent event) {
			  timer = new Timer(true);
			  //设置任务计划，启动和间隔时间
			  timer.schedule(new RandGenerateVMsAndCloudlets(), 0, 10*1000);
			  for(Host host :  datacenter0.getHostList()){
				  
				 myHost myhost = (myHost)host;
				 if(myhost.getCurQueueLenList().size()>0){
					 timer.schedule(myhost, 0,  10*1000);
				 }
			  }
		  }
		  public void contextDestroyed(ServletContextEvent event) {
		    timer.cancel();
		  }
		  
		
	}
	
	public  static class RandGenerateVMsAndCloudlets extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			randGenerateVmsAndCloudlets();
			
			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);

			// A thread that will create a new broker at 200 clock time
		
			
			bindCloudletToVm();
			
			
		}
		
	}

}

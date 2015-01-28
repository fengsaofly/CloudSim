package scu.fly.main;
/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


import DataCenterBrokerModified.DatacenterBrokerModifiedRealTime;
import DataCenterBrokerModified.VmComparator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * A simple example showing how to create a datacenter with two hosts and run
 * two cloudlets on it. The cloudlets run in VMs with different MIPS
 * requirements. The cloudlets will take different time to complete the
 * execution depending on the requested VM performance.
 */
public class TestRealTime {

    /**
     * The cloudlet list.
     */
    private static List<Cloudlet> cloudletList;

    /**
     * The vmlist.
     */
    private static List<VmComparator> vmlist;

    static Cloudlet addCloudlet(int pesNumber) {
        int id = 0;
        long length = 40000;
        long fileSize = 300;
        long outputSize = 300;
        UtilizationModel utilizationModel = new UtilizationModelFull();
        Cloudlet cloudlet1 = new Cloudlet(id, (long) (length*((Math.random()+0.1)*10)), pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
        return cloudlet1;
    }

    /**
     * Creates main() to run this example
     */
    public static void main(String[] args) {
    	
    	
//    	int []a = new int[]{2,4,5,1};
//    	int []res = new int[a.length+1];
//    	int size = 1;
//    	for (int i : a) {
//			size*=(i+1);
//		}
//    	
//    	
//    	MathUtil.dfs(res,a,0,size);
    	
		int mips = 1000;

		int hostId = 0;
		int ram = GlobalParameter.HOST_RAM[0] ;// * 1000; // host memory (MB)
		long storage = GlobalParameter.HOST_STORAGE[0]; // host storage
		int bw = GlobalParameter.HOST_BW;
		
		List<Pe> peNumForPerHost = new ArrayList<Pe>();
		int peId = 0;
		while (peId < 30) {

			peNumForPerHost
					.add(new Pe(peId, new PeProvisionerSimple(mips)));
			peId++;
		}
    	myHost host = new myHost(0, new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw), storage, peNumForPerHost,
				new VmSchedulerTimeShared(peNumForPerHost), null);
//    	double imF = 0;
//    	for (int i = 0; i < 4; i++) {
//    		imF = host.getVMImpactFactor(i);
//    		System.out.println();
//		}
    	host.updateConfig();
    	ArrayList<int []> l = host.getCurVmAvailableConfig();
    	int[] res = new int[4];
    	for (int[] is : l) {
    		for (int i = 0; i < is.length; i++) {
				res[i]+=is[i];
			}
			
		}
    	MathUtil.printArray(res, res.length);
    	
    }

    private static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        //    our machine
        List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<Pe>();

        int mips = 10000;

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new Pe(1, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new Pe(2, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new Pe(3, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new Pe(4, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new Pe(5, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new Pe(6, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new Pe(7, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new Pe(8, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new Pe(9, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new Pe(10, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        //4. Create Hosts with its id and list of PEs and add them to the list of machines
        int hostId = 0;
        int ram = 2048000; //host memory (MB)
        long storage = 100000000; //host storage
        int bw = 10000000;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerTimeShared(peList)
                )
        ); // This is our first machine

        //create another machine in the Data center
        List<Pe> peList2 = new ArrayList<Pe>();

        peList2.add(new Pe(0, new PeProvisionerSimple(mips)));

        hostId++;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList2,
                        new VmSchedulerTimeShared(peList2)
                )
        ); // This is our second machine

		// 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.001;	// the cost of using storage in this resource
        double costPerBw = 0.0;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
    //to the specific rules of the simulated scenario
    private static DatacenterBrokerModifiedRealTime createBroker() {

        DatacenterBrokerModifiedRealTime broker = null;
        try {
            broker = new DatacenterBrokerModifiedRealTime("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    /**
     * Prints the Cloudlet objects
     *
     * @param list list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
                + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

    }
}

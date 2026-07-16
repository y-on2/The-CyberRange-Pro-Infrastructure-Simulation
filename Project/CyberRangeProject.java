package Project;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class CyberRangeProject {

    public static void main(String[] args) {
        try {
            // Loop 1: Scheduling Methods (TimeShared vs SpaceShared)
            String[] schedulerNames = {"TimeShared", "SpaceShared"};
            
            // Loop 2: Allocation Policies (First Fit, Best Fit, Worst Fit)
            String[] policyNames = {"First Fit", "Best Fit", "Worst Fit"};
            
            // Nested Loop: Run every combination (2 x 3 = 6 Simulations)
            for (int s = 0; s < 2; s++) {
                for (int p = 1; p <= 3; p++) {
                    
                    String currentScheduler = schedulerNames[s];
                    String currentPolicy = policyNames[p-1];
                    
                    Log.printLine("------------------------------------------------");
                    Log.printLine("RUNNING: " + currentScheduler + " + " + currentPolicy);
                    Log.printLine("------------------------------------------------");

                    // 1. Initialize
                    int num_user = 1;
                    Calendar calendar = Calendar.getInstance();
                    boolean trace_flag = false;
                    CloudSim.init(num_user, calendar, trace_flag);

                    // 2. Load Network Topology
                    NetworkTopology.buildNetworkTopology("topology.brite");

                    // 3. Create Datacenter with specific Policy (p)
                    Datacenter datacenter = createDatacenter("CyberRange_DC", p);

                    // 4. Create Broker
                    DatacenterBroker broker = new DatacenterBroker("Broker");
                    int brokerId = broker.getId();

                    // 5. Map Nodes
                    NetworkTopology.mapNode(brokerId, 0);
                    NetworkTopology.mapNode(datacenter.getId(), 2);

                    // 6. Create VMs (Pass the Scheduler Type: 0=Time, 1=Space)
                    List<Vm> vmlist = createVMs(brokerId, s);
                    broker.submitVmList(vmlist);

                    // 7. Create Cloudlets
                    List<Cloudlet> cloudletList = createCloudlets(brokerId, 15);
                    broker.submitCloudletList(cloudletList);

                    // 8. Start Simulation
                    CloudSim.startSimulation();

                    // 9. Stop and Export
                    List<Cloudlet> newList = broker.getCloudletReceivedList();
                    CloudSim.stopSimulation();

                    // Generate Filename: -----.csv
                    String filename = "results_" + currentScheduler + "_" + currentPolicy.replace(" ", "") + ".csv";
                    printCloudletListAndExportCSV(newList, filename);
                    
                    Log.printLine("Saved to: " + filename);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happened");
        }
    }

    private static Datacenter createDatacenter(String name, int policyType) {
        List<Host> hostList = new ArrayList<Host>();

        // Host 0: Big Server (32GB RAM)
        List<Pe> peList1 = new ArrayList<Pe>();
        for(int k=0; k<4; k++) peList1.add(new Pe(k, new PeProvisionerSimple(2500)));
        hostList.add(new Host(
            0, new RamProvisionerSimple(32768), new BwProvisionerSimple(10000), 
            1000000, peList1, new VmSchedulerTimeShared(peList1)
        ));

        // Host 1: Small Server (16GB RAM)
        List<Pe> peList2 = new ArrayList<Pe>();
        for(int k=0; k<4; k++) peList2.add(new Pe(k, new PeProvisionerSimple(2500)));
        hostList.add(new Host(
            1, new RamProvisionerSimple(16384), new BwProvisionerSimple(10000), 
            1000000, peList2, new VmSchedulerTimeShared(peList2)
        ));

        // Select Allocation Policy
        VmAllocationPolicy allocationPolicy = null;
        switch(policyType) {
            case 1: allocationPolicy = new VmAllocationPolicySimple(hostList); break; // First Fit
            case 2: allocationPolicy = new VmAllocationPolicyBestFit(hostList); break; // Best Fit
            case 3: allocationPolicy = new VmAllocationPolicyWorstFit(hostList); break; // Worst Fit
            default: allocationPolicy = new VmAllocationPolicySimple(hostList);
        }

        String arch = "x86"; String os = "Linux"; String vmm = "Xen";
        double time_zone = 10.0; double cost = 3.0;        
        double costPerSec = 0.05; double costPerMem = 0.02;      
        double costPerStorage = 0.001; double costPerBw = 0.01;       

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, 
                costPerSec, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, allocationPolicy, new LinkedList<Storage>(), 0);
        } catch (Exception e) { e.printStackTrace(); }

        return datacenter;
    }
    
    // UPDATED: Now accepts "schedulerType" (0 = TimeShared, 1 = SpaceShared)
    private static List<Vm> createVMs(int brokerId, int schedulerType) {
        List<Vm> vms = new ArrayList<Vm>();
        
        CloudletScheduler scheduler;
        if (schedulerType == 0) {
            scheduler = new CloudletSchedulerTimeShared();
        } else {
            scheduler = new CloudletSchedulerSpaceShared();
        }

        // We create new scheduler instances for every VM because they can't share the same object
        
        // AttackBox VMs
        for (int i = 0; i < 10; i++) {
            // Need a fresh scheduler instance for each VM
            CloudletScheduler s = (schedulerType == 0) ? new CloudletSchedulerTimeShared() : new CloudletSchedulerSpaceShared();
            vms.add(new Vm(i, brokerId, 1000, 1, 2048, 1000, 20000, "Xen", s));
        }
        // VictimServer VMs
        for (int i = 10; i < 15; i++) { 
            CloudletScheduler s = (schedulerType == 0) ? new CloudletSchedulerTimeShared() : new CloudletSchedulerSpaceShared();
            vms.add(new Vm(i, brokerId, 2000, 1, 4096, 1000, 50000, "VMware", s));
        }
        return vms;
    }
    
    private static List<Cloudlet> createCloudlets(int brokerId, int cloudletsNumber) {
        List<Cloudlet> list = new ArrayList<Cloudlet>();
        UtilizationModel utilizationModel = new UtilizationModelFull();
        for (int i = 0; i < cloudletsNumber; i++) {
            Cloudlet cloudlet = new Cloudlet(i, 40000, 1, 300, 300, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            list.add(cloudlet);
        }
        return list;
    }

    private static void printCloudletListAndExportCSV(List<Cloudlet> list, String filename) {
        int size = list.size();
        Cloudlet cloudlet;
        DecimalFormat dft = new DecimalFormat("###.##");
        
        try {
            java.io.FileWriter csvWriter = new java.io.FileWriter(filename);
            csvWriter.append("CloudletID,Status,VmID,Time,StartTime,FinishTime,Cost\n");

            for (int i = 0; i < size; i++) {
                cloudlet = list.get(i);
                if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                    double cost = cloudlet.getActualCPUTime() * 0.05; 
                    csvWriter.append(cloudlet.getCloudletId() + ",SUCCESS," + cloudlet.getVmId() + "," +
                            dft.format(cloudlet.getActualCPUTime()) + "," + dft.format(cloudlet.getExecStartTime()) + "," +
                            dft.format(cloudlet.getFinishTime()) + "," + String.format("%.2f", cost) + "\n");
                }
            }
            csvWriter.flush();
            csvWriter.close();
            Log.printLine("Results exported to '" + filename + "'");
        } catch (java.io.IOException e) {
            Log.printLine("Error writing CSV file: " + e.getMessage());
        }
    }
}
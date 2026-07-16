package Project; // Or package Project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;

public class VmAllocationPolicyBestFit extends VmAllocationPolicy {
    private Map<String, Host> vmTable = new HashMap<String, Host>();

    public VmAllocationPolicyBestFit(List<? extends Host> list) {
        super(list);
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        Host bestHost = null;
        // Start with largest possible value so we can find smaller ones
        double minFreeRam = Double.MAX_VALUE; 

        for (Host host : getHostList()) {
            // Check if host has enough resources
            if (host.isSuitableForVm(vm)) {
                // Calculate remaining RAM if we put the VM here
                double freeRam = host.getRamProvisioner().getAvailableRam() - vm.getRam();
                
                // We want the host with the LEAST free RAM (Tightest fit)
                if (freeRam < minFreeRam) {
                    minFreeRam = freeRam;
                    bestHost = host;
                }
            }
        }

        if (bestHost != null) {
            return allocateHostForVm(vm, bestHost);
        }
        return false;
    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        if (host.vmCreate(vm)) {
            vmTable.put(vm.getUid(), host);
            Log.formatLine("%.2f: VM #%d has been allocated to the host #%d", 
                CloudSim.clock(), vm.getId(), host.getId());
            return true;
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        return null; // Not using dynamic migration for this project
    }

    @Override
    public void deallocateHostForVm(Vm vm) {
        Host host = vmTable.remove(vm.getUid());
        if (host != null) {
            host.vmDestroy(vm);
        }
    }

    @Override
    public Host getHost(Vm vm) {
        return vmTable.get(vm.getUid());
    }

    @Override
    public Host getHost(int vmId, int userId) {
        return vmTable.get(Vm.getUid(userId, vmId));
    }
}
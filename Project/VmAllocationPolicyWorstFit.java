package Project; // Or package Project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;

public class VmAllocationPolicyWorstFit extends VmAllocationPolicy {
    private Map<String, Host> vmTable = new HashMap<String, Host>();

    public VmAllocationPolicyWorstFit(List<? extends Host> list) {
        super(list);
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        Host worstHost = null;
        // Start with smallest value so we can find larger ones
        double maxFreeRam = Double.MIN_VALUE;

        for (Host host : getHostList()) {
            if (host.isSuitableForVm(vm)) {
                double freeRam = host.getRamProvisioner().getAvailableRam() - vm.getRam();
                
                // We want the host with the MOST free RAM (Loosest fit)
                if (freeRam > maxFreeRam) {
                    maxFreeRam = freeRam;
                    worstHost = host;
                }
            }
        }

        if (worstHost != null) {
            return allocateHostForVm(vm, worstHost);
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
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) { return null; }

    @Override
    public void deallocateHostForVm(Vm vm) {
        Host host = vmTable.remove(vm.getUid());
        if (host != null) host.vmDestroy(vm);
    }

    @Override
    public Host getHost(Vm vm) { return vmTable.get(vm.getUid()); }

    @Override
    public Host getHost(int vmId, int userId) { return vmTable.get(Vm.getUid(userId, vmId)); }
}
# The CyberRange Pro Infrastructure Simulation

**Academic Seminar Report Repository**  
**Degree:** Bachelor in Cyber Security.
**Authors:** Galal Mohammed and  Ezz Aldeen Alshalafi.
**Supervisor:** Dr. Al Anoud.
**Date:** December 2025.

---

## 1. Abstract
This repository contains the CloudSim-based infrastructure simulation for **CyberRange Pro**, an on-demand virtual cybersecurity training platform.. The system models a "Capture the Flag" (CTF) environment where a high volume of student requests (Red Team) targets vulnerable servers (Blue Team).. The primary objective of this simulation is to evaluate distinct software scheduling algorithms and hardware allocation policies to optimize resource utilization, minimize latency, and reduce operational costs.. 

## 2. Infrastructure Architecture and  Network Topology
To accurately simulate realistic network conditions and potential bottlenecks, a custom network topology was implemented utilizing the BRITE format (`topology.brite`).. 

### 2.1 Network Nodes and Links
* **Node 0 (User Gateway):** Represents the external entry point for remote students..
* **Node 1 (Core Switch):** A high-speed internal routing switch..
* **Node 2 (Datacenter):** The physical facility housing the simulation servers..
* **Internet Link (Node 0 ➔ Node 1):** Configured with 100ms delay and 10 MB/s bandwidth to simulate remote connection latency..
* **LAN Link (Node 1 ➔ Node 2):** Configured with 1ms delay and 100 GB/s bandwidth to simulate high-speed internal fiber connectivity..

### 2.2 Heterogeneous Datacenter Specifications
The simulated datacenter (`CyberRange_DC`) utilizes a heterogeneous hardware configuration to evaluate allocation efficiency across diverse resources.:
* **Host 0 (Primary Server):** 32 GB RAM, 1 TB Storage, 10,000 MIPS (Quad Core)..
* **Host 1 (Secondary Server):** 16 GB RAM, 1 TB Storage, 10,000 MIPS (Quad Core)..

## 3. Workload Characterization
The virtualized services provided within the simulation are divided into two distinct Virtual Machine (VM) profiles:
* **AttackBox Service (Qty: 10):** Lightweight instances (Kali Linux) allocated for individual students, requiring 2 GB RAM and 1000 MIPS..
* **VictimServer Service (Qty: 5):** Resource-intensive targets featuring intentional vulnerabilities, requiring 4 GB RAM and 2000 MIPS..

## 4. Simulation Methodology
A two-layered simulation strategy was conducted across 6 distinct scenarios to benchmark performance..

* **Layer 1: Cloudlet Scheduling (Software Level)**.
  * *TimeShared:* Simulates CPU multitasking (introducing context-switching overhead)..
  * *SpaceShared:* Simulates a FIFO queue granting dedicated CPU access..
* **Layer 2: VM Allocation Policy (Hardware Level)**.
  * *First Fit:* Allocates VM to the first host with sufficient capacity..
  * *Best Fit:* Allocates VM to the host with the tightest capacity margin..
  * *Worst Fit:* Allocates VM to the host with the largest available capacity..

## 5. Results and Analysis
The simulation calculated total costs at a rate of $0.05 per second of CPU time..

| Scenario | Scheduler | Allocation Policy | Avg Finish Time | Total Cost | Reliability Status |
| :---: | :--- | :--- | :---: | :---: | :--- |
| 1 | TimeShared. | First Fit. | 551.77 ms. | $35.00. | Failed (2 VMs). |
| 2 | TimeShared. | Best Fit. | 545.10 ms. | $30.00. | Failed (1 VM). |
| 3 | TimeShared. | Worst Fit. | 551.77 ms. | $35.00. | Failed (2 VMs). |
| 4 | SpaceShared. | First Fit. | 546.43 ms. | $27.00. | Failed (2 VMs). |
| **5** | **SpaceShared**. | **Best Fit**. | **542.43 ms**. | **$26.00**. | **Optimal (1 VM Failed)**. |
| 6 | SpaceShared. | Worst Fit. | 546.43 ms. | $27.00. | Failed (2 VMs). |

### 5.1 Key Findings
1. **Hardware Resource Fragmentation:** The *First Fit* and *Worst Fit* policies demonstrated severe inefficiency by populating the primary 32GB host with small VMs initially.. Consequently, sufficient continuous memory was unavailable for subsequent resource-heavy `VictimServer` VMs, leading to allocation failures.. The *Best Fit* algorithm successfully mitigated this by segregating workloads effectively..
2. **Scheduling Overhead Elimination:** Transitioning from *TimeShared* to *SpaceShared* scheduling yielded a ~13% reduction in total operational cost (from $30.00 to $26.00) by eliminating CPU slicing overhead, allowing resources to be released systematically..

## 6. Conclusion
Empirical results from this simulation dictate that **SpaceShared Scheduling combined with a Best Fit Allocation Policy** is the optimal configuration for the CyberRange Pro architecture.. This specific integration maximizes host capacity, ensures the highest system reliability, and achieves the lowest average execution latency and operational cost..

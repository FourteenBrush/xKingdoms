package me.fourteendoggo.xkingdoms.workloads;

/* https://www.spigotmc.org/threads/guide-on-workload-distribution-or-how-to-handle-heavy-splittable-tasks.409003/ */

public interface Workload {

    void compute();
}

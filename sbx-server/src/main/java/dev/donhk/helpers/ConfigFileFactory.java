package dev.donhk.helpers;

import dev.donhk.pojos.MachineMeta;

import java.time.LocalDateTime;
import java.util.List;

public class ConfigFileFactory {

    private List<MachineMeta> metaExtractor;

    public ConfigFileFactory(List<MachineMeta> metaExtractor) {
        this.metaExtractor = metaExtractor;
    }

    public String createMetaInfoFile() {
        StringBuilder sb = new StringBuilder();
        /*
           create a text document in memory with the info, the goal is to produce this
           #
           # Version date:

           #
           # Seeds info
           #

           #
           # {VM_NAME}
           # CPUs: {#} RAM: {#} MB
           # Comments: ...
           #
           {vm_name}c#.name={VM_NAME}
           {vm_name}c#.snapshot={SNAPSHOT}
           {vm_name}c#.vmpath={HOME}
           {vm_name}c#.user={USER}
           {vm_name}c#.pass={PASSWORD}
         */
        sb.append(String.join("\n",
                "#",
                "# Last update " + LocalDateTime.now(),
                "#",
                "",
                "#",
                "# Seeds info",
                "#",
                ""
        ));
        for (MachineMeta machine : metaExtractor) {
            sb.append("#").append("\n");
            sb.append("# ").append(machine.machineName).append("\n");
            sb.append("# ").append("CPUs: ").append(machine.cpuCount).append(" RAM: ").append(machine.memorySize).append(" MB").append("\n");
            // format comments
            sb.append(formatComments(machine.comments)).append("\n");
            sb.append("#").append("\n");
            sb.append(machine.machinePrefix).append(".name=").append(machine.machineName).append("\n");
            sb.append(machine.machinePrefix).append(".snapshot=").append(machine.snapshotName).append("\n");
            sb.append(machine.machinePrefix).append(".vmpath=").append(machine.home).append("\n");
            sb.append(machine.machinePrefix).append(".user=").append(machine.user).append("\n");
            sb.append(machine.machinePrefix).append(".pass=").append(machine.password).append("\n");
        }
        return sb.toString();
    }

    private String formatComments(String raw) {
        String sangria = "# Comments: ";
        StringBuilder sb = new StringBuilder(sangria);
        int MAX_LENGTH = 80;
        int C_LENGTH = sangria.length();
        for (byte b : raw.getBytes()) {
            char c = (char) b;
            sb.append(c);
            C_LENGTH++;
            if (C_LENGTH >= MAX_LENGTH && c == ' ') {
                sb.append("\n").append("# ");
                C_LENGTH = 2;
            }
        }
        return sb.toString();
    }
}

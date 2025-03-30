package dev.donhk.vbox;

import dev.donhk.pojos.MachineMeta;
import org.virtualbox_7_1.IMachine;
import org.virtualbox_7_1.ISnapshot;
import org.virtualbox_7_1.MachineState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.donhk.vbox.Constants.*;

/**
 * The MetaExtractor class is responsible for extracting metadata
 * from powered-off VirtualBox machines that belong to the SBX_GROUP group.
 * <p>
 * It traverses snapshots of those machines, identifies snapshots following the naming convention ("snap..."),
 * and extracts user-defined metadata embedded in snapshot descriptions.
 */
public class MetaExtractor {

    /**
     * Reference to VBoxManager to interact with VirtualBox API
     */
    private final VBoxManager boxManager;

    /**
     * Temporary collection of extracted machine metadata from snapshot traversal
     */
    private final List<MachineMeta> seeds = new ArrayList<>();

    /**
     * Constructs a MetaExtractor with a given VBoxManager.
     *
     * @param boxManager the VBoxManager instance used to fetch machines and snapshots
     */
    public MetaExtractor(VBoxManager boxManager) {
        this.boxManager = boxManager;
    }

    /**
     * Generates a list of MachineMeta objects by analyzing powered-off machines
     * in the SBX_GROUP group, traversing their snapshot trees,
     * and extracting metadata from their descriptions.
     *
     * @return a cleaned, deduplicated, and annotated list of machine metadata
     */
    public List<MachineMeta> genMetaInfo() {
        // Iterate over powered-off machines
        for (IMachine m : boxManager.getMachines(MachineState.PoweredOff)) {
            // Only include machines in the SBX_GROUP group
            if (!m.getGroups().toString().toLowerCase().contains(SBX_GROUP)) {
                continue;
            }

            // Skip machines with no snapshots
            if (m.getSnapshotCount() == 0) {
                continue;
            }

            // Get the root snapshot
            ISnapshot snap = m.getCurrentSnapshot();
            while (snap.getParent() != null) {
                snap = snap.getParent();
            }

            // Recursively traverse the snapshot tree and populate `seeds`
            traverse(snap);
        }

        // Deduplicate and assign unique prefixes (e.g., devbox32c1, devbox32c2)
        Map<String, MachineMeta> metaInfo = new LinkedHashMap<>();
        int id = 1;
        String latestCleanName = "";

        for (MachineMeta meta : seeds) {
            String cleanName = meta.machineName.toLowerCase().replaceAll("[._\\-]", "");

            // Reset ID counter if the machine name changes
            if (!cleanName.equals(latestCleanName)) {
                id = 1;
            }

            String prefix = cleanName + "c" + id;

            // Ensure unique prefix per entry
            while (metaInfo.containsKey(prefix)) {
                prefix = cleanName + "c" + (++id);
            }

            metaInfo.put(prefix, meta);
            latestCleanName = cleanName;
            id++;
        }

        // Construct final list with updated prefix-based names
        List<MachineMeta> finalListOfSeeds = new ArrayList<>();
        for (Map.Entry<String, MachineMeta> x : metaInfo.entrySet()) {
            finalListOfSeeds.add(new MachineMeta(
                    x.getKey(),
                    x.getValue().machineName,
                    x.getValue().snapshotName,
                    x.getValue().cpuCount,
                    x.getValue().memorySize,
                    x.getValue().user,
                    x.getValue().password,
                    x.getValue().home,
                    x.getValue().comments
            ));
        }

        return finalListOfSeeds;
    }

    /**
     * Recursively traverses a snapshot tree starting at the given node.
     * Adds snapshot information to `seeds` if the snapshot name starts with SBX_SNAP.
     *
     * @param node the root snapshot node
     */
    private void traverse(ISnapshot node) {
        IMachine m = node.getMachine();

        // We are only interested in snapshots that follow the naming convention "snap*"
        if (node.getName().startsWith(SBX_SNAP)) {
            seeds.add(new MachineMeta(
                    "", // prefix to be generated later
                    m.getName(),
                    node.getName(),
                    m.getCPUCount(),
                    m.getMemorySize(),
                    getUser(node.getDescription()),
                    getPassword(node.getDescription()),
                    getHome(node.getDescription()),
                    getComments(node.getDescription())
            ));
        }

        // Recursively process child snapshots
        for (ISnapshot x : node.getChildren()) {
            traverse(x);
        }
    }

    /**
     * Extracts the user from the snapshot description.
     * Expects the format: {@code user: username}
     *
     * @param source snapshot description
     * @return extracted username or NA
     */
    private String getUser(String source) {
        Pattern pName = Pattern.compile("\\s*user\\s*:\\s*([a-zA-Z0-9_.]+)\\s*$", Pattern.MULTILINE);
        Matcher m = pName.matcher(source);
        return m.find() ? m.group(1).trim() : NA;
    }

    /**
     * Extracts the password from the snapshot description.
     * Expects the format: {@code pass: password}
     *
     * @param source snapshot description
     * @return extracted password or NA
     */
    private String getPassword(String source) {
        Pattern pName = Pattern.compile("\\s*pass\\s*:\\s*(.*)\\s*$", Pattern.MULTILINE);
        Matcher m = pName.matcher(source);
        return m.find() ? m.group(1).trim() : NA;
    }

    /**
     * Extracts the home path from the snapshot description.
     * Escapes backslashes for Windows compatibility.
     * Expects the format: {@code home: /path/to/home}
     *
     * @param source snapshot description
     * @return extracted home path or NA
     */
    private String getHome(String source) {
        Pattern pName = Pattern.compile("^\\s*home\\s*:\\s*(.*)\\s*$", Pattern.MULTILINE);
        Matcher m = pName.matcher(source);
        if (m.find()) {
            return m.group(1).replaceAll("\\\\", "\\\\\\\\").trim();
        }
        return NA;
    }

    /**
     * Extracts multiline comments from the snapshot description.
     * Expects lines starting with or following {@code comments:}
     *
     * @param source snapshot description
     * @return concatenated comments or empty string
     */
    private String getComments(String source) {
        StringBuilder sb = new StringBuilder();
        String[] lines = source.split("\n");
        String key = "comments:";
        boolean capture = false;

        for (String line : lines) {
            if (line.contains(key)) {
                line = line.substring(key.length()).trim();
                capture = true;
            }
            if (capture) {
                sb.append(line).append(" ");
            }
        }

        return sb.toString().trim();
    }
}

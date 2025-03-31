package dev.donhk.actor.impl;

import dev.donhk.actor.VBoxMessage;
import dev.donhk.pojos.MachineMeta;
import dev.donhk.vbox.MetaExtractor;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

import java.util.List;

public class ListMachines {

    private final VBoxManager boxManager;

    public ListMachines(VBoxManager boxManager) {
        this.boxManager = boxManager;
    }

    public VBoxMessage.ListMachinesResponse dispatch() {
        Logger.info("ListMachinesRequest");
        final MetaExtractor metaExtractor = new MetaExtractor(this.boxManager);
        final List<MachineMeta> machines = metaExtractor.genMetaInfo();
        return new VBoxMessage.ListMachinesResponse(machines);
    }
}

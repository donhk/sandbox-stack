package dev.donhk.web.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import dev.donhk.database.DBManager;
import dev.donhk.sbx.ClientConnection;
import dev.donhk.web.controlls.*;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebContextsHandler {

    private final Logger logger = LoggerFactory.getLogger(WebContextsHandler.class);
    private final Map<String, ServletHolder> contexts = new HashMap<>();

    public WebContextsHandler(DBManager dbManager, List<ClientConnection> clientConnections) {
        contexts.put("/", new ServletHolder(new ActiveMachines(dbManager)));
        contexts.put("/meta", new ServletHolder(new VMsMeta(dbManager)));
        contexts.put("/config", new ServletHolder(new ConfigFile(dbManager)));
        contexts.put("/stats", new ServletHolder(new UsageStats(dbManager)));
        contexts.put("/reload", new ServletHolder(new ReloadVMsMeta()));
        contexts.put("/sbx", new ServletHolder(new SbxControl(dbManager, clientConnections)));
    }

    public Map<String, ServletHolder> getContexts() {
        return contexts;
    }

}

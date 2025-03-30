package dev.donhk.web.controlls;

import dev.donhk.database.VMDataAccessService;
import dev.donhk.helpers.Utils;
import dev.donhk.pojos.DigestRow;
import dev.donhk.web.core.Controller;

import java.sql.SQLException;

public class ConfigFile extends Controller {

    private final VMDataAccessService VMDataAccessService;

    public ConfigFile(VMDataAccessService VMDataAccessService) {
        super("web/views/Layout.html");
        this.VMDataAccessService = VMDataAccessService;
    }

    @Override
    public void addVariables() {
        variables.put("pure-min", "<style>" + loadResource("web/css/layouts/pure-min.css") + "</style>");
        variables.put("side-menu", "<style>" + loadResource("web/css/layouts/side-menu.css") + "</style>");
        variables.put("ui-js", "<script>" + loadResource("web/js/ui.js") + "</script>");
        try {
            DigestRow row = VMDataAccessService.getDigestRow();
            variables.put("config-file", "<pre>" + Utils.base64Decode(row.content) + "</pre>");
            variables.put("created", row.created);
            variables.put("digest", row.digest);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        variables.put("main-content", loadResource("web/views/ConfigFilePage.html"));
    }
}

package dev.donhk.pojos;

public enum Tasks {
    UPDATE("update", "/sbx?task=update&vm=vm_name"),
    VMS_RUNNING("vms_running", "/sbx?task=vms_running"),
    KILL("kill", "/sbx?task=kill&vm=vm_name"),
    GODKILL("godkill", "/sbx?task=godkill"),
    VMS_INFO("vms_info", "/sbx?task=vms_info"),
    HELP("help", "/sbx?task=help"),
    ADMIN_MSG("admin_msg", "/sbx?task=admin_msg&target=all_vms&message=example%20message"),
    NULL("null", "");

    private final String val;
    private final String help;

    Tasks(String val, String help) {
        this.val = val;
        this.help = help;
    }

    public String getVal() {
        return val;
    }

    public String getHelp() {
        return help;
    }
}

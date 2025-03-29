package dev.donhk.helpers;

public class Constants {
    public static final String INVALID_ARG = "INVALID_ARG";
    public static final String INVALID_COMMAND = "INVALID_COMMAND";
    public static final String MY_NAME = "MY_NAME"; //MY_NAME|name
    public static final String GET_WORK_BASE = "GET_WORK_BASE";//GET_WORK_BASE
    public static final String CREATE_WORK_DIR = "CREATE_WORK_DIR";//CREATE_WORK_DIR|name
    public static final String CLONE_MACHINE = "CLONE_MACHINE";//CLONE_MACHINE|seed|snapshot
    public static final String GET_VM_IPV4 = "GET_VM_IPV4";//GET_VM_IPV4
    public static final String CREATE_PORT_FORWARD_RULE = "CREATE_PORT_FORWARD_RULE";//CREATE_PORT_FORWARD_RULE|hostPort|vmPort|ruleName
    public static final String UPDATE_FORWARD_RULE = "UPDATE_FORWARD_RULE"; //UPDATE_FORWARD_RULE|hostPort|vmPort|oldRuleName|newRuleName
    public static final String CREATE_SSH_PORT_FORWARD_RULE = "CREATE_SSH_PORT_FORWARD_RULE";//UPDATE_SSH_FORWARD_RULE
    public static final String UPDATE_SSH_FORWARD_RULE = "UPDATE_SSH_FORWARD_RULE";//UPDATE_SSH_FORWARD_RULE
    public static final String CONFIRM_MACHINE_UP = "CONFIRM_MACHINE_UP";//CONFIRM_MACHINE_UP
    public static final String GET_FREE_PORT = "GET_FREE_PORT";//GET_FREE_PORT
    public static final String GET_SSH_PORT = "GET_SSH_PORT";//GET_SSH_PORT
    public static final String START_UP_VM = "START_UP_VM";//START_UP_VM
    public static final String OPERATION_SUCCESSFUL = "OPERATION_SUCCESSFUL";
    public static final String RUNTIME_ERR = "RUNTIME_ERR";
    public static final String DESTROY_MACHINE = "DESTROY_MACHINE";
    public static final String SHARED_DIR_NAME = "shared";
    public static final String ANONYMOUS_CLIENT_NOT_ALLOWED = "CONFIG_ERR_NO_NAME_PROVIDED";
    public static final String POLLING_PORT = "POLLING_PORT";
    public static final String CLIENT_NAME_ALREADY_DEFINED = "CONFIG_ERR_CLIENT_NAME_ALREADY_DEFINED";
    public static final String POLLING_RESPONSE = "POLLING_RESPONSE";
    public static final String ARE_YOU_ALIVE = "ARE_YOU_ALIVE";
    public static final String TIME_EXEC_EXCEED = "TIME_EXEC_EXCEED";//TIME_EXEC_EXCEED|hours
    public static final String OVERWRITE_EXEC_TIME = "OVERWRITE_EXEC_TIME";//OVERWRITE_EXEC_TIME|hours
    public static final String CREATE_NAT_NETWORK = "CREATE_NAT_NETWORK";//CREATE_NAT_NETWORK
    public static final String CREATE_SHARED_STORAGE_UNITS = "CREATE_SHARED_STORAGE_UNITS";//CREATE_SHARED_STORAGE_UNITS|size|#
    public static final String ATTACH_SHARED_STORAGE_UNITS = "ATTACH_SHARED_STORAGE_UNITS";//ATTACH_SHARED_STORAGE_UNITS|[path_1, path_2[, path_n]]

    //machine states
    public static final String PREREGISTER = "PREREGISTER";//first client contact
    public static final String RESPONSE_TIMEOUT = "RESPONSE_TIMEOUT";//client stop responding polling
    public static final String CLEAN_END = "CLEAN_END";//client requested to clean vm
    public static final String OPERATION_EXCEPTION = "OPERATION_EXCEPTION";//There was an unexpected error in the client side
    public static final String ADMIN_KILL = "ADMIN_KILL";//There was an unexpected error in the client side
    public static boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
}

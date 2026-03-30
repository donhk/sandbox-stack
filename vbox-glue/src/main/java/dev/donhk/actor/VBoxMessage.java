package dev.donhk.actor;

import dev.donhk.pojos.ActiveMachineRow;
import dev.donhk.pojos.MachineMeta;
import dev.donhk.vbox.LaunchMode;

import java.util.List;

public final class VBoxMessage {
    public record PingRequest(String name, int age) {
    }

    public record PingResponse(String name, int age) {
    }

    public record PingRequest2(String name, int age) {
    }

    public record PingResponse2(String name, int age) {
    }

    public record ListMachinesRequest() {
    }

    public record ListMachinesResponse(List<MachineMeta> machineMetas) {
    }

    public record DelDanglingNetsRequest(List<ActiveMachineRow> activeMachineRows) {
    }

    public record DelDanglingNetsResponse(List<String> unusedNetworks) {
    }

    public record GetVBoxVersionRequest() {
    }

    public record GetVBoxVersionResponse(String version) {
    }

    public record CloneMachineRequest(String seedName, String snapshot, String machineName, String natNetwork) {
    }

    public record CloneMachineResponse(boolean success) {
    }

    public record LaunchMachineRequest(String machineName, LaunchMode mode) {
    }

    public record LaunchMachineResponse(boolean success) {
    }

    public record AddSharedDirectoryRequest(String machineName, String dirName, String hostPath) {
    }

    public record AddSharedDirectoryResponse(boolean success) {
    }

    public record CreateSharedStorageRequest(String machineName, int numDisks, int size) {
    }

    public record CreateSharedStorageResponse(List<String> diskPaths) {
    }

    public record AddSharedStorageToMachineRequest(String machineName, List<String> sharedStorageDisks) {
    }

    public record AddSharedStorageToMachineResponse(boolean success) {
    }

    public record AddNATNetworkPortForwardRuleRequest(String networkName, int hostPort, int guestPort, String machineName, String ruleName) {
    }

    public record AddNATNetworkPortForwardRuleResponse(boolean success) {
    }

    public record AddNATPortForwardRuleRequest(int hostPort, int guestPort, String machineName, String ruleName) {
    }

    public record AddNATPortForwardRuleResponse(boolean success) {
    }

    public record RmNATPortForwardRuleRequest(String machineName, String ruleName) {
    }

    public record RmNATPortForwardRuleResponse() {
    }

    public record RmNATNetworkPortForwardRuleRequest(String networkName, String ruleName) {
    }

    public record RmNATNetworkPortForwardRuleResponse() {
    }

    public record GetPortForwardRulesRequest(String networkName) {
    }

    public record GetPortForwardRulesResponse(List<String> rules) {
    }

    public record GetMachineIPv4Request(String machineName) {
    }

    public record GetMachineIPv4Response(String ipv4) {
    }

    public record CleanUpVMRequest(String machineName) {
    }

    public record CleanUpVMResponse() {
    }

    public record MachineExistsRequest(String machineName) {
    }

    public record MachineExistsResponse(boolean exists) {
    }

    public record CreateNatNetworkRequest(String networkName) {
    }

    public record CreateNatNetworkResponse(boolean success) {
    }

    public record RemoveNatNetworkRequest(String networkName) {
    }

    public record RemoveNatNetworkResponse(boolean success) {
    }
}

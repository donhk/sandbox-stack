package dev.donhk.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.donhk.exception.SandboxException;
import dev.donhk.rest.network.CreateNatNetworkRequest;
import dev.donhk.rest.network.CreateNatNetworkResponse;
import dev.donhk.rest.network.CreatePortForwardRuleRequest;
import dev.donhk.rest.network.CreatePortForwardRuleResponse;
import dev.donhk.rest.network.UpdatePortForwardRuleRequest;
import dev.donhk.rest.network.UpdatePortForwardRuleResponse;
import dev.donhk.rest.operations.vm.CreateMachineRequest;
import dev.donhk.rest.operations.vm.CreateMachineResponse;
import dev.donhk.rest.operations.vm.DeleteMachineResponse;
import dev.donhk.rest.operations.vm.StartMachineRequest;
import dev.donhk.rest.operations.vm.StartMachineResponse;
import dev.donhk.rest.operations.vm.UpdateMachineRequest;
import dev.donhk.rest.operations.vm.UpdateMachineResponse;
import dev.donhk.rest.storage.CreateStorageUnitsRequest;
import dev.donhk.rest.storage.CreateStorageUnitsResponse;
import dev.donhk.rest.types.Machine;
import dev.donhk.rest.types.VMSnapshot;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class SandboxClient {

    private final String baseUrl;
    private final String host;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public SandboxClient(String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.host = URI.create(baseUrl).getHost();
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
    }

    public String getHost() {
        return host;
    }

    // ── Health ────────────────────────────────────────────────────────────────

    public void ping() {
        sendRaw(HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/ping"))
                .GET()
                .build(), "/api/ping");
    }

    // ── Seeds / machines list ─────────────────────────────────────────────────

    public List<VMSnapshot> listSeeds() {
        return getList("/api/vm-seeds/list", new TypeReference<>() {});
    }

    public void scanMachines() {
        post("/api/machines/scan", null, Void.class);
    }

    public List<Machine> listMachines() {
        return getList("/api/machines/list", new TypeReference<>() {});
    }

    // ── VM lifecycle ──────────────────────────────────────────────────────────

    public CreateMachineResponse createMachine(CreateMachineRequest request) {
        return post("/api/machine", request, CreateMachineResponse.class);
    }

    public Machine getMachine(String uuid) {
        return get("/api/machine/" + uuid, Machine.class);
    }

    public StartMachineResponse startMachine(String uuid) {
        return post("/api/machine/start", new StartMachineRequest(uuid), StartMachineResponse.class);
    }

    public UpdateMachineResponse updateMachine(UpdateMachineRequest request) {
        return put("/api/machine", request, UpdateMachineResponse.class);
    }

    public DeleteMachineResponse deleteMachine(String uuid) {
        return delete("/api/machine/" + uuid, DeleteMachineResponse.class);
    }

    // ── Network ───────────────────────────────────────────────────────────────

    public CreateNatNetworkResponse createNatNetwork(String uuid) {
        return post("/api/nat-network", new CreateNatNetworkRequest(uuid), CreateNatNetworkResponse.class);
    }

    public CreatePortForwardRuleResponse createPortForwardRule(String uuid, int vmPort, String portName) {
        return post("/api/port-forward-rule",
                new CreatePortForwardRuleRequest(uuid, vmPort, portName),
                CreatePortForwardRuleResponse.class);
    }

    public UpdatePortForwardRuleResponse updatePortForwardRule(String uuid, int vmPort, String newRuleName) {
        return put("/api/port-forward-rule",
                new UpdatePortForwardRuleRequest(uuid, vmPort, newRuleName),
                UpdatePortForwardRuleResponse.class);
    }

    // ── Storage ───────────────────────────────────────────────────────────────

    public CreateStorageUnitsResponse createStorageUnits(String uuid, long sizeBytes, int numDisks) {
        return post("/api/storage-unit",
                new CreateStorageUnitsRequest(uuid, sizeBytes, numDisks),
                CreateStorageUnitsResponse.class);
    }

    // ── HTTP primitives ───────────────────────────────────────────────────────

    private <T> T get(String path, Class<T> type) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();
        return send(req, path, type);
    }

    private <T> List<T> getList(String path, TypeReference<List<T>> type) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();
        return sendList(req, path, type);
    }

    private <T> T post(String path, Object body, Class<T> type) {
        String json = serialize(body != null ? body : "");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        if (type == Void.class) {
            sendRaw(req, path);
            return null;
        }
        return send(req, path, type);
    }

    private <T> T put(String path, Object body, Class<T> type) {
        String json = serialize(body);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return send(req, path, type);
    }

    private <T> T delete(String path, Class<T> type) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .DELETE()
                .build();
        return send(req, path, type);
    }

    private <T> T send(HttpRequest req, String path, Class<T> type) {
        HttpResponse<String> resp = sendRaw(req, path);
        try {
            return mapper.readValue(resp.body(), type);
        } catch (Exception e) {
            throw new SandboxException("Failed to deserialize response from " + path + ": " + resp.body(), e);
        }
    }

    private <T> List<T> sendList(HttpRequest req, String path, TypeReference<List<T>> type) {
        HttpResponse<String> resp = sendRaw(req, path);
        try {
            return mapper.readValue(resp.body(), type);
        } catch (Exception e) {
            throw new SandboxException("Failed to deserialize list response from " + path + ": " + resp.body(), e);
        }
    }

    private HttpResponse<String> sendRaw(HttpRequest req, String path) {
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            Logger.debug("{} {} -> {}", req.method(), path, resp.statusCode());
            if (resp.statusCode() >= 400) {
                throw new SandboxException(req.method() + " " + path +
                        " failed [" + resp.statusCode() + "]: " + resp.body());
            }
            return resp;
        } catch (SandboxException e) {
            throw e;
        } catch (IOException e) {
            throw new SandboxException("Network error on " + req.method() + " " + path, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SandboxException("Interrupted on " + req.method() + " " + path, e);
        }
    }

    private String serialize(Object body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new SandboxException("Failed to serialize request body", e);
        }
    }
}

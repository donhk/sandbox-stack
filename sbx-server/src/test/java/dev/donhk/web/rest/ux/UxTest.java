package dev.donhk.web.rest.ux;

import dev.donhk.database.DBService;
import dev.donhk.pojos.MachineRow;
import dev.donhk.rest.types.Machine;
import dev.donhk.rest.types.MachineState;
import dev.donhk.rest.types.Network;
import dev.donhk.rest.types.NetworkType;
import io.javalin.http.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class UxTest {

    @Test
    public void testListMachinesEndpoint() throws Exception {
        // Mock or fake DBService
        DBService serviceMock = mock(DBService.class);
        Context ctx = mock(Context.class);
        ListMachines machines = new ListMachines(serviceMock);

        // Mock DB results
        MachineRow machineRow = new MachineRow(
                "mch-018",
                "Machine-R",
                "seed-18",
                "snap-018",
                "network3",
                NetworkType.NAT,
                "local1.localhost:3999",
                "machine-r",
                "hostnmae",
                MachineState.RUNNING,
                new Timestamp(1),
                new Timestamp(2),
                true
        );
        when(serviceMock.listAllVirtualMachines()).thenReturn(List.of(machineRow));


        // Mock DbUtils.machineRow2Machine
        mockStaticDevDonhkDbUtils(machineRow, serviceMock);

        // Act
        machines.handle(ctx);

        // Assert
        verify(ctx).json(any(List.class));
    }

    private void mockStaticDevDonhkDbUtils(MachineRow machineRow, DBService dbService) {
        Mockito.mockStatic(dev.donhk.database.DbUtils.class).when(() ->
                dev.donhk.database.DbUtils.machineRow2Machine(eq(dbService), eq(machineRow))
        ).thenReturn(
                new Machine(
                        "mch-018",
                        "Machine-R",
                        "seed-18",
                        "snap-018",
                        new Network(NetworkType.NAT, "network3"),
                        Optional.of("192.168.10.18"),
                        "local1.localhost:3999",
                        List.of(),
                        "machine-r",
                        MachineState.RUNNING,
                        new Timestamp(1),
                        new Timestamp(2),
                        List.of(),
                        false
                )
        );
    }
}

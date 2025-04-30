package dev.donhk.web.rest.vm;

import dev.donhk.database.DBService;
import dev.donhk.database.DbUtils;
import dev.donhk.pojos.MachineRow;
import dev.donhk.rest.types.Machine;
import dev.donhk.rest.types.MachineState;
import dev.donhk.rest.types.Network;
import dev.donhk.rest.types.NetworkType;
import io.javalin.http.Context;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GetVmTest {
    @Test
    public void testGetVm() throws Exception {
        MachineRow machineRow = new MachineRow(
                "mch-018",
                "Machine-R",
                "seed-18",
                "snap-018",
                "network3",
                NetworkType.NAT,
                "local1.localhost:3999",
                "machine-r",
                "hostname",
                MachineState.RUNNING,
                new Timestamp(1),
                new Timestamp(2),
                true
        );

        Machine machine = new Machine(
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
        );

        DBService serviceMock = mock(DBService.class);
        Context ctx = mock(Context.class);

        when(serviceMock.findMachine("mch-018")).thenReturn(Optional.of(machineRow));
        when(ctx.pathParam("uuid")).thenReturn("mch-018");

        try (MockedStatic<DbUtils> mockedDbUtils = Mockito.mockStatic(DbUtils.class)) {
            mockedDbUtils.when(() ->
                    DbUtils.machineRow2Machine(eq(serviceMock), eq(machineRow))
            ).thenReturn(machine);

            GetVm getVm = new GetVm(serviceMock);
            getVm.handle(ctx);

            verify(ctx).json(machine);
        }
    }
}

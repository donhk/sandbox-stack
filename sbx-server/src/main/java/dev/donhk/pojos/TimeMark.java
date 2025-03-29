package dev.donhk.pojos;

import dev.donhk.database.DBManager;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeMark {
    private LocalDateTime time = LocalDateTime.now();
    private final LocalDateTime startTime = LocalDateTime.now();
    private final DBManager dbManager;
    private final String name;

    public TimeMark(DBManager dbManager, String name) {
        this.dbManager = dbManager;
        this.name = name;
    }

    public long secondsSinceLastUpdate() {
        refreshTime();
        //if the last contact with the client was made more than 5 sec ago,
        //it is dead, kill and clean
        return time.until(LocalDateTime.now(), ChronoUnit.SECONDS);
    }

    public long minutesSinceStart() {
        //calculate difference in minutes
        //if this test takes more than DEFAULT_EXEC_TIME_LIMIT, it will be aborted
        //this will prevent hanging jobs
        return startTime.until(LocalDateTime.now(), ChronoUnit.MINUTES);
    }

    private void refreshTime() {
        final Timestamp ts = dbManager.getMachinePoll(name);
        if (ts == null) {
            //this should never happen
            time = LocalDateTime.MIN;
        } else {
            time = ts.toLocalDateTime();
        }
    }
}

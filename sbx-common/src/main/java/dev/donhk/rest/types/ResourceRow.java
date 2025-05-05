package dev.donhk.rest.types;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceRow(Timestamp timestamp, float usage) {
    public static ResourceRow fromResultSet(ResultSet rs) throws SQLException {
        return new ResourceRow(
                rs.getTimestamp("dt_min"),
                rs.getFloat("avg_usage")
        );
    }
}

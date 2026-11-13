package se.yrgo.libraryapp.dao;

import static java.time.Period.ofDays;
import static org.assertj.core.api.Assertions.*;

import com.radcortez.flyway.test.annotation.H2;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.exception.CredentialsException;
import se.yrgo.libraryapp.entities.UserId;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.UUID;

@Tag("integration")
@H2
public class SessionDaoIntegrationTest {
    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        // this way we do not need to create a new datasource every time
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        SessionDaoIntegrationTest.ds = ds;
    }

    @Test
    void createAndValidateSession() {
        final UserId userId = UserId.of(1);

        SessionDao sessionDao = new SessionDao(ds);

        UUID sessionId = sessionDao.create(userId);
        UserId result = sessionDao.validate(sessionId);

        assertThat(result).isEqualTo(userId);
    }

    @Test
    void deleteSession() {
        final UserId userId = UserId.of(1);

        SessionDao sessionDao = new SessionDao(ds);

        UUID sessionId = sessionDao.create(userId);
        sessionDao.delete(sessionId);

        assertThatThrownBy(() -> sessionDao.validate(sessionId))
                .isInstanceOf(CredentialsException.class);
    }

    @Test
    void validateExpiredSession() throws SQLException {
        final UserId userId = UserId.of(1);

        SessionDao sessionDao = new SessionDao(ds);
        UUID sessionId = sessionDao.create(userId);
        int daysSinceCreated = 8;

        insertExpiredSession(sessionId, daysSinceCreated);

        assertThatThrownBy(() -> sessionDao.validate(sessionId))
                .isInstanceOf(CredentialsException.class);
    }

    @Test
    void validateNonExistingSession() {
        UUID sessionId = UUID.randomUUID();

        SessionDao sessionDao = new SessionDao(ds);

        assertThatThrownBy(() -> sessionDao.validate(sessionId))
                .isInstanceOf(CredentialsException.class);
    }

    // HELPER METHODS

    private void insertExpiredSession(UUID sessionId, int daysSinceCreated) throws SQLException {
        String sql = "INSERT INTO session (id, user_id, created) VALUES (?, ?, ?)";

        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionId.toString());
            stmt.setInt(2, 1);
            stmt.setTimestamp(3, Timestamp.from(Instant.now().minus(ofDays(daysSinceCreated))));

            stmt.executeUpdate();
        }
    }
}

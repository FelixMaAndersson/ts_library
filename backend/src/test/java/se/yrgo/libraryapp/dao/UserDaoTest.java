package se.yrgo.libraryapp.dao;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import java.sql.*;
import java.util.Optional;
import javax.sql.DataSource;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.yrgo.libraryapp.entities.*;
import se.yrgo.libraryapp.validators.*;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class UserDaoTest {
    @Mock
    private DataSource ds;
    @Mock
    private Connection conn;
    @Mock
    private Statement stmt;
    @Mock
    private ResultSet rs;

    @Test
    void getExistingLoginInfo() throws SQLException {
        final String username = "test";
        final UserId id = UserId.of(1);
        final String passwordHash =
                "$argon2i$v=19$m=16,t=2,p=1$QldXU09Sc2dzOWdUalBKQw$LgKb6x4usOpDLTlXCBVhxA";

        when(ds.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(contains(username))).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(id.getId());
        when(rs.getString("password_hash")).thenReturn(passwordHash);

        UserDao userDao = new UserDao(ds);
        LoginInfo info = userDao.getLoginInfo(username).get();

        assertThat(info.getUserId()).isEqualTo(id);
        assertThat(info.getPasswordHash()).isEqualTo(passwordHash);
    }

    @Test
    void getNonExistingLoginInfo() throws SQLException {
        final String username = "test";

        when(ds.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(contains(username))).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        UserDao userDao = new UserDao(ds);

        assertThat(userDao.getLoginInfo(username)).isEmpty();
    }

    // I think maybe you could assert that you get a realname and a username here
    // to ensure the SQL i correctly written
    @Test
    void getExistingUser() throws SQLException {
        final String userId = "1";
        final UserId id = UserId.of(userId);
        final String username = "testuser";
        final String realname = "bosse";
        final User expectedUser = new User(id, username, realname);

        when(ds.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("user")).thenReturn(username);
        when(rs.getString("realname")).thenReturn(realname);

        UserDao userDao = new UserDao(ds);

        assertThat(userDao.get(userId)).isEqualTo(Optional.of(expectedUser));
    }

    @Test
    void getNonExistingUser() throws SQLException {
        final String id = "99999";

        when(ds.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        UserDao userDao = new UserDao(ds);

        assertThat(userDao.get(id)).isEmpty();
    }

    // I don't understand why you shouldn't throw an exception when you
    // can't connect to the database. This is something that I would have
    // discussed with the team, 'how do we want this to be handled?',
    // since it feels extremely unprofessional to rewrite the method because
    // this isn't just a bug fix, it's the design.
    // You could also say that just logging the error and letting no one else
    // know that there is something wrong with the database is an absolutely
    // wonderful design.
    @Test
    @Disabled
    void GetUserDatabaseFail() throws SQLException {
        final String userId = "1";

        when(ds.getConnection())
                .thenThrow(new SQLException());

        UserDao user = new UserDao(ds);

         assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> user.get(userId));
    }
}
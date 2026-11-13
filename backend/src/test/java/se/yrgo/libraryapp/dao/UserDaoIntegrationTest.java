package se.yrgo.libraryapp.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.*;
import java.util.Optional;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.radcortez.flyway.test.annotation.H2;
import se.yrgo.libraryapp.entities.LoginInfo;
import se.yrgo.libraryapp.entities.User;
import se.yrgo.libraryapp.entities.UserId;

@Tag("integration")
@H2
public class UserDaoIntegrationTest {
    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        // this way we do not need to create a new datasource every time
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        UserDaoIntegrationTest.ds = ds;
    }

    @Test
    void getUserById() {
        // this data comes from the test migration files
        final String username = "test";
        final String realname = "unknown";
        final UserId userId = UserId.of(1);

        UserDao userDao = new UserDao(ds);
        Optional<User> maybeUser = userDao.get(Integer.toString(userId.getId()));

        assertThat(maybeUser).isPresent();
        assertThat(maybeUser.get().getName()).isEqualTo(username);
        assertThat(maybeUser.get().getRealname()).isEqualTo(realname);
        assertThat(maybeUser.get().getId()).isEqualTo(userId);
    }

    @Test
    void getNonExistingUserById() {
        final String userId = "9999";

        UserDao userDao = new UserDao(ds);

        Optional<User> nonExistingUser = userDao.get(userId);

        assertThat(nonExistingUser).isEmpty();
    }

    @Test
    void getLoginInfo() {
        final String username = "test";
        final UserId userId = UserId.of(1);
        final String expectedHash = "$argon2i$v=19$m=16,t=2,p=1$MTIzNDU2Nzg5MDEyMzQ1NjA$LmFqTZeUWwqsnbZCS2E8XQ";

        UserDao userDao = new UserDao(ds);

        Optional<LoginInfo> loginInfo = userDao.getLoginInfo(username);

        assertThat(loginInfo).isPresent();

        LoginInfo info = loginInfo.get();

        assertThat(info.getUserId()).isEqualTo(userId);
        assertThat(info.getPasswordHash()).isEqualTo(expectedHash);
    }

    @Test
    void getNonExistingLoginInfo() {
        final String username = "unknownuser";

        UserDao userDao = new UserDao(ds);

        Optional<LoginInfo> loginInfo = userDao.getLoginInfo(username);

        assertThat(loginInfo).isEmpty();
    }

    @Test
    void avaliableUserName() {
        final String username = "newuser";

        UserDao userDao = new UserDao(ds);

        boolean result = userDao.isNameAvailable(username);

        assertThat(result).isTrue();
    }

    @Test
    void unavailableUserName() {
        final String username = "test";

        UserDao userDao = new UserDao(ds);

        boolean result = userDao.isNameAvailable(username);

        assertThat(result).isFalse();
    }

    @Test
    void registerSucess() {
        final String username = "newuser";
        final String realname = "new user";
        final String passwordHash = "hashedPassword";

        UserDao userDao = new UserDao(ds);

        boolean result = userDao.register(username,realname, passwordHash);

        assertThat(result).isTrue();

        Optional<LoginInfo> loginInfo = userDao.getLoginInfo(username);

        assertThat(loginInfo).isPresent();
        assertThat(loginInfo.get().getPasswordHash()).isEqualTo(passwordHash);

        Optional<User> savedUser = userDao.get(loginInfo.get().getUserId().toString());

        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getName()).isEqualTo(username);
        assertThat(savedUser.get().getRealname()).isEqualTo(realname);
    }

    @Test
    void registerDuplicateUsername() {
        final String username = "test";
        final String realname = "new user";
        final String passwordHash = "hashedPassword";

        UserDao userDao = new UserDao(ds);

        boolean result = userDao.register(username,realname, passwordHash);

        assertThat(result).isFalse();
    }

    @Test
    void registerUserIanOToole() {
        final String username = "ianotoole";
        final String realname = "Ian O'Toole";
        final String passwordHash = "hashedPassword";

        UserDao userDao = new UserDao(ds);

        userDao.register(username,realname, passwordHash);

        LoginInfo loginInfo = userDao.getLoginInfo(username).orElseThrow();

        User savedUser = userDao.get(
                loginInfo.getUserId().toString()
        ).orElseThrow();

        assertThat(savedUser.getRealname()).isEqualTo(realname);
    }

    @Test
    void registerAssignsUserRole() throws SQLException {
        final String username = "rolechecker";
        final String realname = "role checker";
        final String passwordHash = "hashedPassword";

        UserDao userDao = new UserDao(ds);

        userDao.register(username, realname, passwordHash);

        boolean result = hasRole(username, "user");

        assertThat(result).isTrue();
    }


    // is this a preferred behaviour?
    @Test
    void registerRollbackWhenRoleAssignmentFails() throws SQLException {
        final String username = "norole";
        final String realname = "no role";
        final String passwordHash = "hashedPassword";

        removeDefaultUserRole();

        UserDao userDao = new UserDao(ds);

        boolean result = userDao.register(username,realname,passwordHash);
        Optional<LoginInfo> savedUser = userDao.getLoginInfo(username);

        assertThat(result).isFalse();
        assertThat(savedUser).isEmpty();
    }

    // HELPER METHODS
    // (to make the test prettier and for future tests.)

    private boolean hasRole(String username, String role) throws SQLException {
        String sql = "SELECT 1 FROM user_role ur JOIN user u ON ur.user_id = u.id JOIN role r ON ur.role_id = r.id WHERE u.user = ? AND r.role = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, role);

            try (ResultSet rs = stmt.executeQuery()){
                return rs.next();
            }
        }
    }

    private void removeDefaultUserRole() throws SQLException {

        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM user_role WHERE role_id = 2");
            stmt.executeUpdate("DELETE FROM role WHERE id = 2");
        }
    }
}


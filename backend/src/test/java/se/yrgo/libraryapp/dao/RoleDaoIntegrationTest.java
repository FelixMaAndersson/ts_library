package se.yrgo.libraryapp.dao;

import com.radcortez.flyway.test.annotation.H2;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import se.yrgo.libraryapp.entities.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@H2
public class RoleDaoIntegrationTest {

    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        // this way we do not need to create a new datasource every time
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        RoleDaoIntegrationTest.ds = ds;
    }

    @Test
    void getSingleRole() {
        final UserId userId = UserId.of(1);

        RoleDao roleDao = new RoleDao(ds);

        List<Role> result = roleDao.get(userId);

        assertThat(result).containsExactly(Role.ADMIN);
    }

    @Test
    void getMultipleRoles() {
        final UserId userId = UserId.of(2);

        RoleDao roleDao = new RoleDao(ds);

        List<Role> result = roleDao.get(userId);

        assertThat(result).containsExactly(Role.ADMIN, Role.USER);
    }

    @Test
    void getNonExistingRole() {
        final UserId userId = UserId.of(9999);

        RoleDao roleDao = new RoleDao(ds);

        List<Role> result = roleDao.get(userId);

        assertThat(result).isEmpty();
    }
}

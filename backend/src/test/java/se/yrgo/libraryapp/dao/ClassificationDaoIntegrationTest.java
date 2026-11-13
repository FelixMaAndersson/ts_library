package se.yrgo.libraryapp.dao;

import com.radcortez.flyway.test.annotation.H2;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

import se.yrgo.libraryapp.entities.DdsClassification;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@H2
public class ClassificationDaoIntegrationTest {

    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        // this way we do not need to create a new datasource every time
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        ClassificationDaoIntegrationTest.ds = ds;
    }

    @Test
    void getAvailableClassification() {
        ClassificationDao classificationDao = new ClassificationDao(ds);

        List<DdsClassification> result = classificationDao.getAvailable();

        assertThat(result)
                .extracting(DdsClassification::getCode)
                .containsExactly(0,5,306,510,641,800,898);

    }

    @Test
    void getAvailableWhenNoBookEditionExists() throws SQLException {
        removeAllBookEditions();

        ClassificationDao classificationDao = new ClassificationDao(ds);

        List<DdsClassification> result = classificationDao.getAvailable();

        assertThat(result).isEmpty();
    }

    private void removeAllBookEditions() throws SQLException {
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM book_loan");
            stmt.executeUpdate("DELETE FROM book");
            stmt.executeUpdate("DELETE FROM book_edition");
        }
    }
}

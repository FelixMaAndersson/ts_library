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
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@H2
public class BookDaoIntegrationTest {

    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        // this way we do not need to create a new datasource every time
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        BookDaoIntegrationTest.ds = ds;
    }

    @Test
    void getExistingBook() {
        final BookId bookId = BookId.of(2);

        BookDao bookDao = new BookDao(ds);

        Optional<Book> result = bookDao.get(bookId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(bookId);
        assertThat(result.get().getEdition().getTitle()).isEqualTo("T-SQL Fundamentals");
        assertThat(result.get().getAvailable()).isFalse();
    }

    @Test
    void findBookByTitle() {
        final String title = "The world atlas of wine";

        BookDao bookDao = new BookDao(ds);

        Set<BookEdition> result = bookDao.find(null, title, null);

        assertThat(result).containsExactly(
                new BookEdition(
                        2,
                        title,
                        "Hugh Johnson",
                        "9781784726188",
                        new DdsClassification(641, "Cookery")
                )
        );
    }

    @Test
    void getBookByClassification() {
        final DdsClassification classification = new DdsClassification(641, "Cookery");

        BookDao bookDao = new BookDao(ds);

        Set<BookEdition> result = bookDao.withClass(classification);

        assertThat(result).containsExactly(
                new BookEdition(
                        "The Oxford companion to wine",
                        "Jancis Robinson",
                        "9780198705383"
                ),
                new BookEdition(
                        "The world atlas of wine",
                        "Hugh Johnson",
                        "9781784726188"
                )
        );
    }

    @Test
    void getUserLoan() {
        final UserId userId = UserId.of(2);

        BookDao bookDao = new BookDao(ds);

        List<BookLoan> result = bookDao.loans(userId);

        assertThat(result)
                .extracting(loan-> loan.getBook().getId())
                .containsExactly(BookId.of(4), BookId.of(2));
    }

    @Test
    void getOverdueLoans() {

        BookDao bookDao = new BookDao(ds);

        List<BookLoan> result = bookDao.overdueLoans();

        assertThat(result)
                .extracting(loan -> loan.getBook().getId())
                .containsExactly(BookId.of(4), BookId.of(2));
    }

    @Test
    void returnBook() {
        final BookId bookId = BookId.of(2);

        BookDao bookDao = new BookDao(ds);

        boolean result = bookDao.returnBook(bookId);
        Book returnedBook = bookDao.get(bookId).orElseThrow();

        assertThat(result).isTrue();
        assertThat(returnedBook.getAvailable()).isTrue();
    }
}

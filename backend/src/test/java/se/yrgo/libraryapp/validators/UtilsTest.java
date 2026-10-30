package se.yrgo.libraryapp.validators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/*
* Tests that no alphabetical letters, nor white space, are deleted.
* Everything else should be deleted, and uppercase letters are set to lowercase.
* */
public class UtilsTest {
    @ParameterizedTest
    @CsvSource({
            "'FELIX', 'felix'",
            "'FELix', 'felix'",
            "'Felix123', 'felix'",
            "'Felix!', 'felix'",
            "'Felix Andersson', 'felix andersson'",
            "'F1 A2', 'f a'",
            "'Felix😊', 'felix'",
            "'Привет123', 'привет'",
            "'ΑΒΓ123', 'αβγ'",
            "'', ''"
    })
    void correctFiltering(String input, String expected) {

        String result = Utils.onlyLettersAndWhitespace(input);
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            ""
    })
    void correctConversion(String input, String expected) {

        String result = Utils.cleanAndUnLeet(input);
        assertThat(result).isEqualTo(expected);
    }

}

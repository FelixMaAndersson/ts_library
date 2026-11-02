package se.yrgo.libraryapp.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/*
* Tests that no alphabetical letters, nor white space, are deleted.
* Everything else should be deleted, and uppercase letters are set to lowercase.
* */
public class UtilsTest {
    @ParameterizedTest
    @CsvFileSource(resources = "/se/yrgo/libraryapp/filter-data.csv", numLinesToSkip = 1)
    void correctFiltering(String input, String expected) {
        String result = Utils.onlyLettersAndWhitespace(input);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void filterNullInputThrowsNullException() {

        // Null handling is not specified in documentation
        // Even though the current implementaiton throws
        // a NullPointerException since str.codePoint() is called
        // WITHOUT A NULL CHECK!!!
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> Utils.onlyLettersAndWhitespace(null));
    }


    /*
     * Tests that no alphabetical letters, nor white space, are deleted.
     * Everything else should be deleted, and uppercase letters are set to lowercase.
     * Also that leet speak is translated to alphabetical letters.
     * */
    @ParameterizedTest
    @CsvFileSource(resources = "/se/yrgo/libraryapp/conversion-data.csv", numLinesToSkip = 1)
    void correctLeetConversion(String input, String expected) {

        // I'm absolutely not fluent in leet speak, but 2 and 9 seems to me
        // to be reasonable to translate?
        // the documentation says nothing (even though it is shown in the leetMap)
        // that leet speak is to be translated into the latin alphabet.
        String result = Utils.cleanAndUnLeet(input);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void leetNullInputThrowsNullException() {

        // Null handling is not specified in documentation
        // Even though the current implementaiton throws
        // a NullPointerException since str.codePoint() is called
        // WITHOUT A NULL CHECK!!!
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> Utils.cleanAndUnLeet(null));
    }

}

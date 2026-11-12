package se.yrgo.libraryapp.validators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;


public class RealNameTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "felix",
            "Hampus",
            "F31ix",
            "Felix Åman",
            "Felix Shietman"})
    void RealNameCorrect(String validName) {

        Boolean result = RealName.validate(validName);

        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "balderdash",
            "Felix shiet",
            "Felix Shiet",
            "shiet Felix",
            "Shiet Felix",
            "Felix shi3t",
            "Felix S.h.i.e.t",
            "Felix!Shiet"})
    void RealNameProfanity(String invalidName) {

        Boolean result = RealName.validate(invalidName);

        assertThat(result).isFalse();
    }
}

package se.yrgo.libraryapp.validators;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

public class UsernameTest {

    @ParameterizedTest
    @ValueSource(strings = {"Felix", "FElix", "Felix.Andersson", "Felix_Andersson", "@FelixAndersson", "Felix123456789", "Feli", "Felix-Andersson", "Felix0"})
    void correctUsername(String name) {

        boolean result = Username.validate(name);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Felix Andersson", " Felix", "Felix ", "     ", "Fel", "Felix!", "Felix😊", "FelixÅndersson", "Fдlix", "Felix\t", "Felix\n", "FelixAnderssån"})
    @EmptySource
    void incorrectUsername(String name) {

        boolean result = Username.validate(name);
        assertThat(result).isFalse();
    }
}
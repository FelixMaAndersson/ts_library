package se.yrgo.libraryapp.validators;

import org.junit.jupiter.api.Test;
import se.yrgo.libraryapp.entities.User;
import se.yrgo.libraryapp.entities.UserId;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UsernameTest {
    @Test
    void correctUsername() {
        //Arrange
        UserId id = new UserId(1);
        User user = new User(id, "Felix", "Felix");

        //Act
        String result = user.getName();

        //Assert
        assertThat(result).isEqualTo("Felix");
    }

    @Test
    void incorrectUsername() {
        //Arrange
        UserId id = new UserId(2);
        User user = new User(id, "name with space", "namewithoutspace");

        //Act
        String result = user.getName();

        //Assert
        assertThat(result).isNotEqualTo("name with space");
    }
}
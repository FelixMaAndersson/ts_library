package se.yrgo.libraryapp.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.yrgo.libraryapp.entities.forms.RegisterUserData;
import se.yrgo.libraryapp.services.UserService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class RegisterUserControllerTest {

    @Mock
    private UserService userService;

    @Test
    void registerUser() {
        final String username = "newuser";
        final String realname = "new user";
        final String password = "password";
        final RegisterUserData userdata = new RegisterUserData(username, realname, password);

        when(userService.register(username, realname, password))
                .thenReturn(true);

        RegisterUserController controller = new RegisterUserController(userService);

        boolean result = controller.register(userdata);

        assertThat(result).isTrue();
    }

    @Test
    void checkUsernameAvaliablilty() {
        final String username = "testuser";

        when(userService.isNameAvailable(username))
                .thenReturn(false);

        RegisterUserController controller = new RegisterUserController(userService);

        boolean result = controller.isNameAvailable(username);

        assertThat(result).isFalse();
    }
}

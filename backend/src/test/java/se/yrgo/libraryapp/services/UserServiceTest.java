package se.yrgo.libraryapp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.yrgo.libraryapp.dao.UserDao;
import se.yrgo.libraryapp.entities.LoginInfo;
import se.yrgo.libraryapp.entities.UserId;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class UserServiceTest {
    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder encoder;

    //validate()

    @Test
    void correctLogin() {
        final String userId = "1";
        final UserId id = UserId.of(userId);
        final String username = "testuser";
        final String password = "password";
        final String passwordHash = "password";
        final LoginInfo info = new LoginInfo(id, passwordHash);

        when(userDao.getLoginInfo(username)).thenReturn(Optional.of(info));

        when(encoder.matches(password, passwordHash))
                .thenReturn(true);

        UserService userService = new UserService(userDao, encoder);
        assertThat(userService.validate(username,
                password)).isEqualTo(Optional.of(id));
    }

    @Test
    void incorrectPassword() {
        final String userId = "1";
        final UserId id = UserId.of(userId);
        final String username = "testuser";
        final String password = "wrongPassword";
        final String passwordHash = "hashedPassword";

        LoginInfo info = new LoginInfo(id, passwordHash);

        when(userDao.getLoginInfo(username))
                .thenReturn(Optional.of(info));

        when(encoder.matches(password, passwordHash))
                .thenReturn(false);

        UserService userService = new UserService(userDao,encoder);

        assertThat(userService.validate(username, password)).isEmpty();
    }

    @Test
    void nonExistingUser() {
        final String username = "unknownuser";
        final String password = "password";

        when(userDao.getLoginInfo(username))
                .thenReturn(Optional.empty());

        UserService userService = new UserService(userDao,encoder);

        assertThat(userService.validate(username, password)).isEmpty();

        verifyNoInteractions(encoder);
    }

    // register()

    @Test
    void registerSuccessful() {
        final String username = "testuser";
        final String realname = "test user";
        final String password = "password";
        final String passwordHash = "hashedPassword";

        when(encoder.encode(password)).thenReturn(passwordHash);
        when(userDao.register(username, realname, passwordHash)).thenReturn(true);

        UserService userService = new UserService(userDao, encoder);

        boolean result = userService.register(username, realname, password);

        assertThat(result).isTrue();

        verify(userDao).register(username, realname, passwordHash);
    }

    @Test
    void registerFailed() {
        final String username = "testuser";
        final String realname = "test user";
        final String password = "password";
        final String passwordHash = "hashedPassword";

        when(encoder.encode(password)).thenReturn(passwordHash);
        when(userDao.register(username, realname, passwordHash)).thenReturn(false);

        UserService userService = new UserService(userDao, encoder);

        boolean result = userService.register(username, realname, password);

        assertThat(result).isFalse();

        verify(userDao).register(username, realname, passwordHash);
    }

    // isNameAvailable()

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "f", "fe", "fel", "    ", " f", "f ", " felix", "felix "})
    void invalidUsername(String username) {
        UserService userService = new UserService(userDao, encoder);

        boolean result = userService.isNameAvailable(username);

        assertThat(result).isFalse();
        verifyNoInteractions(userDao);
    }

    @ParameterizedTest
    @ValueSource(strings = {"feli", "felix"})
    void validUsername(String username) {

        when(userDao.isNameAvailable(username)).thenReturn(true);

        UserService userService = new UserService(userDao, encoder);

        boolean result = userService.isNameAvailable(username);

        assertThat(result).isTrue();
    }

    @Test
    void unavailableName() {
        String username = "felix";

        when(userDao.isNameAvailable(username)).thenReturn(false);

        UserService userService = new UserService(userDao, encoder);

        boolean result = userService.isNameAvailable(username);

        assertThat(result).isFalse();
    }
}
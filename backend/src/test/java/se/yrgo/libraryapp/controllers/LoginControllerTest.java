package se.yrgo.libraryapp.controllers;

import io.jooby.*;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.yrgo.libraryapp.dao.RoleDao;
import se.yrgo.libraryapp.dao.SessionDao;
import se.yrgo.libraryapp.entities.Role;
import se.yrgo.libraryapp.entities.UserId;
import se.yrgo.libraryapp.entities.forms.LoginData;
import se.yrgo.libraryapp.services.UserService;

import org.pac4j.core.exception.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class LoginControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private RoleDao roleDao;
    @Mock
    private SessionDao sessionDao;
    @Mock
    private Context context;

    @Test
    void successfulLogin() {
        final String username = "testuser";
        final String password = "password";
        final UserId userId = UserId.of(1);
        final UUID sessionId = UUID.randomUUID();
        final LoginData loginData = new LoginData(username, password);

        when(userService.validate(username, password))
                .thenReturn(Optional.of(userId));

        when(sessionDao.create(userId))
                .thenReturn(sessionId);

        when(roleDao.get(userId))
                .thenReturn(List.of(Role.USER));

        LoginController controller = new LoginController(userService, roleDao, sessionDao);

        List<Role> result = controller.login(context, null, loginData);

        assertThat(result).containsExactly(Role.USER);
        verify(context).setResponseCookie(any(Cookie.class));
    }

    @Test
    void incorrectLoginDetails() {
        final String username = "testuser";
        final String password = "wrongPassword";
        final LoginData loginData = new LoginData(username, password);

        when(userService.validate(username, password))
                .thenReturn(Optional.empty());

        LoginController controller = new LoginController(userService, roleDao, sessionDao);

        List<Role> result = controller.login(context, null, loginData);

        assertThat(result).isEmpty();
        verify(context).setResponseCode(StatusCode.UNAUTHORIZED);
    }

    // I think it's weird that you get an empty list when you are already logged in
    // shouldn't you just get back your current session?
    @Test
    void alreadyLoggedIn() {
        final UserId userId = UserId.of(1);
        final UUID sessionId = UUID.randomUUID();
        final String username = "testuser";
        final String password = "password";
        final LoginData loginData = new LoginData(username, password);

        when(sessionDao.validate(sessionId))
                .thenReturn(userId);

        LoginController controller = new LoginController(userService, roleDao, sessionDao);

        List<Role> result = controller.login(context, sessionId.toString(), loginData);

        assertThat(result).isEmpty();
        verify(sessionDao, never()).create(any(UserId.class));
    }

    @Test
    void loginWithInvalidSession() {
        final UserId userId = UserId.of(1);
        final String username = "testuser";
        final String password = "password";
        final UUID oldSessionId = UUID.randomUUID();
        final UUID newSessionId = UUID.randomUUID();
        final LoginData loginData = new LoginData(username, password);

        when(sessionDao.validate(oldSessionId))
                .thenThrow(new CredentialsException(""));

        when(userService.validate(username, password))
                .thenReturn(Optional.of(userId));

        when(sessionDao.create(userId))
                .thenReturn(newSessionId);

        when(roleDao.get(userId))
                .thenReturn(List.of(Role.USER));

        LoginController controller = new LoginController(userService, roleDao, sessionDao);

        List<Role> result = controller.login(context, oldSessionId.toString(), loginData);

        assertThat(result).containsExactly(Role.USER);
        verify(context).setResponseCookie(any(Cookie.class));

    }


    @Test
    void validSession() {
        final UUID sessionId = UUID.randomUUID();
        final UserId userId = UserId.of(1);

        when(sessionDao.validate(sessionId))
                .thenReturn(userId);

        when(roleDao.get(userId))
                .thenReturn(List.of(Role.USER));

        LoginController controller = new LoginController(userService, roleDao, sessionDao);

        List<Role> result = controller.isLoggedIn(sessionId.toString());

        assertThat(result).containsExactly(Role.USER);
    }

    @Test
    void invalidSession() {
        final UUID sessionId = UUID.randomUUID();

        when(sessionDao.validate(sessionId))
                .thenThrow(new CredentialsException(""));

        LoginController controller = new LoginController(userService, roleDao, sessionDao);

        List<Role> result = controller.isLoggedIn(sessionId.toString());

        assertThat(result).isEmpty();
    }

    @Test
    void missingSession() {
        LoginController controller = new LoginController(userService, roleDao, sessionDao);

        List<Role> result = controller.isLoggedIn(null);

        assertThat(result).isEmpty();
    }
}

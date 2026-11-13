package se.yrgo.libraryapp.controllers;

import io.jooby.*;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.yrgo.libraryapp.dao.SessionDao;
import org.pac4j.core.exception.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.ArgumentCaptor;
import java.util.UUID;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class LogoutControllerTest {

    @Mock
    private SessionDao sessionDao;
    @Mock
    private Context context;

    // delete the cookie immediately when logout
    @Test
    void logoutWithValidSession() {
        final UUID sessionId = UUID.randomUUID();

        LogoutController controller = new LogoutController(sessionDao);

        controller.logout(context, sessionId.toString());

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        verify(sessionDao).delete(sessionId);
        verify(context).setResponseCookie(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue();

        assertThat(cookie.getName()).isEqualTo("session");
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge()).isZero();
    }

    @Test
    void logoutWithoutSession() {
        LogoutController controller = new LogoutController(sessionDao);

        controller.logout(context, null);

        verifyNoInteractions(sessionDao, context);
    }

    @Test
    void logoutWithInvalidSession() {
        final String invalidSession = "hej";

        LogoutController controller = new LogoutController(sessionDao);

        controller.logout(context, invalidSession);

        verifyNoInteractions(sessionDao);
        verify(context).setResponseCookie(any(Cookie.class));
    }
}

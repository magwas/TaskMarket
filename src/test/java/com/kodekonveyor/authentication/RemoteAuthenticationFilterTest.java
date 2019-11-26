package com.kodekonveyor.authentication;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;

import com.kodekonveyor.annotations.TestedBehaviour;
import com.kodekonveyor.annotations.TestedService;
import com.kodekonveyor.market.LogSeverityEnum;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@RunWith(MockitoJUnitRunner.class)
@TestedBehaviour("Puts the remote user into the Authentication object")
@TestedService("RemoteAuthenticationFilter")
public class RemoteAuthenticationFilterTest
    extends RemoteAuthenticationFilterTestBase {

  @DisplayName("if authenticated, calls the filter chain")
  @Test
  public void test01() throws IOException, ServletException {
    AuthenticationStubs.authenticated(userTestData);
    remoteAuthenticationFilter
        .doFilter(testData.REQUEST, servletResponse, filterChain);
    verify(filterChain).doFilter(testData.REQUEST, servletResponse);
  }

  @DisplayName("puts the username to Mapped Diagnostic Context for log")
  @Test
  public void testMdc1() throws IOException, ServletException {
    AuthenticationStubs.authenticated(userTestData);
    remoteAuthenticationFilter
        .doFilter(testData.REQUEST, servletResponse, filterChain);
    verify(mdc).put(logTestData.AUTH_USER, userTestData.LOGIN);
  }

  @DisplayName("puts the session id to Mapped Diagnostic Context for log")
  @Test
  public void testMdc2() throws IOException, ServletException {
    AuthenticationStubs.authenticated(userTestData);
    remoteAuthenticationFilter
        .doFilter(testData.REQUEST, servletResponse, filterChain);
    verify(mdc).put(eq(logTestData.AUTH_SESSION), anyString());
  }

  @DisplayName(
    "logs the authentication attempt"
  )
  @Test
  public void test02() throws IOException, ServletException {
    AuthenticationStubs.nullAuthentication();
    remoteAuthenticationFilter
        .doFilter(testData.REQUEST, servletResponse, filterChain);
    verify(loggerService)
        .call(
            logTestData.AUTHENTICATING, LogSeverityEnum.DEBUG,
            logTestData.EMPTY_MESSAGE
        );
  }

  @DisplayName("logs the authenticated user")
  @Test
  public void test03() throws IOException, ServletException {
    AuthenticationStubs.nullAuthentication();
    remoteAuthenticationFilter
        .doFilter(testData.REQUEST, servletResponse, filterChain);
    verify(loggerService)
        .call(logTestData.LOGIN, LogSeverityEnum.INFO, userTestData.LOGIN);
  }

  @DisplayName(
    "if Authentication is null, sets the remote user as authenticated, and clears it after the request is processed"
  )
  @Test
  public void test1() throws IOException, ServletException {
    AuthenticationStubs.nullAuthentication();
    remoteAuthenticationFilter
        .doFilter(testData.REQUEST, servletResponse, filterChain);
    assertRemoteUserIsCorrectlySetAndCleared(userTestData.LOGIN);
  }

  @DisplayName(
    "if Authentication is anonymous, sets the remote user as authenticated, and clears it after the request is processed"
  )
  @Test
  public void test2() throws IOException, ServletException {
    AuthenticationStubs.anonymous();
    remoteAuthenticationFilter
        .doFilter(testData.REQUEST, servletResponse, filterChain);
    assertRemoteUserIsCorrectlySetAndCleared(userTestData.LOGIN);
  }

  @DisplayName(
    "if Authentication is null and the remote user exists, sets the remote user as authenticated, and clears it after the request is processed"
  )
  @Test
  public void test3() throws IOException, ServletException {
    AuthenticationStubs.nullAuthentication();
    remoteAuthenticationFilter
        .doFilter(testData.REQUEST, servletResponse, filterChain);
    assertRemoteUserIsCorrectlySetAndCleared(userTestData.LOGIN);
  }

  private void assertRemoteUserIsCorrectlySetAndCleared(final String login) {
    verify(AuthenticationStubs.securityContext, times(2))
        .setAuthentication(newAuthentication.capture());
    final List<Authentication> capturedValues =
        newAuthentication.getAllValues();
    assertEquals(
        login, capturedValues.get(0).getCredentials()
    );
    assertEquals(null, capturedValues.get(1));
  }

  @DisplayName(
    "if Authentication is null and the remote user does not exists, creates an authenticated user, and clears authentication after the request is processed"
  )
  @Test
  public void test4() throws IOException, ServletException {
    AuthenticationStubs.nullAuthentication();
    remoteAuthenticationFilter.doFilter(
        testData.REQUEST_WITH_UNKNOWN_USER, servletResponse, filterChain
    );
    assertRemoteUserIsCorrectlySetAndCleared(userTestData.BAD_LOGIN);
  }

}

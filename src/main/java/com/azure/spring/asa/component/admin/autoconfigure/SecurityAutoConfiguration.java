package com.azure.spring.asa.component.admin.autoconfigure;

import com.azure.spring.asa.component.admin.web.LoginController;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.ws.rs.HttpMethod;

@Configuration(proxyBeanMethods = false)
public class SecurityAutoConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AdminServerProperties adminServer, ServerProperties serverProperties) throws Exception {
        http.authorizeRequests(
                (authorizeRequests) ->
                    authorizeRequests.antMatchers(adminServer.path("/assets/**")).permitAll()
                                                        .antMatchers(adminServer.path("/actuator/info")).permitAll()
                                                        .antMatchers(adminServer.path("/actuator/health")).permitAll()
                                                        .antMatchers(adminServer.path("/login_oauth2")).permitAll().anyRequest().authenticated()
            ).oauth2Login(
                login ->
                    login.defaultSuccessUrl(adminServer.path("/"), true)
                         .authorizationEndpoint(authorization -> authorization.baseUri(adminServer.path("/oauth2/authorization")))
                         .loginPage(adminServer.path("/login_oauth2"))
            ).logout((logout) -> logout.logoutUrl(adminServer.path("/logout")).logoutSuccessUrl(adminServer.path("/login_oauth2") + "?logoutSuccess=1"))
            .csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                .ignoringRequestMatchers(
                                    new AntPathRequestMatcher(adminServer.path("/instances"),
                                        HttpMethod.POST.toString()),
                                    new AntPathRequestMatcher(adminServer.path("/instances/*"),
                                        HttpMethod.DELETE.toString()),
                                    new AntPathRequestMatcher(adminServer.path("/actuator/**"))
                                ));
        return http.build();
    }

    @Bean
    public LoginController loginController(AdminServerUiProperties adminUi, AdminServerProperties adminServer,
                                           ApplicationContext applicationContext,
                                           ClientRegistrationRepository clientRegistrationRepository,
                                           ServerProperties serverProperties) {
        return new LoginController(adminUi, adminServer, applicationContext, clientRegistrationRepository, serverProperties);
    }

}

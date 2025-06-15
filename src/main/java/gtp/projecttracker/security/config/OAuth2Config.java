//package gtp.projecttracker.security.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.client.registration.ClientRegistration;
//import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
//import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
//import org.springframework.security.oauth2.core.AuthorizationGrantType;
//
//@Configuration
//public class OAuth2Config {
//
//    @Bean
//    public ClientRegistrationRepository clientRegistrationRepository(
//            @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId,
//            @Value("${spring.security.oauth2.client.registration.google.client-secret}") String clientSecret
//    ) {
//        return new InMemoryClientRegistrationRepository(
//                ClientRegistration.withRegistrationId("google")
//                        .clientId(clientId)
//                        .clientSecret(clientSecret)
//                        .scope("email", "profile")
//                        .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
//                        .tokenUri("https://oauth2.googleapis.com/token")
//                        .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
//                        .clientName("Google")
//                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
//                        .redirectUri("http://localhost:8080/api/v1/auth/oauth2/login/code/google")
//                        .build()
//        );
//    }
//}
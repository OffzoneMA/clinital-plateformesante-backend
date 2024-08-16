package com.clinitalPlatform.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.clinitalPlatform.util.PDFGenerator;
import com.clinitalPlatform.exception.CustumaccessdeniedHandler;
import com.clinitalPlatform.security.jwt.JwtAuthFilter;
import com.clinitalPlatform.security.oauth2.CustomOAuth2UserService;
import com.clinitalPlatform.security.oauth2.HttpCookieOAuth2AuthrizationRequestRepository;
import com.clinitalPlatform.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.clinitalPlatform.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.clinitalPlatform.security.oauth2.RestAuthenticationEntryPoint;
import com.clinitalPlatform.security.services.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class WebSecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private JwtAuthFilter authFilter;

    @Autowired
    private ApplicationContext applicationContext; 

    // Bean for handling OAuth2 authorization requests using HTTP cookies
    @Bean
    @Primary
    public HttpCookieOAuth2AuthrizationRequestRepository cookiesAuthorizationRepository() {
        return new HttpCookieOAuth2AuthrizationRequestRepository();
    }
    
	@Bean
  	public PDFGenerator pdfGenerator() {
    // return a new instance of PDFGenerator
    return new PDFGenerator();
  }

    // Security filter chain configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        String[] permitAllRoutes = {"/api/auth/**", "/api/users/activity/**","/api/demandes/create","/api/cabinet/**",
        		"/api/med/medecins",
        		"/api/med/medById/**",
    			"/api/med/medByName",
    			"/api/med/medByNameOrSpecAndVille/**",
    			"/api/med/medByNameAndSpec",
    			"/api/med/medByNameOrSpec",
    			"/api/med/medByVille",
    			"/api/med/getAllSpec",
                "/api/med/medByNameCabinetOrSpec",
                "/api/med/cabinets/**",
                "/api/med/agenda/**",
                "/api/med/medByCabinetName",
                "/api/med/medecins/schedules/filter",
                "/api/med/byLangue/**",
                "/api/med/allmedecins/**",
                "/api/med/medByLetter/**",
    			"/api/ville/**",
    			"/api/specialites/**",
                "/api/langues/**",
                "/api/tarifmed/**",
               "/api/medecinSchedule/fromCreno/**",
                "/api/rdv/rdvs/patient",
                "/api/rdv/patient/rdvbyday",
                "/api/cabinet/medecin/**",
                "/api/medecinSchedule/shedulebyMed/**",
                "/api/med/schedulesofMed/**"




        };



      
        String[] authenticatedRoutes = {"/api/demandes/**", "/api/med/**", "/api/doc/**", "/api/shares/**","/api/medecinSchedule/**","/api/patient/**",
                "/api/rdv/patient/**","/api/rdv/today/**","/api/rdv/med/**", "/api/rdv/rdvs/medecin","/api/users/**", "/api/rdv/**",

                "/api/med/agenda/**", "/api/medecinSchedule/fromCreno/**",
                "/api/rdv/rdvs/patient","/api/rdv/patient/rdvbyday","api/med/getAllPatients"
                                        

        };



        return http.csrf().disable()
                .cors().and()
                .authorizeHttpRequests()
                .requestMatchers(permitAllRoutes).permitAll()
                .and()
                .authorizeHttpRequests()
                .requestMatchers(authenticatedRoutes)
                .authenticated()
                .and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler())
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .oauth2Login()
                .authorizationEndpoint()
                .baseUri("/oauth2/authorize")
                .authorizationRequestRepository(cookiesAuthorizationRepository())
                .and()
                .redirectionEndpoint()
                .baseUri("/oauth2/callback/*")
                .and()
                .userInfoEndpoint()
                .userService(customOAuth2UserService)
                .and()
                .successHandler(applicationContext.getBean(OAuth2AuthenticationSuccessHandler.class)) // Obtain the bean from ApplicationContext
                .failureHandler(applicationContext.getBean(OAuth2AuthenticationFailureHandler.class))
                .and()
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }
    
    // Bean for password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // Bean for authentication provider
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
    
    // Bean for authentication manager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Bean for handling access denied situations
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustumaccessdeniedHandler();
    }
    

    // Bean for CORS configuration source
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("OPTIONS");
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Bean for user details service
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }
}

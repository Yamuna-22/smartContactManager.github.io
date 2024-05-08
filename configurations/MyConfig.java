package com.smart.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig extends WebSecurityConfiguration {
	
	
	@Bean
	public UserDetailsService getUserDetailService() {
		return new UserDetailsServiceImpl();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
     @Bean
     public DaoAuthenticationProvider authenticationProvider() {
    	 DaoAuthenticationProvider daoAuthenticationProvider= new DaoAuthenticationProvider();
    	 daoAuthenticationProvider.setUserDetailsService(this.getUserDetailService());
    	 daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
    	 return daoAuthenticationProvider;
 	}
     
    
   //below are two configure methods  
     
     protected void configure(AuthenticationManagerBuilder auth )throws Exception{
    	auth.authenticationProvider(authenticationProvider()) ;
    	
     }
     
     //telling the server to protect only a few methods
    
     
   
//	protected void configure(HttpSecurity http)throws Exception{
//    	 http.authorizeRequests().requestMatchers("/admin/**").hasRole("ADMIN")
//    	 .requestMatchers("/user/**").hasRole("USER").requestMatchers("/**").permitAll().and().formLogin().csrf(csrf -> csrf.disable());
//    	
//
//       
//     }
     
     
     @Bean
     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
         http.authorizeHttpRequests(auth -> auth
        		      .requestMatchers("/admin/**").hasRole("ADMIN")
                         .requestMatchers("/user/**").hasRole("USER")
                         .requestMatchers("/**").permitAll()
                 ).formLogin(customizer ->
                 {
                     customizer.loginPage("/signin")
                    // .loginProcessingUrl("/dologin") // Login processing URL
                     .defaultSuccessUrl("/user/index");
                     /*.failureUrl("/faliure");*/
                     
                      // Default success URL
                 })
                 .authenticationProvider(authenticationProvider())
               .csrf(AbstractHttpConfigurer::disable)
                 .exceptionHandling().accessDeniedHandler((request, response, accessDeniedException) -> {
                     System.out.println("Access Denied: " + accessDeniedException.getMessage());
                     response.sendRedirect("/access-denied");
                 })
         ;
			/* formLogin(Customizer.withDefaults()) */
         return http.build();
     }
     
    
}

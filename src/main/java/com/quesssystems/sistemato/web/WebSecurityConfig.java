package com.quesssystems.sistemato.web;

import com.quesssystems.sistemato.beans.usuario.CustomUsuarioDetailsService;
import com.quesssystems.sistemato.beans.usuario.UsuarioRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UsuarioRepository repo;

    public WebSecurityConfig(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(new CustomUsuarioDetailsService(repo));
        authenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder());

        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
                .antMatchers("/voltar/**").authenticated()

                .antMatchers("/usuarios").authenticated()
                .antMatchers("/usuarios/consultar/**").authenticated()
                .antMatchers("/usuarios/blocktoggle/**").authenticated()
                .antMatchers("/usuarios/admtoggle/**").authenticated()
                .antMatchers("/usuarios/excluir/**").authenticated()
                .antMatchers("/usuarios/novo").authenticated()
                .antMatchers("/usuarios/salvar").authenticated()

                .antMatchers("/automacoes/**").authenticated()
                .antMatchers("/automacoes/consultar/**").authenticated()
                .antMatchers("/automacoes/ativotoggle/**").authenticated()
                .antMatchers("/automacoes/excluir/**").authenticated()
                .antMatchers("/automacoes/novo").authenticated()
                .antMatchers("/automacoes/salvar").authenticated()

                .anyRequest().permitAll()

                .and()
                .formLogin()
                .loginPage("/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/automacoes/true")
                .permitAll()

                .and()
                .logout()
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                .invalidateHttpSession(false);

        http.sessionManagement()
                .invalidSessionUrl("/login?sessaoexpirada");

        http.csrf().disable();
    }
}

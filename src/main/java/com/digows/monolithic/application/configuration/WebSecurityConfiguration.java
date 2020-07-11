package com.digows.monolithic.application.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 
 * @author rodrigo
 */
//@Configuration
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter
{
	/*-------------------------------------------------------------------
	 * 		 					    ATTRIBUTES
	 *-------------------------------------------------------------------*/

	/*-------------------------------------------------------------------
	 * 		 					      BEANS
	 *-------------------------------------------------------------------*/

	/*-------------------------------------------------------------------
	 *							    OVERRIDES
	 *-------------------------------------------------------------------*/
	/**
	 *
	 * @return
	 * @throws Exception
	 */
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception
	{
		return super.authenticationManagerBean();
	}

	/**
	 *
	 * @param httpSecurity
	 * @throws Exception
	 */
	@Override
	protected void configure( HttpSecurity httpSecurity ) throws Exception
	{
		httpSecurity.csrf().disable();
		httpSecurity.headers().frameOptions().disable();
		httpSecurity.cors();

		httpSecurity
			.authorizeRequests()
				.antMatchers(HttpMethod.OPTIONS, "/oauth/token")
					.permitAll();

		httpSecurity.authorizeRequests()
			.anyRequest()
				.authenticated();
	}
	
	/**
	 * Override this method to configure {@link WebSecurity}. For example, if you wish to
	 * ignore certain requests.
	 */
	@Override
	public void configure( WebSecurity webSecurity ) throws Exception
	{
		webSecurity.ignoring()
			.antMatchers(

				//DWR
				"/broker/**/*.js",
				"/broker/download/**",
				"/broker/call/plaincall/__System.generateId.dwr",
				"/broker/call/plaincall/accountService.findUserByRecoveryToken.dwr",
				"/broker/call/plaincall/accountService.updateUserToUnlocked.dwr",
				"/broker/call/plaincall/accountService.updateUserPassword.dwr",
				"/broker/call/plaincall/accountService.sendUserPasswordRecoveryMail.dwr",
				"/broker/call/plaincall/accountService.sendUserTokenRecoveryMail.dwr"
			);
	}
}

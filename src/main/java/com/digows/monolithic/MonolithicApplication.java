package com.digows.monolithic;

import com.digows.monolithic.application.configuration.settings.DWRSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Validator;
import java.util.Locale;
import java.util.TimeZone;

@EnableConfigurationProperties({
	DWRSettings.class
})
@SpringBootApplication
public class MonolithicApplication
{

	/**
	 * Static block initialization
	 */
	static
	{
		Locale.setDefault( Locale.forLanguageTag("pt_BR") );
		TimeZone.setDefault( TimeZone.getTimeZone("GMT-3") );
	}

	public static void main(String[] args)
	{
		SpringApplication.run(MonolithicApplication.class, args);
	}

	/**
	 *
	 * @param messageSource
	 * @return
	 */
	@Bean
	public Validator validator(MessageSource messageSource )
	{
		final LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
		localValidatorFactoryBean.setValidationMessageSource(messageSource);
		return localValidatorFactoryBean;
	}

}

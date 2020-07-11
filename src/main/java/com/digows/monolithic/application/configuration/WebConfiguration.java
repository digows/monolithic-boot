package com.digows.monolithic.application.configuration;

import com.digows.monolithic.application.configuration.settings.DWRSettings;
import com.digows.monolithic.application.dwr.DwrAnnotationPostProcessor;
import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.GlobalFilter;
import org.directwebremoting.spring.DwrClassPathBeanDefinitionScanner;
import org.directwebremoting.spring.DwrSpringServlet;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 
 * @author rodrigo
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer
{
	/*-------------------------------------------------------------------
	 * 		 						BEANS
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 * @return
	 */
	@Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter()
	{
        final FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }

	//---------
	// DWR
	//---------
	/**
	 * Process all spring beans with @RemoteProxy
	 * @return
	 */
	@Bean
	public static DwrAnnotationPostProcessor dwrAnnotationPostProcessor(ApplicationContext applicationContext )
	{
		final BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
		final ClassPathBeanDefinitionScanner scanner = new DwrClassPathBeanDefinitionScanner(beanDefinitionRegistry);
        scanner.addIncludeFilter(new AnnotationTypeFilter(DataTransferObject.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(GlobalFilter.class));
        scanner.scan(
            "com.digows.monolithic.domain.entity.**",
            "com.digows.monolithic.application.dto.**"
        );

		return new DwrAnnotationPostProcessor();
	}

	/**
	 * 
	 * @return
	 */
	@Bean
	public ServletRegistrationBean<DwrSpringServlet> dwrSpringServletRegistration( DWRSettings dwrSettings )
	{
		final ServletRegistrationBean<DwrSpringServlet> registration = new ServletRegistrationBean<>( new DwrSpringServlet(), "/broker/*" );
		registration.setName( "dwrSpringServlet" );

		registration.addInitParameter( "debug", dwrSettings.getDebug() );
		registration.addInitParameter( "scriptCompressed", dwrSettings.getScriptCompressed() );
		registration.addInitParameter( "generateDtoClasses", dwrSettings.getGenerateDtoClasses() );
		
		//cross-domain
		registration.addInitParameter( "crossDomainSessionSecurity", dwrSettings.getCrossDomainSessionSecurity() );
		registration.addInitParameter( "overridePath", dwrSettings.getOverridePath() );
		registration.addInitParameter( "allowGetForSafariButMakeForgeryEasier", dwrSettings.getAllowGetForSafariButMakeForgeryEasier() );
		registration.addInitParameter( "allowScriptTagRemoting", dwrSettings.getAllowScriptTagRemoting() );
		
		return registration;
	}
//
//	/**
//	 * Global CORS filter
//	 * @return
//	 */
//	@Bean
//	public FilterRegistrationBean<CorsFilter> globalCorsFilter()
//	{
//	    final CorsConfiguration config = new CorsConfiguration();
//	    config.setAllowCredentials( true );
//	    config.addAllowedOrigin( CorsConfiguration.ALL );
//	    config.addAllowedHeader( CorsConfiguration.ALL );
//	    config.addAllowedMethod( CorsConfiguration.ALL );
//
//	    final UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
//	    urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", config);
//
//	    final FilterRegistrationBean<CorsFilter> filter = new FilterRegistrationBean<>();
//	    filter.setFilter( new CorsFilter(urlBasedCorsConfigurationSource) );
//		filter.setOrder(Ordered.HIGHEST_PRECEDENCE);
//	    return filter;
//	}

	/*-------------------------------------------------------------------
	 * 		 						OVERRIDES
	 *-------------------------------------------------------------------*/
}

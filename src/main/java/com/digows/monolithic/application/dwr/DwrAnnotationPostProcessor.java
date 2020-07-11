package com.digows.monolithic.application.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.annotations.GlobalFilter;
import org.directwebremoting.annotations.Param;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.namespace.ConfigurationParser;
import org.directwebremoting.spring.namespace.CreatorParserHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author Jose Noheda [jose.noheda@gmail.com]
 * @author rodrigo@gamifier.co
 */
public class DwrAnnotationPostProcessor extends CreatorParserHelper implements BeanFactoryPostProcessor
{
	/**
	 * 
	 */
	private static final Log LOG = LogFactory.getLog( DwrAnnotationPostProcessor.class );

	/**
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) throws BeansException
	{
		final BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;
		
		for ( String beanName : beanDefinitionRegistry.getBeanDefinitionNames() )
		{
			final BeanDefinition springConfigurator = ConfigurationParser.registerConfigurationIfNecessary( beanDefinitionRegistry );
			final BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder( beanDefinitionRegistry.getBeanDefinition( beanName ), beanName );
			final Class<?> beanDefinitionClass = getBeanDefinitionClass( beanDefinitionHolder, beanDefinitionRegistry );
			
			if ( beanDefinitionClass != null )
			{
				final RemoteProxy remoteProxy = beanDefinitionClass.getAnnotation( RemoteProxy.class );
				
				if ( remoteProxy != null )
				{
					String javascript = remoteProxy.name();
					if ( !StringUtils.hasText( javascript ) )
					{
						// convert the first char to lowercase
						javascript = beanDefinitionClass.getSimpleName();
						javascript = Character.toLowerCase( javascript.charAt( 0 ) ) + javascript.substring( 1 );
					}

					registerCreator( beanDefinitionHolder, beanDefinitionRegistry, beanDefinitionClass, javascript );
				}
				
				final GlobalFilter globalFilter = beanDefinitionClass.getAnnotation( GlobalFilter.class );
				if ( globalFilter != null )
				{
					
					final ManagedList filters = (ManagedList) springConfigurator.getPropertyValues().getPropertyValue( "filters" ).getValue();
					final Param[] params = globalFilter.params();
					if ( params != null )
					{
						for ( Param param : params )
						{
							beanDefinitionHolder.getBeanDefinition().getPropertyValues().addPropertyValue( param.name(), param.value() );
						}
					}
					filters.add( new RuntimeBeanReference( beanName ) );
				}
			}
		}
	}

	/**
	 * 
	 * @param beanDefinitionHolder
	 * @param beanDefinitionRegistry
	 * @return
	 */
	protected Class<?> getBeanDefinitionClass(BeanDefinitionHolder beanDefinitionHolder, BeanDefinitionRegistry beanDefinitionRegistry )
	{
		try
		{
			String beanClassName = resolveBeanClassname( beanDefinitionHolder.getBeanDefinition(), beanDefinitionRegistry );
			return ClassUtils.forName( beanClassName, ClassUtils.getDefaultClassLoader() );
		}
		catch ( Exception e )
		{
//			if ( LOG.isDebugEnabled() )
//			{
//				LOG.debug( "Could not infer class for [" + beanDefinitionHolder.getBeanName() + "]. Is it a factory bean? Omitting bean from annotation processing" );
//			}
			return null;
		}
	}

}

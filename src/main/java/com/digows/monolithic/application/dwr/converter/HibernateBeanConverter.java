package com.digows.monolithic.application.dwr.converter;

import com.digows.monolithic.application.dwr.converter.hibernate.HibernatePropertyDescriptorProperty;
import org.directwebremoting.ConversionException;
import org.directwebremoting.convert.BeanConverter;
import org.directwebremoting.extend.PlainProperty;
import org.directwebremoting.extend.Property;
import org.hibernate.Hibernate;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * BeanConverter that works with Hibernate to get BeanInfo.
 * 
 * @author rodrigo@gamifier.co
 */
public class HibernateBeanConverter extends BeanConverter
{
    /**
     * The log stream
     */
    private static final Logger log = Logger.getLogger(HibernateBeanConverter.class.getName());
    
	/*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/
	
    /**
     * Do we assume there is an open session and read properties?
     */
    protected boolean assumeSession = false;

    /**
     * The cache of method lookups that we've already done
     */
    private final ConcurrentMap<String, Method> methods = new ConcurrentHashMap<String, Method>();
    
	/*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     * 
     */
    @Override
    public Map<String, Property> getPropertyMapFromObject( Object example, boolean readRequired, boolean writeRequired ) throws ConversionException
    {
        Class<?> clazz = getClass(example);

        try
        {
            BeanInfo info = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] descriptors = info.getPropertyDescriptors();

            Map<String, Property> properties = new HashMap<>();
            for (PropertyDescriptor descriptor : descriptors)
            {
                String name = descriptor.getName();

                // We don't marshall getClass()
                if ("class".equals(name))
                {
                    continue;
                }

                // And this is something added by hibernate
                if ("hibernateLazyInitializer".equals(name))
                {
                    continue;
                }

                // Access rules mean we might not want to do this one
                if (!isAllowedByIncludeExcludeRules(name))
                {
                    continue;
                }

                if (readRequired && descriptor.getReadMethod() == null)
                {
                    continue;
                }

                if (writeRequired && descriptor.getWriteMethod() == null)
                {
                    continue;
                }

                if (!assumeSession)
                {
                    // We don't marshall un-initialized properties for
                    // Hibernate3
                    Method method = findGetter(example, name);

                    if (method == null)
                    {
                        log.warning("Failed to find property: " + name);

                        properties.put(name, new PlainProperty(name, null));
                        continue;
                    }

                    if (!Hibernate.isPropertyInitialized(example, name))
                    {
                        properties.put(name, new PlainProperty(name, null));
                        continue;
                    }

                    // This might be a lazy-collection so we need to double check
                    Object retval = null;
                    try
					{
                    	retval = method.invoke(example);
                    	
                        if (!Hibernate.isInitialized(retval))
                        {
                    		properties.put(name, new PlainProperty(name, null));
                            continue;                        		
                        }
					}
					catch ( java.lang.reflect.InvocationTargetException e )
					{
						properties.put(name, new PlainProperty(name, null));
						continue;
					}
                }

                properties.put(name, new HibernatePropertyDescriptorProperty(descriptor));
            }

            return properties;
        }
        catch (Exception ex)
        {
            throw new ConversionException(clazz, ex);
        }
    }

    /**
     * 
     */
    public Class<?> getClass(Object example)
    {
        if (example instanceof HibernateProxy)
        {
        	final HibernateProxy proxy = (HibernateProxy) example;
            final LazyInitializer initializer = proxy.getHibernateLazyInitializer();
            final SharedSessionContractImplementor implementor = initializer.getSession();

            if ( initializer.isUninitialized() )
            {
                try
                {
                    // getImplementation is going to want to talk to a session
                    if ( implementor.isClosed() )
                    {
                        // Give up and return example.getClass();
                        return example.getClass();
                    }
                }
                catch (NoSuchMethodError ex)
                {
                    // We must be using Hibernate 3.0/3.1 which doesn't have
                    // this method
                }
            }

            return initializer.getImplementation().getClass();
        }
        else
        {
            return example.getClass();
        }
    }

    /**
     * 
     */
    protected Method findGetter(Object data, String property) throws IntrospectionException
    {
    	final Class<?> clazz = getClass(data);
        final String key = clazz.getName() + ":" + property;
        Method method = methods.get(key);
        
        if (method == null)
        {
            Method newMethod = null;
            PropertyDescriptor[] props = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
            
            for (PropertyDescriptor prop : props)
            {
                if (prop.getName().equalsIgnoreCase(property))
                {
                    newMethod = prop.getReadMethod();
                }
            }
            method = methods.putIfAbsent(key, newMethod);
            
            if (method == null) 
            {
                // put succeeded, use new value
                method = newMethod;
            }
        }
        return method;
    }

    /**
     * 
     */
    public void setAssumeSession(boolean assumeSession)
    {
        this.assumeSession = assumeSession;
    }
}

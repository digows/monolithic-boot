package com.digows.monolithic.application.dwr.converter.hibernate;

import org.directwebremoting.ConversionException;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.extend.Property;
import org.directwebremoting.extend.PropertyDescriptorProperty;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import javax.servlet.ServletContext;
import java.beans.PropertyDescriptor;

/**
 * A {@link Property} that catches Hibernate exceptions.
 * This is useful for Hibernate where lazy loading results in an exception and you are unable to detect and prevent this.
 * 
 * @author rodrigo@gamifier.co
 */
public class HibernatePropertyDescriptorProperty extends PropertyDescriptorProperty
{
	/*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/
	
	/*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     * Simple constructor
     * @param descriptor The PropertyDescriptor that we are proxying to
     */
    public HibernatePropertyDescriptorProperty(PropertyDescriptor descriptor)
    {
        super(descriptor);
    }

	/*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /* (non-Javadoc)
     * @see org.directwebremoting.impl.PropertyDescriptorProperty#getValue(java.lang.Object)
     */
    @Override
    public Object getValue(Object bean) throws ConversionException
    {
        if ( !(bean instanceof HibernateProxy) )
        {
            // This is not a hibernate dynamic proxy, just use it
            return super.getValue(bean);
        }
        else
        {
            // If the property is already initialized, use it
            boolean initialized = Hibernate.isPropertyInitialized(bean, descriptor.getName());
            
            if ( initialized )
            {
                // This might be a lazy-collection so we need to double check
                Object reply = super.getValue(bean);
                initialized = Hibernate.isInitialized(reply);
            }

            if ( initialized )
            {
                return super.getValue(bean);
            }
            else
            {
                // If the session bound to the property is live, use it
                final HibernateProxy proxy = (HibernateProxy) bean;
                final LazyInitializer initializer = proxy.getHibernateLazyInitializer();
                final SharedSessionContractImplementor implementor = initializer.getSession();
                
                if ( implementor.isOpen() )
                {
                    return super.getValue(bean);
                }

                // So the property needs database access, and the session is closed
                // We'll need to try get another session
                final ServletContext context = WebContextFactory.get().getServletContext();
                final Session session = HibernateSessionAjaxFilter.getCurrentSession(context);

                if (session != null)
                {
                    session.update(bean);
                    return super.getValue(bean);
                }

                return null;
            }
        }
    }
}

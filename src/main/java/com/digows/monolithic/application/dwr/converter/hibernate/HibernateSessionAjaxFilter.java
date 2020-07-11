package com.digows.monolithic.application.dwr.converter.hibernate;

import org.directwebremoting.AjaxFilter;
import org.directwebremoting.AjaxFilterChain;
import org.directwebremoting.WebContextFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.servlet.ServletContext;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * An {@link AjaxFilter} that uses DWR Hibernate support classes to do a
 * {@link Session#beginTransaction()} before passing the control on to the chain
 * and a {@link Transaction#commit()} after.
 * 
 * @author rodrigo@gamifier.co
 */
public class HibernateSessionAjaxFilter implements AjaxFilter
{
    /**
     * The log stream
     */
    private static final Logger log = Logger.getLogger(HibernateSessionAjaxFilter.class.getName());

    /**
     * Under what name do we store the session factory?
     */
    protected static final String ATTRIBUTE_SESSION = "org.directwebremoting.hibernate.SessionFactory";
    
	/*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     * 
     */
    public Object doFilter(Object object, Method method, Object[] params, AjaxFilterChain chain) throws Exception
    {
        ServletContext context = WebContextFactory.get().getServletContext();
        SessionFactory sessionFactory = (SessionFactory) context.getAttribute(ATTRIBUTE_SESSION);

        Transaction transaction = null;
        if (sessionFactory != null)
        {
            Session session = sessionFactory.getCurrentSession();
            transaction = session.beginTransaction();
        }
        else
        {
            log.severe("SessionFactory not initialized for this web application. Use: H4SessionAjaxFilter.setSessionFactory(servletContext, sessionFactory);");
        }

        Object reply = chain.doFilter(object, method, params);

        if (transaction != null)
        {
            transaction.commit();
        }

        return reply;
    }

    /**
     * Assigns a {@link SessionFactory} to a {@link ServletContext} so DWR knows
     * how to get hold of a {@link Session}.
     * @param context The Servlet environment to store the ServletContext in
     * @param sessionFactory The Hibernate session factory to register
     */
    public static void setSessionFactory(ServletContext context, SessionFactory sessionFactory)
    {
        context.setAttribute(ATTRIBUTE_SESSION, sessionFactory);
    }

    /**
     * Get access to a Session, given the {@link SessionFactory} linked in
     * {@link #setSessionFactory(ServletContext, SessionFactory)}
     * @param context The webapp to link the calls together
     * @return A Session from the {@link SessionFactory} or null if
     * {@link #setSessionFactory(ServletContext, SessionFactory)} has not been
     * called for this {@link ServletContext}
     */
    public static Session getCurrentSession(ServletContext context)
    {
        SessionFactory sessionFactory = (SessionFactory) context.getAttribute(ATTRIBUTE_SESSION);
        if (sessionFactory == null)
        {
            return null;
        }

        return sessionFactory.getCurrentSession();
    }
}

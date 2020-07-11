package com.digows.monolithic.application.dwr.converter;


import org.directwebremoting.ConversionException;
import org.directwebremoting.extend.*;

import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.Optional;

/**
 * 
 * @author rodrigo@gamifier.co
 *
 */
public class OptionalConverter implements Converter
{
	/*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/
	/**
	 *
	 */
	protected ConverterManager converterManager;

	/*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
	/* (non-Javadoc)
	 * @see org.directwebremoting.Converter#setConverterManager(org.directwebremoting.ConverterManager)
	 */
	public void setConverterManager( ConverterManager converterManager )
	{
		this.converterManager = converterManager;
	}

	/**
	 *
	 * @param paramType
	 * @param data
	 * @return
	 * @throws ConversionException
	 */
	@Override
	public Object convertInbound( Class<?> paramType, InboundVariable data ) throws ConversionException
	{
	    if ( data.isNull() )
        {
            return Optional.empty();
        }
	    else
        {
        	if ( paramType.getGenericSuperclass() instanceof ParameterizedType )
	        {
		        final Class<?> optionalType = (Class<?>) ((ParameterizedType) paramType.getGenericSuperclass()).getActualTypeArguments()[0];
		        return Optional.ofNullable(
					converterManager.convertInbound( optionalType, data, data.getContext().getCurrentProperty() )
		        );
	        }
        	else
	        {
	        	if ( data.getType().equals("string") )
		        {
			        return Optional.ofNullable( converterManager.convertInbound( String.class, data, data.getContext().getCurrentProperty() ) );
		        }
	        	else if ( data.getType().equals("date") )
		        {
			        return Optional.ofNullable( converterManager.convertInbound( Date.class, data, data.getContext().getCurrentProperty() ) );
		        }
	        	else
		        {
			        return Optional.ofNullable( converterManager.convertInbound( data.getValue().getClass(), data, data.getContext().getCurrentProperty() ) );
		        }
	        }
        }
	}

	/**
	 *
	 * @param data
	 * @param outboundContext
	 * @return
	 * @throws ConversionException
	 */
	@Override
	public OutboundVariable convertOutbound(Object data, OutboundContext outboundContext ) throws ConversionException
	{
		if ( data == null ) return new NonNestedOutboundVariable("null");

		final Optional<?> optional = (Optional<?>) data;
		return optional
				.map( value -> this.converterManager.convertOutbound(value, outboundContext) )
				.orElse( new NonNestedOutboundVariable("null") );
	}
}

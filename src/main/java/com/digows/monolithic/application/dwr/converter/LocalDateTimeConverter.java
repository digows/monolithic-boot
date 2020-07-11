package com.digows.monolithic.application.dwr.converter;

import org.directwebremoting.ConversionException;
import org.directwebremoting.extend.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Conversor para o Java 8 LocalDate, LocalTime, e LocalDateTime
 * 
 * @author eduardo
 */
public class LocalDateTimeConverter extends AbstractConverter
{
	/*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 */
	private final ZoneOffset localOffset = ZonedDateTime.now().getOffset();

	/*
	 * (non-Javadoc)
	 * @see org.directwebremoting.extend.Converter#convertInbound(java.lang.Class, org.directwebremoting.extend.InboundVariable)
	 */
	@Override
	public Object convertInbound( Class<?> paramType, InboundVariable data ) throws ConversionException
	{
		if ( data.isNull() )
		{
			return null;
		}

		String val = data.getValue();

		if ( val.trim().equals( ProtocolConstants.INBOUND_NULL ) )
		{
			return null;
		}

		LocalDateTime date = null;

		try
		{
			val = java.net.URLDecoder.decode( val, "UTF-8" );

			if ( Pattern.matches( "^-?[0-9]+$", val ) )
			{
				long seconds = 0;
				if ( val.length() > 0 )
				{
					seconds = Long.parseLong( val ) / 1000;
				}
				date = LocalDateTime.ofEpochSecond( seconds, 0, localOffset );
			}
			else if ( Pattern.matches( "^[0-9]{2}/[0-9]{2}/[0-9]{4}$", val ) )
			{
				LocalDate d = LocalDate.parse( val, DateTimeFormatter.ofPattern( "dd/MM/yyyy" ) );
				date = d.atStartOfDay();
			}
			else
			{
				throw new IllegalArgumentException( "Invalid pattern for Date/Time! Given Pattern: '" + val + "'" );
			}
		}
		catch ( ConversionException ex )
		{
			throw ex;
		}
		catch ( Exception ex )
		{
			throw new ConversionException( paramType, ex );
		}

		if ( paramType == LocalDateTime.class )
		{
			return date;
		}
		else if ( paramType == LocalDate.class )
		{
			return date.toLocalDate();
		}
		else if ( paramType == LocalTime.class )
		{
			return date.toLocalTime();
		}
		else
		{
			throw new ConversionException( paramType );
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.directwebremoting.extend.Converter#convertOutbound(java.lang.Object, org.directwebremoting.extend.OutboundContext)
	 */
	@Override
	public OutboundVariable convertOutbound( Object data, OutboundContext outctx ) throws ConversionException
	{
		long ms;
		LocalDateTime dt;
		if ( data instanceof LocalDateTime )
		{
			dt = ( LocalDateTime ) data;
		}
		else if ( data instanceof LocalDate )
		{
			dt = LocalDateTime.of( ( LocalDate ) data, LocalTime.NOON );
		}
		else if ( data instanceof LocalTime )
		{
			dt = LocalDateTime.of( LocalDate.now(), ( LocalTime ) data );
		}
		else
		{
			throw new ConversionException( data.getClass() );
		}
		ms = dt.toEpochSecond( localOffset ) * 1000;

		return new NonNestedOutboundVariable( "new Date(" + ms + ")" );
	}
}

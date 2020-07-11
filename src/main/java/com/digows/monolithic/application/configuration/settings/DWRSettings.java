package com.digows.monolithic.application.configuration.settings;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 
 * @author rodrigo
 */
@Data
@ConfigurationProperties(prefix="dwr")
public class DWRSettings 
{
	/*-------------------------------------------------------------------
	 * 		 					SETTINGS
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 */
    private String debug = "false";
    /**
     * 
     */
    private String scriptCompressed = "true";
    /**
     * 
     */
    private String crossDomainSessionSecurity = "true";
    /**
     * 
     */
    private String overridePath = null;
    /**
     * 
     */
    private String generateDtoClasses = "dtoall";
    /**
     * 
     */
    private String allowGetForSafariButMakeForgeryEasier = "false";
    /**
     * 
     */
    private String allowScriptTagRemoting = "false";
}

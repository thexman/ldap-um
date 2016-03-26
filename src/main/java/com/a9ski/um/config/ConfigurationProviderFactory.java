package com.a9ski.um.config;

import java.io.Serializable;

public class ConfigurationProviderFactory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6513037524086573562L;
	
	protected ConfigurationProviderFactory() {
		super();
	}
	
	public static ConfigurationProvider createConfigurationProvider() {
		return new SystemPropertiesConfigurationProvider();
	}

}

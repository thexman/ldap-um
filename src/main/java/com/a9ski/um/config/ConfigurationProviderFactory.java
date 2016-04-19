package com.a9ski.um.config;

import java.io.Serializable;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationProviderFactory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6513037524086573562L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationProviderFactory.class);
	
	private static final ServiceLoader<ConfigurationProvider> serviceLoader = ServiceLoader.load(ConfigurationProvider.class);
	
	protected ConfigurationProviderFactory() {
		super();
	}
	
	public static ConfigurationProviderFactory createFactory() {
		final String className = System.getProperty("configuration-provider-factory-class");
		ConfigurationProviderFactory factory = null;
		if (className != null) {
			try {
				final Class<?> clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
				factory = (ConfigurationProviderFactory)clazz.newInstance();
			} catch (final ReflectiveOperationException ex) {
				LOGGER.error("Cannot create configuration provider factory", ex);
			}
		} 
		
		
		if (factory == null) {
			factory = new ConfigurationProviderFactory();
		}
		return factory;
	}
	
	public ConfigurationProvider createConfigurationProvider() {
		// TODO think about better service discovery mechanism 
		final Iterator<ConfigurationProvider> it = serviceLoader.iterator();
		final ConfigurationProvider configProvider;
		if (it.hasNext()) {
			configProvider = it.next();
		} else {
			configProvider = new SystemPropertiesConfigurationProvider();
		}
		return configProvider;
	}

}

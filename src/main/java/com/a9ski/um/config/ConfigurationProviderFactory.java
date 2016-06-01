/*
 * #%L
 * LDAP User Management
 * %%
 * Copyright (C) 2016 Kiril Arabadzhiyski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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

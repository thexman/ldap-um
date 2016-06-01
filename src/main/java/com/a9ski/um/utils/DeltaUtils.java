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
package com.a9ski.um.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

public class DeltaUtils {
	protected DeltaUtils() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Set<T> calculateDeltaAdded(Collection<T> oldValues, Collection<T> newValues) {
		final HashSet<T> delta = new HashSet<>();
		if (oldValues != null && newValues != null) {
			delta.addAll(CollectionUtils.subtract(newValues, oldValues));
		} else if (oldValues == null && newValues != null) {
			delta.addAll(newValues);
		}
		return delta;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Set<T> calculateDeltaDeleted(Collection<T> oldValues, Set<T> newValues) {
		final HashSet<T> delta = new HashSet<>();
		if (oldValues != null && newValues != null) {
			delta.addAll(CollectionUtils.subtract(oldValues, newValues));
		} else if (newValues == null && oldValues != null) {
			delta.addAll(oldValues);
		}
		return delta;
	}
}

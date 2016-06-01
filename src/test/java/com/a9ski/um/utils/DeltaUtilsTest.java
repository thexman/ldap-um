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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class DeltaUtilsTest {

	private Set<String> set(String...values) {
		return new HashSet<String>(Arrays.asList(values));
	}
	
	@Test
	public void testAdded() {
		assertEquals(set("B","C"), DeltaUtils.calculateDeltaAdded(set("A"), set("B","A","C")));
		assertEquals(set("A","B","C"), DeltaUtils.calculateDeltaAdded(null, set("B","A","C")));
		assertEquals(set(), DeltaUtils.calculateDeltaAdded(set("A"), null));
		assertEquals(set(), DeltaUtils.calculateDeltaAdded(null, null));
	}
	
	@Test
	public void testDeleted() {
		assertEquals(set("B","C"), DeltaUtils.calculateDeltaDeleted(set("A","B","C"), set("A")));
		assertEquals(set("A","B","C"), DeltaUtils.calculateDeltaDeleted(set("B","A","C"), null));
		assertEquals(set(), DeltaUtils.calculateDeltaDeleted(null, set("A")));
		assertEquals(set(), DeltaUtils.calculateDeltaDeleted(null, null));
	}

}

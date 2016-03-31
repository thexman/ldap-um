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

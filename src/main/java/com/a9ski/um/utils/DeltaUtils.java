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

package com.ipeirotis.gal.core;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a basic 'memoization' feature.
 * 
 * Used by decorators
 * 
 * @author aldrin
 *
 */
public interface Memoizing extends Serializable {
	public Map<String, Object> getValueMap();
}

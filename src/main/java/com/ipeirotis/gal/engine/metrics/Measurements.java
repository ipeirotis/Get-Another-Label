package com.ipeirotis.gal.engine.metrics;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Measurements implements Iterable<Measure> {
	Map<MeasureUnit, Measure> repository = new LinkedHashMap<MeasureUnit, Measure>();

	public Measurements() {
	}

	public void set(MeasureUnit c, Double value) {
		if (!repository.containsKey(c)) {
			repository.put(c, new Measure(c, value));
		} else {
			repository.put(c, repository.get(c).append(value));
		}
	}

	@Override
	public Iterator<Measure> iterator() {
		return repository.values().iterator();
	}

}

package com.ipeirotis.gal.engine.metrics;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class Measure implements Comparable<Measure> {
	final MeasureUnit unit;

	final Double value;

	public Measure(MeasureUnit unit, Double value) {
		super();
		this.unit = unit;
		this.value = value;
	}

	public Measure append(Double value) {
		return new Measure(this.unit, this.value + value);
	}

	@Override
	public int compareTo(Measure other) {
		if (null == other)
			return -1;
		
		if (this == other)
			return 0;
		
		return new CompareToBuilder().append(this.unit, other.unit)
				.append(this.value, other.value).toComparison();
	}

}

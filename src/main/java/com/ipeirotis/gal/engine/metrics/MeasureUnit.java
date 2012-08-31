package com.ipeirotis.gal.engine.metrics;

public class MeasureUnit implements Comparable<MeasureUnit> {
	final String name;
	
	final String shortDesc;
	
	public MeasureUnit(String name, String shortDesc) {
		this.name = name;
		this.shortDesc = shortDesc;
	}

	public String getName() {
		return name;
	}

	public String getShortDesc() {
		return shortDesc;
	}

	@Override
	public int compareTo(MeasureUnit other) {
		if (null == other) {
			return -1;
		} else if (other == this) {
			return 0;
		}
		
		return name.compareTo(other.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}

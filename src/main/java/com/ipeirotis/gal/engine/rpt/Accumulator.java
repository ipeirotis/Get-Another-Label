package com.ipeirotis.gal.engine.rpt;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.stripEnd;

import java.util.ArrayList;
import java.util.List;

import com.ipeirotis.utils.Utils;

class Accumulator {
	String name;
	
	List<Double> values;
	
	boolean valid = true;
	
	boolean hasPercent = false;

	public Accumulator(String name) {
		super();
		this.name = name;
		this.values = new ArrayList<Double>();
	}
	
	public void append(String value) {
		if (! valid)
			return;
		
		if (isBlank(value) || "---".equals(value))
			return;
		
		if (!hasPercent && value.endsWith("%"))
			hasPercent = true;
		
		if (hasPercent && value.endsWith("%"))
			value = stripEnd(value, "%");
		
		Double d = null; 
		
		try {
			d = Double.valueOf(value);
		} catch (NumberFormatException e) {
			valid = false;
			
			return;
		}
		
		if (Double.isNaN(d)) {
			valid = false;
			
			return;
		}
			
		
		values.add(d);
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public Double getAverage() {
		double sum = 0d;
		
		for (Double d : values)
			sum += d;
		
		return (sum / values.size());
	}
	
	@Override
	public String toString() {
		if (! valid)
			return "";
		
		return String.format("%s:%s", name, getFormattedAverage());
	}

	public String getFormattedAverage() {
		Double average = getAverage();
		
		if (Double.isNaN(average))
			return "N/A";
		
		return "" + Utils.round(average, 3) + (hasPercent ? "%" : "");
	}
}
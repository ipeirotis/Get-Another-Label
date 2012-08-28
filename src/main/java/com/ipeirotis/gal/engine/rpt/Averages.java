package com.ipeirotis.gal.engine.rpt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

public class Averages {
	public Collection<Accumulator> generateFrom(InputStream inputStream) {
		Map<Integer, Accumulator> accumulators = new TreeMap<Integer, Accumulator>();
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream));
			
			String[] headerLine = reader.readLine().split("\\t");
			for (ListIterator<String> headerIterator = Arrays.asList(headerLine).listIterator(); headerIterator.hasNext();) {
				int index = headerIterator.nextIndex();
				String fieldName = headerIterator.next();
				
				accumulators.put(index, new Accumulator(fieldName));
			}
			
			String line = null;
			
			while (null != (line = reader.readLine())) {
				for (ListIterator<String> rowIterator = Arrays.asList(line.split("\\t")).listIterator(); rowIterator.hasNext();) {
					int index = rowIterator.nextIndex();
					String columnValue = rowIterator.next();
					
					accumulators.get(index).append(columnValue);
				}
			}
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
		
		Collection<Accumulator> result = new ArrayList<Accumulator>();
		
		for (Accumulator a : accumulators.values())
			if (a.isValid())
				result.add(a);
		
		return result;
	}

}
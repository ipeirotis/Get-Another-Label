package com.ipeirotis.gal.csv;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

import com.ipeirotis.gal.decorator.FieldAccessors.FieldAccessor;

public class CSVGenerator<T> {
	final Collection<FieldAccessor> fieldAcessors;

	final Iterable<T> iterable;

	public CSVGenerator(Collection<FieldAccessor> fieldAcessors, Iterable<T> iterable) {
		super();
		this.fieldAcessors = fieldAcessors;
		this.iterable = iterable;
	}
	
	public void writeTo(PrintWriter printWriter) throws IOException {
		CSVWriter csvWriter = new CSVWriter(printWriter, '\t', CSVWriter.NO_QUOTE_CHARACTER);
		
		String[] headerLine = getHeaderLineFor();
		
		csvWriter.writeNext(headerLine);
		
		for (T record : iterable) {
			String[] recordLine = getRecordLineFor(record);
			
			csvWriter.writeNext(recordLine);			
		}
		
		csvWriter.close();
	}

	private String[] getHeaderLineFor() {
		List<String> result = new ArrayList<String>();
		
		for (FieldAccessor fieldAcessor : fieldAcessors)
			result.add(fieldAcessor.getDesc());
		
		return (String[]) result.toArray(new String[result.size()]);
	}
	
	private String[] getRecordLineFor(T object) {
		List<String> result = new ArrayList<String>();
		
		for (FieldAccessor fieldAcessor : fieldAcessors)
			result.add(fieldAcessor.getStringValue(object));
		
		return (String[]) result.toArray(new String[result.size()]);
	}
}

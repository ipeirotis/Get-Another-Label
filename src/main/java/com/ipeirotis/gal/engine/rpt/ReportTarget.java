package com.ipeirotis.gal.engine.rpt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

public class ReportTarget {
	protected PrintWriter writer;
	
	public ReportTarget(OutputStream outputStream) {
		this.writer = new PrintWriter(outputStream, true);
	}
	
	public void close() throws IOException {
		this.writer.close();
	}
	
	public void print(String mask, Object... args) {
		this.writer.write(String.format(Locale.ENGLISH, mask, args));
	}
	
	public void println(String mask, Object... args) {
		print(mask, args);
		print("\n");
	}

	public PrintWriter getPrintWriter() {
		return writer;
	}

}

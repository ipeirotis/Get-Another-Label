package com.ipeirotis.gal.engine.rpt;

import java.io.IOException;

public abstract class Report {
	public boolean execute(ReportingContext ctx) throws IOException {
		return false;
	}

	protected void info(String message, Object... args) {
		// TODO: Implement PROPER Logging
		System.out.println(String.format(message, args));
	}

}

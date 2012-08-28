package com.ipeirotis.gal.engine.rpt;

import java.io.IOException;

public class ObjectResultReport extends Report {
	@Override
	public boolean execute(ReportingContext ctx) throws IOException {
		ReportTarget reportTarget = new FileReportTarget("results/object-probabilities.txt");
		
		info("Printing category probabilities for objects (see also file results/object-probabilities.txt)");
		
		ctx.getDawidSkene().printObjectClassProbabilities(reportTarget.getPrintWriter());
		
		reportTarget.close();

		return super.execute(ctx);
	}

}

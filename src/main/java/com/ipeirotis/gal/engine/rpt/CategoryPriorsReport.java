package com.ipeirotis.gal.engine.rpt;

import java.io.IOException;

public class CategoryPriorsReport extends Report {
	@Override
	public boolean execute(ReportingContext ctx) throws IOException {
		info("Printing prior probabilities (see also file results/priors.txt)");

		ReportTarget reportTarget = new FileReportTarget("results/priors.txt");
		
		ctx.getDawidSkene().printPriors(reportTarget.getPrintWriter());

		return super.execute(ctx);
	}
}

package com.ipeirotis.gal.engine.rpt;

import java.io.IOException;
import java.io.PrintWriter;

import com.ipeirotis.gal.scripts.Category;
import com.ipeirotis.gal.scripts.DawidSkene;

public class CategoryPriorsReport extends Report {
	@Override
	public boolean execute(ReportingContext ctx) throws IOException {
		info("Printing prior probabilities (see also file results/priors.txt)");

		ReportTarget reportTarget = new FileReportTarget("results/priors.txt");
		
		printPriors(ctx.getDawidSkene(), reportTarget.getPrintWriter());

		return super.execute(ctx);
	}

	public void printPriors(DawidSkene dawidSkene, PrintWriter writer) {
		for (Category c : dawidSkene.getCategories().values()) {
			writer.println("Prior[" + c.getName() + "]=" + c.getPrior());
		}
	}
}

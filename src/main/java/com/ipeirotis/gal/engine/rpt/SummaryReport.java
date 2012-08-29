package com.ipeirotis.gal.engine.rpt;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class SummaryReport extends Report {
	@Override
	public boolean execute(ReportingContext ctx) throws IOException {
		info("Summary");

		ReportTarget[] reportTargets = new ReportTarget[] {
				new StreamReportTarget(System.out),
				new FileReportTarget("results/summary.txt") };

		Collection<Accumulator> accumulators = new ArrayList<Accumulator>();
		
		accumulators.addAll(getObjectAverages(ctx));
		accumulators.addAll(getWorkerAverages(ctx));

		for (ReportTarget reportTarget : reportTargets) {
			reportTarget.println("Categories: %s", ctx.getEngine()
					.getCategories().size());
			reportTarget.println("Objects in Data Set: %s", ctx.getDawidSkene()
					.getObjects().size());
			reportTarget.println("Workers in Data Set: %s", ctx.getDawidSkene()
					.getWorkers().size());
			reportTarget.println("Labels Assigned by Workers: %s", ctx
					.getEngine().getLabels().size());

			for (Accumulator a : accumulators) {
				reportTarget.println("Average Value for %s: %s", a.name,
						a.getFormattedAverage());
			}

			reportTarget.close();
		}

		return super.execute(ctx);
	}

	public Collection<Accumulator> getObjectAverages(ReportingContext ctx) throws IOException {
		Averages averages = new Averages();
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//		ctx.getDawidSkene().printObjectClassProbabilities(
//				new PrintWriter(baos, true));

		return averages.generateFrom(new FileInputStream("results/object-probabilities.txt"));
	}

	public Collection<Accumulator> getWorkerAverages(ReportingContext ctx) throws IOException {
		Averages averages = new Averages();
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//		ctx.getDawidSkene().printAllWorkerScores(new PrintWriter(baos, true), false);

		return averages.generateFrom(new FileInputStream("results/worker-statistics-summary.txt"));
	}
}

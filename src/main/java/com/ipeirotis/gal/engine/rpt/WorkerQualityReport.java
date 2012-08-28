package com.ipeirotis.gal.engine.rpt;

import java.io.IOException;

public class WorkerQualityReport extends Report {
	static class ReportKind {
		String outputFile;
		
		boolean detailedP;

		public ReportKind(String outputFile, boolean detailedP) {
			super();
			this.outputFile = outputFile;
			this.detailedP = detailedP;
		}
	}
	
	private static final ReportKind[] REPORT_KINDS = new ReportKind[] {
		new ReportKind("results/worker-statistics-summary.txt", false),
		new ReportKind("results/worker-statistics-detailed.txt", true)
	};
	
	@Override
	public boolean execute(ReportingContext ctx) throws IOException {
		// Save the estimated quality characteristics for each worker
		info("Estimating worker quality (see also file results/worker-statistics-summary.txt and results/worker-statistics-detailed.txt)");
		
		for (ReportKind rk : REPORT_KINDS) {
			ReportTarget reportTarget = new FileReportTarget(
					rk.outputFile);

			ctx.getDawidSkene().printAllWorkerScores(reportTarget.getPrintWriter(), rk.detailedP);
			reportTarget.close();
		}
		
		return super.execute(ctx);
	}
}

package com.ipeirotis.gal.engine.rpt;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import com.ipeirotis.gal.Helper;
import com.ipeirotis.gal.core.Worker;
import com.ipeirotis.gal.decorator.FieldAccessors.FieldAccessor;

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
			new ReportKind("results/worker-statistics-detailed.txt", true) };

	@Override
	public boolean execute(ReportingContext ctx) throws IOException {
		// Save the estimated quality characteristics for each worker
		info("Estimating worker quality (see also file results/worker-statistics-summary.txt and results/worker-statistics-detailed.txt)");

		for (ReportKind rk : REPORT_KINDS) {
			ReportTarget reportTarget = new FileReportTarget(rk.outputFile);

			printAllWorkerScores(ctx, reportTarget,
					rk.detailedP);
			reportTarget.close();
		}

		return super.execute(ctx);
	}

	public void printAllWorkerScores(ReportingContext ctx, ReportTarget reportTarget,
			boolean detailed) throws IOException {
		Collection<FieldAccessor> fieldAccessors = ctx.getDawidSkene()
				.getFieldAccessors(Worker.class);
		Collection<Worker> workers = ctx
				.getDawidSkene().getWorkers().values();
		
		if (!detailed) {
			PrintWriter writer = reportTarget.getPrintWriter();
			CSVGenerator<Worker> csvGenerator = new CSVGenerator<Worker>(fieldAccessors, workers);
			
			csvGenerator.writeTo(writer);

			return;
		}

		for (Worker w : workers) {
			for (FieldAccessor a : fieldAccessors) {
				String value = null;
				
				if (null != a.getFormatter()) {
					Double doubleValue = (Double) a.getValue(w);
					value = "" + a.getFormatter().format(doubleValue);
				} else {
					value = "" + a.getValue(w);
				}
				
				reportTarget.println("%s: %s", a.getDesc(), value);
			}
			
			writeEstimatedConfusionMatrix(reportTarget, w);

			writeEvalConfusionMatrix(reportTarget, w);
			
			reportTarget.println("");
		}
	}

	/**
	 * TODO Smells bad
	 */
	private void writeEvalConfusionMatrix(ReportTarget reportTarget, Worker w) {
		reportTarget.println("Actual Confusion Matrix (evaluation data):");
		for (String correct_name : w.getDs().getCategories().keySet()) {
			for (String assigned_name : w.getDs().getCategories().keySet()) {
				Double cm_entry = w.getErrorRate_Eval(correct_name,
						assigned_name);
				String s_cm_entry = Double.isNaN(cm_entry) ? "---" : Helper.round(100 * cm_entry, 3).toString();
				reportTarget.print("P[" + correct_name + "->" + assigned_name + "]="
						+ s_cm_entry + "%%\t");
			}
			reportTarget.println("");
		}
	}

	/**
	 * TODO Smells bad
	 */
	private void writeEstimatedConfusionMatrix(ReportTarget reportTarget, Worker w) {
		reportTarget.println("Estimated Confusion Matrix:");
		for (String correct_name : w.getDs().getCategories().keySet()) {
			for (String assigned_name : w.getDs().getCategories().keySet()) {
				Double cm_entry = w.getErrorRate(correct_name,
						assigned_name);
				String s_cm_entry = Double.isNaN(cm_entry) ? "---" : Helper.round(100 * cm_entry, 3).toString();
				reportTarget.print("P[" + correct_name + "->" + assigned_name + "]="
						+ s_cm_entry + "%%\t");
			}
			reportTarget.println("");
		}
	}

}

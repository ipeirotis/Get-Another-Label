package com.ipeirotis.gal.engine.rpt;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.ipeirotis.gal.scripts.Category;
import com.ipeirotis.gal.scripts.DawidSkene;
import com.ipeirotis.gal.scripts.Worker;
import com.ipeirotis.utils.Utils;

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

			printAllWorkerScores(ctx.getDawidSkene(),
					reportTarget.getPrintWriter(), rk.detailedP);
			reportTarget.close();
		}

		return super.execute(ctx);
	}

	public void printAllWorkerScores(DawidSkene ds, PrintWriter writer, boolean detailed) {
		if (!detailed) {
			writer.println("Worker\tEst. Error Rate\tEval. Error Rate\tEst. Quality (Expected)\tEst. Quality (Optimized)\tEval. Quality (Expected)\tEval. Quality (Optimized)\tNumber of Annotations\tGold Tests");
		}

		for (Map.Entry<String, Worker> entry : ds.getWorkers().entrySet()) {
			writer.println(printWorkerScore(entry.getValue(), detailed));
		}
	}

	public String printWorkerScore(Worker w, boolean detailed) {

		StringBuffer sb = new StringBuffer();

		String workerName = w.getName();

		// Double cost_naive = w.getWorkerCost(categories,
		// Worker.COST_NAIVE_EST);
		// String s_cost_naive = (Double.isNaN(cost_naive)) ? "---" :
		// Utils.round(100 * cost_naive, 2) + "%";
		//
		// Double cost_naive_eval = w.getWorkerCost(categories,
		// Worker.COST_NAIVE_EVAL);
		// String s_cost_naive_eval = (Double.isNaN(cost_naive_eval)) ? "---" :
		// Utils.round(100 * cost_naive_eval, 2) + "%";

		Map<String, Category> categories = w.getDs().getCategories();
		
		Double cost_exp = w.getWorkerQuality(categories, Worker.ClassificationMethod.DS_Soft_Estm);
		String s_cost_exp = (Double.isNaN(cost_exp)) ? "---" : Math
				.round(100 * cost_exp) + "%";

		Double cost_min = w.getWorkerQuality(categories, Worker.ClassificationMethod.DS_MinCost_Estm);
		String s_cost_min = (Double.isNaN(cost_min)) ? "---" : Math
				.round(100 * cost_min) + "%";

		Double cost_exp_eval = w
				.getWorkerQuality(categories, Worker.ClassificationMethod.DS_Soft_Eval);
		String s_cost_exp_eval = (Double.isNaN(cost_exp_eval)) ? "---" : Math
				.round(100 * cost_exp_eval) + "%";

		Double cost_min_eval = w
				.getWorkerQuality(categories, Worker.ClassificationMethod.DS_MinCost_Eval);
		String s_cost_min_eval = (Double.isNaN(cost_min_eval)) ? "---" : Math
				.round(100 * cost_min_eval) + "%";

		Integer contributions = w.getAssignedLabels().size();
		Integer gold_tests = w.getDs().countGoldTests(w.getAssignedLabels());

		if (detailed) {
			sb.append("Worker: " + workerName + "\n");
			// sb.append("Est. Error Rate: " + s_cost_naive + "\n");
			// sb.append("Eval. Error Rate: " + s_cost_naive_eval + "\n");
			sb.append("Est. Quality (Expected): " + s_cost_exp + "\n");
			sb.append("Est. Quality (Optimized): " + s_cost_min + "\n");
			sb.append("Eval. Quality (Expected): " + s_cost_exp_eval + "\n");
			sb.append("Eval. Quality (Optimized): " + s_cost_min_eval + "\n");
			sb.append("Number of Annotations: " + contributions + "\n");
			sb.append("Number of Gold Tests: " + gold_tests + "\n");

			sb.append("Confusion Matrix (Estimated): \n");
			for (String correct_name : categories.keySet()) {
				for (String assigned_name : categories.keySet()) {
					Double cm_entry = w.getErrorRate(correct_name,
							assigned_name);
					String s_cm_entry = Double.isNaN(cm_entry) ? "---" : Utils
							.round(100 * cm_entry, 3).toString();
					sb.append("P[" + correct_name + "->" + assigned_name + "]="
							+ s_cm_entry + "%\t");
				}
				sb.append("\n");
			}
			sb.append("Confusion Matrix (Evaluation data): \n");
			for (String correct_name : categories.keySet()) {
				for (String assigned_name : categories.keySet()) {
					Double cm_entry = w.getErrorRate_Eval(correct_name,
							assigned_name);
					String s_cm_entry = Double.isNaN(cm_entry) ? "---" : Utils
							.round(100 * cm_entry, 3).toString();
					sb.append("P[" + correct_name + "->" + assigned_name + "]="
							+ s_cm_entry + "%\t");
				}
				sb.append("\n");
			}
		} else {
			sb.append(workerName + "\t" + "NaN" /* TODO: It was s_cost_naive */
					+ "\t" + "NaN" /* TODO: it was s_cost_naive_eval */+ "\t"
					+ s_cost_exp + "\t" + s_cost_min + "\t" + s_cost_exp_eval
					+ "\t" + s_cost_min_eval + "\t" + contributions + "\t"
					+ gold_tests);
		}

		return sb.toString();
	}

}

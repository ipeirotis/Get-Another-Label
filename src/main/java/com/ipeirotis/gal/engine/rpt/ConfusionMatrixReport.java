package com.ipeirotis.gal.engine.rpt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.ipeirotis.gal.decorator.DawidSkeneDecorator;
import com.ipeirotis.gal.scripts.ConfusionMatrix;
import com.ipeirotis.gal.scripts.Datum;
import com.ipeirotis.gal.scripts.Datum.ClassificationMethod;
import com.ipeirotis.gal.scripts.DawidSkene;
import com.ipeirotis.utils.Utils;

public class ConfusionMatrixReport extends Report {
	@Override
	public boolean execute(ReportingContext ctx) throws IOException {
		// Save the estimated quality characteristics for each worker
		info("Writing DS Confusion Matrix (see also file results/confusion-matrix.txt)");

		ReportTarget reportTarget = new FileReportTarget(
				"results/confusion-matrix.txt");

		reportConfusionMatrix(ctx, reportTarget);

		reportTarget.close();

		return super.execute(ctx);
	}

	public void reportConfusionMatrix(ReportingContext ctx,
			ReportTarget reportTarget) {
		DawidSkene ds = ctx.getDawidSkene();
		DawidSkeneDecorator decorator = new DawidSkeneDecorator(ds);
		
		String type = "Estimated";
		
		List<ClassificationMethod> estimatedClasMethods = Arrays.asList(ClassificationMethod.DS_Soft, ClassificationMethod.MV_Soft);
		
		for (ClassificationMethod estimatedClasMethod : estimatedClasMethods) {
			ConfusionMatrix confMatrix = decorator.getEstimatedConfusionMatrix(estimatedClasMethod);
			
			reportTarget.println("%s Confusion Matrix (%s):", type, estimatedClasMethod.name());
			
			for (String from : confMatrix.getCategoryNames()) {
				for (String to : confMatrix.getCategoryNames()) {
					Double cm_entry = confMatrix.getErrorRate(from, to);
					String s_cm_entry = Double.isNaN(cm_entry) ? "---" : Utils
							.round(100 * cm_entry, 3).toString();
					reportTarget.print("P[%s->%s]=%s\t", from, to, s_cm_entry);
				}
				reportTarget.println("");
			}
			reportTarget.println("");
		}

		type = "Evaluation";
		
		// for each classification method, we need to create a confusion matrix
		for (ClassificationMethod clasMethod : Datum.ClassificationMethod.values()) {
			ConfusionMatrix confMatrix = decorator.getConfusionMatrix(clasMethod);
			
			reportTarget.println("%s Confusion Matrix (%s):", type, clasMethod.name());
			
			for (String from : confMatrix.getCategoryNames()) {
				for (String to : confMatrix.getCategoryNames()) {
					Double cm_entry = confMatrix.getErrorRate(from, to);
					String s_cm_entry = Double.isNaN(cm_entry) ? "---" : Utils
							.round(100 * cm_entry, 3).toString();
					reportTarget.print("P[%s->%s]=%s\t", from, to, s_cm_entry);
				}
				reportTarget.println("");
			}
			reportTarget.println("");
		}
	}

}

package com.ipeirotis.gal.engine.rpt;

import java.io.IOException;
import java.io.PrintWriter;

import com.ipeirotis.gal.algorithms.DawidSkene;
import com.ipeirotis.gal.core.Datum;

public class ObjectResultReport extends Report {
	@Override
	public boolean execute(ReportingContext ctx) throws IOException {
		ReportTarget reportTarget = new FileReportTarget(
				"results/object-probabilities.txt");

		info("Printing category probabilities for objects (see also file results/object-probabilities.txt)");

		printObjectClassProbabilities(ctx.getDawidSkene(),
				reportTarget.getPrintWriter());

		reportTarget.close();

		return super.execute(ctx);
	}

	/**
	 * Prints the objects that have probability distributions with entropy
	 * higher than the given threshold
	 * 
	 * @param dawidSkene
	 * 
	 * @param writer
	 *            PrintWriter to write into
	 * @throws IOException
	 *             I/O Exception
	 */
	public void printObjectClassProbabilities(DawidSkene dawidSkene,
			PrintWriter writer) throws IOException {
		CSVGenerator<Datum> csvGenerator = new CSVGenerator<Datum>(
				dawidSkene.getFieldAccessors(Datum.class), dawidSkene.getObjects().values());

		csvGenerator.writeTo(writer);
	}
}

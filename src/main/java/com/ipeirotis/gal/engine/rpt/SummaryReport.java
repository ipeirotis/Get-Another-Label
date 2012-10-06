package com.ipeirotis.gal.engine.rpt;

import java.io.IOException;

import com.ipeirotis.gal.core.Datum;
import com.ipeirotis.gal.core.Worker;
import com.ipeirotis.gal.decorator.FieldAccessors.EvalDatumFieldAccessor;
import com.ipeirotis.gal.decorator.FieldAccessors.FieldAccessor;

public class SummaryReport extends Report {
	@Override
	public boolean execute(ReportingContext ctx) throws IOException {
		info("Summary");

		ReportTarget[] reportTargets = new ReportTarget[] {
				new StreamReportTarget(System.out),
				new FileReportTarget("results/summary.txt") };

		for (ReportTarget reportTarget : reportTargets) {
			reportTarget.println("Categories: %s", ctx.getEngine()
					.getCategories().size());
			reportTarget.println("Objects in Data Set: %s", ctx.getDawidSkene()
					.getObjects().size());
			reportTarget.println("Workers in Data Set: %s", ctx.getDawidSkene()
					.getWorkers().size());
			reportTarget.println("Labels Assigned by Workers: %s", ctx
					.getEngine().getLabels().size());
			
			for (FieldAccessor a : ctx.getDawidSkene().getFieldAccessors(Datum.class)) {
				if (! a.isAveraged())
					continue;
				
				reportTarget.println("[%s] %s: %s", a.getDesc(), a.getSummaryDescription(), getAverage(a, ctx.getDawidSkene().getObjects().values()));
			}
			
			for (FieldAccessor a : ctx.getDawidSkene().getFieldAccessors(Worker.class)) {
				if (! a.isAveraged())
					continue;
				
				reportTarget.println("[%s] %s: %s", a.getDesc(), a.getSummaryDescription(), getAverage(a, ctx.getDawidSkene().getWorkers().values()));
			}

			reportTarget.close();
		}

		return super.execute(ctx);
	}

	public <T> Object getAverage(FieldAccessor fieldAccessor, Iterable<T> objects) {
		Double accumulator = 0d;
		double count = 0;
		boolean evalP = fieldAccessor instanceof EvalDatumFieldAccessor;
		
		for (T object : objects) {
			if (evalP) {
				Datum datum = ((Datum) object);
				
				if (! datum.isEvaluation())
					continue;
			}
			
			
			Double value = (Double) fieldAccessor.getValue(object);
			Integer weight = fieldAccessor.getWeight(object);
			
 			if (null == value || value.isNaN())
				continue;
			
			accumulator += weight*value;
				count += weight;
		}
		
		Double result = accumulator / count;
		
		if (null != fieldAccessor.getFormatter()) {
			return fieldAccessor.getFormatter().format(result);
		}
		
		return result;
	}
	
	
	
}

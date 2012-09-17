package com.ipeirotis.gal.decorator;

import com.ipeirotis.gal.decorator.FieldAccessors.EntityFieldAccessor;
import com.ipeirotis.gal.scripts.DawidSkene;
import com.ipeirotis.gal.scripts.Worker;

public class DawidSkeneDecorator extends Decorator<DawidSkene> {
	public DawidSkeneDecorator(DawidSkene wrapped) {
		super(wrapped);
	}

	public Double getWeightedQualityForEstQualityExp() {
		return getWeightedQualityFor(object,
				FieldAccessors.WORKER_ACESSORS.EST_QUALITY_EXP);
	}

	public Double getWeightedQualityForEstQualityOpt() {
		return getWeightedQualityFor(object,
				FieldAccessors.WORKER_ACESSORS.EST_QUALITY_OPT);
	}

	public Double getWeightedQualityForEvalQualityExp() {
		return getWeightedQualityFor(object,
				FieldAccessors.WORKER_ACESSORS.EVAL_QUALITY_EXP);
	}

	public Double getWeightedQualityForEvalQualityOpt() {
		return getWeightedQualityFor(object,
				FieldAccessors.WORKER_ACESSORS.EVAL_QUALITY_OPT);
	}

	private Double getWeightedQualityFor(DawidSkene object,
			EntityFieldAccessor fieldAccessor) {
		Double wqN = 0d;
		long wqD = 0;

		for (Worker w : object.getWorkers().values()) {
			Double metricValue = getMetric(w, fieldAccessor);
			int noOfLabels = w.getAssignedLabels().size();
			
			if (null == metricValue)
				return null;
			
			wqN += noOfLabels * metricValue;
			wqD += noOfLabels;
		}

		return wqN / wqD;
	}

	private Double getMetric(Worker w, EntityFieldAccessor fieldAccessor) {
		Object objVal = fieldAccessor.getValue(w);
		
		return (null == objVal ? null : ((Double) objVal));
	}

}

package com.ipeirotis.gal.decorator;

import com.ipeirotis.gal.decorator.FieldAccessors.EntityFieldAccessor;
import com.ipeirotis.gal.scripts.Worker;
import com.ipeirotis.gal.scripts.Worker.ClassificationMethod;

public class WorkerDecorator extends Decorator<Worker> {
	private Worker worker;
	
	public WorkerDecorator(Worker wrapped) {
		super(wrapped);
		
		this.worker = (Worker) wrapped;
	}
	
	public Double getExpectedCost() {
		return worker.getWorkerQuality(worker.getDs().getCategories(), ClassificationMethod.DS_Soft_Estm);
	}

	public Double getMinCost() {
		return worker.getWorkerQuality(worker.getDs().getCategories(), ClassificationMethod.DS_MinCost_Estm);
	}
	
	public Double getMaxLikelihoodCost() {
		return worker.getWorkerQuality(worker.getDs().getCategories(), ClassificationMethod.DS_MaxLikelihood_Estm);
	}
	
	public Double getExpCostEval() {
		return worker.getWorkerQuality(worker.getDs().getCategories(), ClassificationMethod.DS_Soft_Eval);
	}

	public Double getMinCostEval() {
		return worker.getWorkerQuality(worker.getDs().getCategories(), ClassificationMethod.DS_MinCost_Eval);
	}

	public Double getMaxLikelihoodCostEval() {
		return worker.getWorkerQuality(worker.getDs().getCategories(), ClassificationMethod.DS_MaxLikelihood_Eval);
	}

	
	public Double getWeightedExpectedCost() {
		return getNumContributions() * worker.getWorkerQuality(worker.getDs().getCategories(), ClassificationMethod.DS_Soft_Estm);
	}

	public Double getWeightedMinCost() {
		return getNumContributions() * worker.getWorkerQuality(worker.getDs().getCategories(), ClassificationMethod.DS_MinCost_Estm);
	}
	
	public Double getWeightedMaxLikelihoodCost() {
		return getNumContributions() * worker.getWorkerQuality(worker.getDs().getCategories(), ClassificationMethod.DS_MaxLikelihood_Estm);
	}
	
	
	public Double getWeightedExpCostEval() {
		return getNumContributions() * worker.getWorkerQuality(worker.getDs().getCategories(), ClassificationMethod.DS_Soft_Eval);
	}

	public Double getWeightedMinCostEval() {
		return getNumContributions() * worker.getWorkerQuality(worker.getDs().getCategories(), ClassificationMethod.DS_MinCost_Eval);
	}
	
	public Double getWeightedMaxLikelihoodCostEval() {
		return getNumContributions() * worker.getWorkerQuality(worker.getDs().getCategories(), ClassificationMethod.DS_MaxLikelihood_Eval);
	}

	public Double getWeightedQualityForEstQualityExp() {
		return getWeightedQualityFor(FieldAccessors.WORKER_ACCESSORS.EST_QUALITY_EXP);
	}

	public Double getWeightedQualityForEstQualityOpt() {
		return getWeightedQualityFor(FieldAccessors.WORKER_ACCESSORS.EST_QUALITY_OPT);
	}

	public Double getWeightedQualityForEvalQualityExp() {
		return getWeightedQualityFor(FieldAccessors.WORKER_ACCESSORS.EVAL_QUALITY_EXP);
	}

	public Double getWeightedQualityForEvalQualityOpt() {
		return getWeightedQualityFor(FieldAccessors.WORKER_ACCESSORS.EVAL_QUALITY_OPT);
	}

	private Double getWeightedQualityFor(EntityFieldAccessor fieldAccessor) {
		Double metricValue = getMetric(object, fieldAccessor);
		
		if (metricValue.isNaN())
			return null;
		
		return metricValue;
	}

	private Double getMetric(Worker w, EntityFieldAccessor fieldAccessor) {
		Object objVal = fieldAccessor.getValue(w);
		
		return (null == objVal ? null : ((Double) objVal));
	}
	
	public Double getNumContributions() {
		return 0d + worker.getAssignedLabels().size();
	}
	
	public Double getNumGoldTests() {
		return 0d + worker.getDs().countGoldTests(worker.getAssignedLabels());
	}
}

package com.ipeirotis.gal.decorator;

import com.ipeirotis.gal.scripts.Worker;

public class WorkerDecorator extends Decorator<Worker> {
	private Worker worker;
	
	public WorkerDecorator(Worker wrapped) {
		super(wrapped);
		
		this.worker = (Worker) wrapped;
	}
	
	
	public Double getExpectedCost() {
		return worker.getWorkerQuality(worker.getDs().getCategories(), Worker.EXP_COST_EST);
	}

	public Double getMinCost() {
		return worker.getWorkerQuality(worker.getDs().getCategories(), Worker.MIN_COST_EST);
	}
	
	public Double getExpCostEval() {
		return worker.getWorkerQuality(worker.getDs().getCategories(), Worker.EXP_COST_EVAL);
	}

	public Double getMinCostEval() {
		return worker.getWorkerQuality(worker.getDs().getCategories(), Worker.MIN_COST_EVAL);
	}
	
	public Double getWeightedExpectedCost() {
		return getNumContributions() * worker.getWorkerQuality(worker.getDs().getCategories(), Worker.EXP_COST_EST);
	}

	public Double getWeightedMinCost() {
		return getNumContributions() * worker.getWorkerQuality(worker.getDs().getCategories(), Worker.MIN_COST_EST);
	}
	
	public Double getWeightedExpCostEval() {
		return getNumContributions() * worker.getWorkerQuality(worker.getDs().getCategories(), Worker.EXP_COST_EVAL);
	}

	public Double getWeightedMinCostEval() {
		return getNumContributions() * worker.getWorkerQuality(worker.getDs().getCategories(), Worker.MIN_COST_EVAL);
	}
	
	public Double getNumContributions() {
		return 0d + worker.getAssignedLabels().size();
	}
	
	public Double getNumGoldTests() {
		return 0d + worker.getDs().countGoldTests(worker.getAssignedLabels());
	}
}

package com.ipeirotis.gal.decorator;

import com.ipeirotis.gal.scripts.Worker;

public class WorkerDecorator extends Decorator {
	private Worker worker;

	public WorkerDecorator(Object wrapped) {
		super(wrapped);
		
		this.worker = (Worker) wrapped;
	}
	
	public Double getExpectedCost() {
		return worker.getWorkerCost(worker.getDs().getCategories(), Worker.EXP_COST_EST);
	}

	public Double getMinCost() {
		return worker.getWorkerCost(worker.getDs().getCategories(), Worker.MIN_COST_EST);
	}
	
	public Double getExpCostEval() {
		return worker.getWorkerCost(worker.getDs().getCategories(), Worker.EXP_COST_EVAL);
	}

	public Double getMinCostEval() {
		return worker.getWorkerCost(worker.getDs().getCategories(), Worker.MIN_COST_EVAL);
	}
	
	public Double getNumContributions() {
		return 0d + worker.getAssignedLabels().size();
	}
	
	public Double getNumGoldTests() {
		return 0d + worker.getDs().countGoldTests(worker.getAssignedLabels());
	}
}

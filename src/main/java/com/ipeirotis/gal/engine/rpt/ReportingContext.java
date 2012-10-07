package com.ipeirotis.gal.engine.rpt;

import java.util.Set;

import com.ipeirotis.gal.algorithms.DawidSkene;
import com.ipeirotis.gal.core.CorrectLabel;
import com.ipeirotis.gal.engine.Engine;

public class ReportingContext {
	private Engine engine;
	
	private Set<CorrectLabel> expectedEvaluation;

	public ReportingContext(Engine engine) {
		this.engine = engine;
	}
	
	public Engine getEngine() {
		return engine;
	}

	public DawidSkene getDawidSkene() {
		return this.engine.getDs();
	}
	
	public Set<CorrectLabel> getExpectedEvaluation() {
		return expectedEvaluation;
	}

	public void setExpectedEvaluation(Set<CorrectLabel> expectedEvaluation) {
		this.expectedEvaluation = expectedEvaluation;
	}
}

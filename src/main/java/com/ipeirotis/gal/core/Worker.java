/*******************************************************************************
 * Copyright 2012 Panos Ipeirotis
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.ipeirotis.gal.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ipeirotis.gal.Helper;
import com.ipeirotis.gal.algorithms.DawidSkene;

@SuppressWarnings("serial")
public class Worker implements Entity, Comparable<Worker> {

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Worker o) {

		return this.name.compareTo(o.getName());
	}

	private String										name;

	// The error matrix for the worker
	private ConfusionMatrix						cm;

	//The confusion matrix for the worker based on evaluation data
	private ConfusionMatrix						eval_cm;

	
	// The error matrix for the worker
	private HashMap<String, Integer>	priorCounts;

	// The labels that have been assigned by this worker
	// Serves mainly as a speedup, and intended to be used in
	// environments with persistence and caching (especially memcache)
	private Set<AssignedLabel>				labels;

	private DawidSkene ds;
	
	public DawidSkene getDs() {
		return ds;
	}

	/**
	 * @return the labels
	 */
	public Set<AssignedLabel> getAssignedLabels() {

		return labels;
	}

	public Worker(String name, DawidSkene ds) {
		this.ds = ds;
		
		Collection<Category> categories = ds.getCategories().values();

		this.name = name;
		this.cm = new ConfusionMatrix(categories);
		this.eval_cm = new ConfusionMatrix(categories);
		this.labels = new HashSet<AssignedLabel>();
		this.priorCounts = new HashMap<String, Integer>();
		for (Category c : categories) {
			priorCounts.put(c.getName(), 0);
		}
	}



	/**
	 * @param categories
	 * @return
	 */
	public HashMap<String, Double> getPrior() {

		int sum = 0;
		for (Integer i : this.priorCounts.values()) {
			sum += i;
		}
		
		HashMap<String, Double> worker_prior = new HashMap<String, Double>();
		for (String category : priorCounts.keySet()) {
			if (sum>0) {
				Double prob = 1.0 * this.priorCounts.get(category) / sum;
				worker_prior.put(category, prob);
			} else {
				worker_prior.put(category, 1.0/priorCounts.keySet().size());
			}
		}
	
		return worker_prior;
	}
	

	
	/**
	 * @return the eval_cm
	 */
	public ConfusionMatrix getEvalConfusionMatrix() {
	
		return eval_cm;
	}

	
	/**
	 * @param eval_cm the eval_cm to set
	 */
	public void setEvalConfusionMatrix(ConfusionMatrix eval_cm) {
	
		this.eval_cm = eval_cm;
	}

	public void addAssignedLabel(AssignedLabel al) {

		if (al.getWorkerName().equals(name)) {
			this.labels.add(al);
		}
		String category = al.getCategoryName();
		Integer categoryCount = priorCounts.get(category);
		priorCounts.put(category, categoryCount + 1);
	}

	
	public enum ClassificationMethod {
		DS_MaxLikelihood_Estm, DS_Soft_Estm, DS_MinCost_Estm,
		DS_MaxLikelihood_Eval, DS_Soft_Eval, DS_MinCost_Eval;
		//TODO: Add the naive cost estimation of measuring the off-diagonal elements of the conf matrix
		//TODO: Allow for using both the empirical and the ideal conf matrix in evaluation
	 }
	
	
	/**
	 * 
	 * @param method

	 * @return The quality of a worker, normalized to be 1 for a perfect worker, 0 for a spammer
	 */
	public Double getWorkerQuality(Map<String, Category>	categories, ClassificationMethod method) {


		Double cost = 0.0;
		
		// We estimate first how often the worker assigns each category label
		HashMap<String, Double> worker_prior = getPrior();

		for (Category assigned : categories.values()) {
			
			// Let's find the soft label that corresponds to assigned_label
			String assignedCategory = assigned.getName();
			Map<String, Double> softLabel = null;
			
			switch (method) {
				case DS_MaxLikelihood_Estm:
				case DS_Soft_Estm:
				case DS_MinCost_Estm:
					softLabel = getSoftLabelForLabel(assignedCategory, categories, false);
					break;
				case DS_MaxLikelihood_Eval:
				case DS_Soft_Eval:
				case DS_MinCost_Eval:
					softLabel = getSoftLabelForLabel(assignedCategory, categories, true);
					break;
			}
				
			// Add the cost of this label
			switch (method) {
				case DS_MaxLikelihood_Estm:
				case DS_MaxLikelihood_Eval:
					cost += Helper.getMaxLikelihoodCost(softLabel, categories) * worker_prior.get(assignedCategory);
					break;
				case DS_Soft_Estm:
				case DS_Soft_Eval:
					cost += Helper.getExpectedSoftLabelCost(softLabel, categories) * worker_prior.get(assignedCategory);
					break;
				case DS_MinCost_Estm:
				case DS_MinCost_Eval:
					cost += Helper.getMinCostLabelCost(softLabel, categories) * worker_prior.get(assignedCategory);
					break;
			}
			

		}

		// TODO: Here we may want to have different spammerCost for Eval and Estm
		return 1 - cost / Helper.getMinSpammerCost(categories);
		
	}
	
	
	public Map<String, Double> getSoftLabelForLabel(String label, Map<String, Category>	categories, boolean evaluation) {

		// Pr(c | label) = Pr(label | c) * Pr (c) / Pr(label)

		// We compute the Pr(label), using the worker prior 
		HashMap<String, Double> worker_prior = getPrior();

		HashMap<String, Double> result = new HashMap<String, Double>();
		for (Category source : categories.values()) {
			// Error is Pr(label | c)
			String sourceName = source.getName();
			double err = getErrorRate(source.getName(), label);
			double err_eval = getErrorRate_Eval(source.getName(), label);
			double workerprior = worker_prior.get(label);
			double sourceprior = source.getPrior();
			
			double error = evaluation? err_eval :err ;
			
			// Pr(c) is source.getPrior()
			// Pr(c | label) is soft
			double soft = ( workerprior > 0 )? sourceprior * error / workerprior : 0;
			result.put(sourceName, soft);
		}

		return result;
	}

	
	public Double getErrorRate(String categoryFrom, String categoryTo) {

		return this.cm.getErrorRate(categoryFrom, categoryTo);
	}
	
	public Double getErrorRate_Eval(String categoryFrom, String categoryTo) {

		return this.eval_cm.getErrorRate(categoryFrom, categoryTo);
	}


	public void setErrorRate(String categoryFrom, String categoryTo, Double error) {

		this.cm.setErrorRate(categoryFrom, categoryTo, error);
	}
	

	public void setErrorRate_Eval(String categoryFrom, String categoryTo, Double error) {

		this.eval_cm.setErrorRate(categoryFrom, categoryTo, error);
	}


	/**
	 * @return the cm
	 */
	public ConfusionMatrix getConfusionMatrix() {

		return cm;
	}

	/**
	 * @param cm
	 *          the cm to set
	 */
	public void setConfusionMatrix(ConfusionMatrix cm) {

		this.cm = cm;
	}

	/**
	 * @return the name
	 */
	public String getName() {

		return name;
	}

	/**
	 * @param name
	 *          the name to set
	 */
	public void setName(String name) {

		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Worker))
			return false;
		Worker other = (Worker) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}

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
package com.ipeirotis.gal.scripts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Worker {

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

	/**
	 * @return the labels
	 */
	public Set<AssignedLabel> getAssignedLabels() {

		return labels;
	}

	public Worker(String name, Set<Category> categories) {

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
	public HashMap<String, Double> getPrior(Set<Category> categories) {

		int sum = 0;
		for (Integer i : this.priorCounts.values()) {
			sum += i;
		}
		
		HashMap<String, Double> worker_prior = new HashMap<String, Double>();
		for (Category c : categories) {
			Double prob = 1.0 * this.priorCounts.get(c.getName()) / sum;
			worker_prior.put(c.getName(), prob);
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

	
	public static int	EXP_COST_EVAL	= 0; // Expected cost, according to evaluation data
	public static int	EXP_COST_EST	= 1; // Expected cost, according to the algorithm estimates
	public static int	MIN_COST_EVAL	= 2; // Minimized cost, according to evaluation data	
	public static int	MIN_COST_EST	= 3; // Minimized cost, according to the algorithm estimates
	public static int	COST_NAIVE_EST = 4; // Expected cost, according to the algorithm estimates, before fixing bias
	public static int	COST_NAIVE_EVAL = 5; // Expected cost, according to the algorithm estimates, before fixing bias
	
	/**
	 * 
	 * Estimates the cost for worker using various methods: COST_NAIVE: We do
	 * not adjust the label assigned by the worker (i.e., use the "hard" label)
	 * COST_ADJUSTED: We use the error rates of the worker and compute the
	 * posterior probability vector for each object COST_MINIMIZED: Like
	 * COST_ADJUSTED but we also assign the object to the category that
	 * generates the minimum expected error
	 * 
	 * @param w
	 *          The worker object
	 * @param method
	 *          One of DawidSkene.COST_NAIVE, DawidSkene.COST_ADJUSTED,
	 *          DawidSkene.COST_MINIMIZED
	 * @return The expected cost of the worker, normalized to be between 0 and
	 *         1, where 1 is the cost of a "spam" worker
	 */
	public Double getWorkerCost(HashMap<String, Category>	categories, int method) {

		assert (method == Worker.EXP_COST_EST || method == Worker.EXP_COST_EVAL 
				|| method==Worker.MIN_COST_EST || method == Worker.MIN_COST_EVAL);

		Double cost = 0.0;
		
		if (method == Worker.COST_NAIVE_EST) {
			Double c = 0.0;
			Double s = 0.0;
			for (Category from : categories.values()) {
				for (Category to : categories.values()) {
					c += from.getPrior() * from.getCost(to.getName()) * getErrorRate(from.getName(), to.getName());
					s += from.getPrior() * from.getCost(to.getName());
				}
			}
			return (s > 0) ? c / s : 0.0;
		}
		
		if (method == Worker.COST_NAIVE_EVAL) {
			Double c = 0.0;
			Double s = 0.0;
			for (Category from : categories.values()) {
				for (Category to : categories.values()) {
					c += from.getPrior() * from.getCost(to.getName()) * getErrorRate_Eval(from.getName(), to.getName());
					s += from.getPrior() * from.getCost(to.getName());
				}
			}
			return (s > 0) ? c / s : 0.0;
		}

		// We estimate first how often the worker assigns each category label

		HashMap<String, Double> worker_prior = getPrior(new HashSet<Category>(categories.values()));

		// We now know the frequency with which we will see a label
		// "assigned_label" from worker
		// Each of this "hard" labels from the annotator k will correspond to a
		// corrected
		// "soft" label
		for (Category assigned : categories.values()) {
			// Let's find the soft label that corresponds to assigned_label
			String assignedCategory = assigned.getName();

			if (method == Worker.EXP_COST_EVAL) {
				HashMap<String, Double> softLabel = getSoftLabelForLabel(assignedCategory, categories, true);
				cost += Helper.getExpectedSoftLabelCost(softLabel, categories) * worker_prior.get(assignedCategory);
			} else if (method == Worker.MIN_COST_EVAL) {
				HashMap<String, Double> softLabel = getSoftLabelForLabel(assignedCategory, categories, true);
				cost += Helper.getMinSoftLabelCost(softLabel, categories) * worker_prior.get(assignedCategory);
			} else if (method == Worker.EXP_COST_EST) {
				HashMap<String, Double> softLabel = getSoftLabelForLabel(assignedCategory, categories, false);
				cost += Helper.getExpectedSoftLabelCost(softLabel, categories) * worker_prior.get(assignedCategory);
			} else if (method == Worker.MIN_COST_EST) {
				HashMap<String, Double> softLabel = getSoftLabelForLabel(assignedCategory, categories, false);
				cost += Helper.getMinSoftLabelCost(softLabel, categories) * worker_prior.get(assignedCategory);
			} else {
				// We should never reach this
				System.err.println("Error: Incorrect method for cost");
			}

			// And add the cost of this label, weighted with the prior of seeing
			// this label.

		}

		if (method == Worker.EXP_COST_EVAL) {
			return cost / Helper.getSpammerCost(categories);
		} else if (method == Worker.MIN_COST_EVAL) {
			return cost / Helper.getMinSpammerCost(categories);
		} else if (method == Worker.EXP_COST_EST) {
			return cost / Helper.getSpammerCost(categories);
		} else if (method == Worker.MIN_COST_EST) {
			return cost / Helper.getMinSpammerCost(categories);
		} else {
			// We should never reach this
			System.err.println("Error: We should have never reached this in getWorkerCost");
			return Double.NaN;
		}

	}
	
	
	public HashMap<String, Double> getSoftLabelForLabel(String label, HashMap<String, Category>	categories, boolean evaluation) {

		// Pr(c | label) = Pr(label | c) * Pr (c) / Pr(label)

		// We compute the Pr(label), using the worker prior 
		HashMap<String, Double> worker_prior = getPrior(new HashSet<Category>(categories.values()));

		HashMap<String, Double> result = new HashMap<String, Double>();
		for (Category source : categories.values()) {
			// Error is Pr(label | c)
			Double error = evaluation?getErrorRate_Eval(source.getName(), label):getErrorRate(source.getName(), label);
			
			// Pr(c) is source.getPrior()
			// Pr(c | label) is soft
			Double soft = ( worker_prior.get(label) >0 )? source.getPrior() * error / worker_prior.get(label): 0;
			result.put(source.getName(), soft);
		}

		return result;
	}

	
	public Double getErrorRate(String categoryFrom, String categoryTo) {

		return this.cm.getErrorRate(categoryFrom, categoryTo);
	}
	
	public Double getErrorRate_Eval(String categoryFrom, String categoryTo) {

		return this.eval_cm.getErrorRate(categoryFrom, categoryTo);
	}


	/**
	 * @return the priorCounts
	 */
	public HashMap<String, Integer> getPriorCounts() {

		return priorCounts;
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

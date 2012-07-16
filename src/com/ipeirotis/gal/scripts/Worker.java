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
	 * 
	 *         public HashMap<String, Double> getPrior() {
	 *         HashMap<String, Double> worker_prior = new HashMap<String, Double>();
	 *         for (String categoryName : priorCounts.keySet()) {
	 *         Integer categoryCount = this.priorCounts.get(categoryName);
	 *         Integer total = this.labels.size();
	 *         worker_prior.put(categoryName, 1.0*categoryCount/total);
	 *         }
	 *         return worker_prior;
	 *         }
	 */

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

	public Double getErrorRate(String categoryFrom, String categoryTo) {

		return this.cm.getErrorRate(categoryFrom, categoryTo);
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

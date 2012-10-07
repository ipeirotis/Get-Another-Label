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
import java.util.TreeMap;

import com.ipeirotis.gal.Helper;
import com.ipeirotis.gal.algorithms.DawidSkene;

@SuppressWarnings("serial")
public class Datum implements Entity, Comparable<Datum> {
	String									name;

	// Defines if we have the correct category for this object
	// and if it is gold, the correctCategory holds the name of the correct category
	Boolean									isGold;

	Boolean									isEvaluation = false;

	String									goldCategory;

	String									evaluationCategory;

	// The probability estimates for the object belonging to different categories
	Map<String, Double>	categoryProbability;

	// The labels that have been assigned to this object, together with the workers who
	// assigned these labels. Serves mainly as a speedup, and intended to be used in
	// environments with persistence and caching (especially memcache)
	Set<AssignedLabel>			labels;
	
	public enum ClassificationMethod {
		MV_MaxLikelihood, DS_MaxLikelihood, MV_Soft, DS_Soft, MV_MinCost, DS_MinCost;
	 }
	
	private DawidSkene ds;
	
	Map<String, Object> valueMap = new TreeMap<String, Object>();

	public Datum(String name, DawidSkene ds) {
		this.ds = ds;

		this.name = name;
		this.isGold = false;
		this.goldCategory = null;
		this.labels = new HashSet<AssignedLabel>();

		// We initialize the probabilities vector to be uniform across categories
		this.categoryProbability = new HashMap<String, Double>();
		
		for (Category c : getCategories()) {
			this.categoryProbability.put(c.getName(), 1.0 / ds.getCategories().size());
		}
	}

	public void addAssignedLabel(AssignedLabel al) {
		if (al.getObjectName().equals(name)) {
			this.labels.add(al);
		}
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
		if (!(obj instanceof Datum))
			return false;
		Datum other = (Datum) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public Set<AssignedLabel> getAssignedLabels() {

		return this.labels;
	}

	private Collection<Category> getCategories() {
		return ds.getCategories().values();
	}

	public String getGoldCategory() {
		return goldCategory;
	}

	public DawidSkene getDs() {
		return ds;
	}

	/**
	 * Computes the actual classification cost for this object, compared to the evaluation data for this datum
	 * when classification done according to the passed ClassificationMethod
	 * 
	 * Returns null when we do not have evaluation data for this datum. 
	 * 
	 * @param method
	 * @return
	 */
	public Double getEvalClassificationCost(ClassificationMethod method) {

		if (!this.isEvaluation()) return null;
		
		String from = this.getEvaluationCategory();
		
		Map<String, Double>	dest_probabilities = getProbabilityVector(method);
		
		Category fromCostVector = ds.getCategories().get(from);
		Double cost = 0.0;
		
		for (String to : dest_probabilities.keySet()) {
			Double prob = dest_probabilities.get(to);
			Double misclassification_cost = fromCostVector.getCost(to);
			cost += prob * misclassification_cost;
		}

		return cost;
	}
	
	public String getEvaluationCategory() {
		return evaluationCategory;
	}
	
	/**
	 * Returns a single class classification
	 * 
	 * @param classificationMethod
	 * @return
	 */
	public String getSingleClassClassification(ClassificationMethod method) throws IllegalArgumentException {
		switch (method) { 
			case DS_MaxLikelihood: 
			case MV_MaxLikelihood: 
				return Helper.getMaxLikelihoodLabel(getProbabilityVector(method) , ds.getCategories());
			case DS_MinCost:
			case MV_MinCost:
				return Helper.getMinCostLabel(getProbabilityVector(method) , ds.getCategories());
			default:
				throw new IllegalArgumentException("The Classification method passed ("+method.name()+") is not valid for getSingleClassClassification");
				
		}
	}


	public Map<String, Double> getProbabilityVector(ClassificationMethod method) {
		
		Map<String, Double> result = new HashMap<String, Double>();
		for (String c : this.categoryProbability.keySet()) {
			result.put(c, 0.0);
		}
		
		if (this.isGold) {
			result.put(this.goldCategory, 1.0);
			return result;
		}
		
		switch (method) { 
			case DS_MaxLikelihood:
				result.put(Helper.getMaxLikelihoodLabel(categoryProbability , ds.getCategories()), 1.0);
				return result;
			case MV_MaxLikelihood:
				result.put(Helper.getMaxLikelihoodLabel(getMV_Probability() , ds.getCategories()), 1.0);
				return result;
			case DS_MinCost:
				result.put(Helper.getMinCostLabel(categoryProbability , ds.getCategories()), 1.0);
				return result;
			case MV_MinCost:
				result.put(Helper.getMinCostLabel(getMV_Probability() , ds.getCategories()), 1.0);
				return result;
			case DS_Soft: 
				return categoryProbability;
			case MV_Soft: 
				return getMV_Probability();
			default: 
				return null;
			}
				
		}

	/**
	 * @return
	 */
	private Map<String, Double> getMV_Probability() {

		Map<String, Double> mv = new HashMap<String, Double>();
		for (String c : ds.getCategories().keySet()) {
			mv.put(c, 0.0);
		}
				
		int n = this.labels.size();		
		for (AssignedLabel al : this.labels) {
			String c = al.getCategoryName();
			Double current = mv.get(c);
			mv.put(c, current + 1.0/n);
		}
		return mv;
	}
		
	
	

	/**
	 * @return the categoryProbability
	 */
	public Double getCategoryProbability(ClassificationMethod method, String category) {

		return getProbabilityVector(method).get(category);
		
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	public Boolean isEvaluation() {
		return isEvaluation;
	}
	
	public Boolean isGold() {
		return isGold;
	}

	
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Datum o) {

		return this.name.compareTo(o.getName());
	}

	public void setCategoryProbability(String c, Double prob) {
		categoryProbability.put(c, prob);
	}

	public void setGoldCategory(String goldCategory) {
		this.goldCategory = goldCategory;
	}

	public void setEvaluation(Boolean isEvaluation) {
		this.isEvaluation = isEvaluation;
	}

	public void setEvaluationCategory(String evaluationCategory) {
		this.evaluationCategory = evaluationCategory;
	}
	
	public void setGold(Boolean isGold) {
		this.isGold = isGold;
	}

	/**
	 * @param name
	 *          the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}

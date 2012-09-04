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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ipeirotis.gal.core.Entity;
import com.ipeirotis.utils.Utils;

@SuppressWarnings("serial")
public class Datum implements Entity {
	String									name;

	// Defines if we have the correct category for this object
	// and if it is gold, the correctCategory holds the name of the correct category
	Boolean									isGold;

	Boolean									isEvaluation = false;

	String									correctCategory;

	String									evaluationCategory;

	// The probability estimates for the object belonging to different categories
	Map<String, Double>	categoryProbability;

	// The labels that have been assigned to this object, together with the workers who
	// assigned these labels. Serves mainly as a speedup, and intended to be used in
	// environments with persistence and caching (especially memcache)
	Set<AssignedLabel>			labels;
	
	public static int MV_ML = 0;
	public static int DS_ML = 1;
	public static int MV_Soft = 2;
	public static int DS_Soft = 3;
	
	private DawidSkene ds;
	
	public Boolean isGold() {
		return isGold;
	}

	public void setGold(Boolean isGold) {
		this.isGold = isGold;
	}

	public String getCorrectCategory() {
		return correctCategory;
	}

	public Boolean isEvaluation() {
		return isEvaluation;
	}

	public void setEvaluation(Boolean isEvaluation) {
		this.isEvaluation = isEvaluation;
	}

	public String getEvaluationCategory() {
		return evaluationCategory;
	}

	public void setEvaluationCategory(String evaluationCategory) {
		this.evaluationCategory = evaluationCategory;
	}

	public void setCorrectCategory(String correctCategory) {
		this.correctCategory = correctCategory;
	}

	public Double getCategoryProbability(String c) {
		if (this.isGold) {
			if (c.equals(this.correctCategory)) {
				return 1.0;
			} else {
				return 0.0;
			}
		}
		return categoryProbability.get(c);
	}

	public void setCategoryProbability(String c, Double prob) {
		categoryProbability.put(c, prob);
	}

	public Double getEntropy() {
		double[] p = new double[this.categoryProbability.size()];

		int i = 0;
		for (String c : this.categoryProbability.keySet()) {
			p[i] = getCategoryProbability(c);
			i++;
		}

		return Utils.entropy(p);
	}
	
	public Double getEvalClassificationCost(int method) {
		String from  = this.getEvaluationCategory();
		Category fromMap = ds.getCategories().get(from);
		if (method == Datum.DS_ML) {
			String to = this.getMostLikelyCategory();
			return fromMap.getCost(to);
		} else if (method == Datum.DS_Soft) {
			Double cost = 0.0;
			Map<String, Double>	probabilities = this.getCategoryProbability();
			for (String to : probabilities.keySet()) {
				Double prob = probabilities.get(to);
				Double misclassification_cost = fromMap.getCost(to);
				cost += prob * misclassification_cost;
			}
			return cost;
		} else if (method == Datum.MV_ML) {
			String to = this.getMostLikelyCategory_MV();
			
			return fromMap.getCost(to);
		} else if (method == Datum.MV_Soft) {
			Double cost = 0.0;
			Map<String, Double>	probabilities = this.getMVCategoryProbability();
			for (String to : probabilities.keySet()) {
				Double prob = probabilities.get(to);
				Double misclassification_cost = fromMap.getCost(to);
				cost += prob * misclassification_cost;
			}
			return cost;
		}
		
		return -1.0;
	}
	
	/**
	 * This class computes the expected cost of the example.
	 * 
	 * @param categories Each Category object contains the misclassification costs, so by passing this parameter, we allow the method to compute the expected misclassification cost of the object
	 *   
	 * @return 
	 */
	public Double getExpectedMVCost() {
		Map<String, Double> majorityVote = this.getMVCategoryProbability();
		return Helper.getExpectedSoftLabelCost(majorityVote, ds.getCategories());
	}
	
	public Datum(String name, DawidSkene ds) {
		this.ds = ds;

		this.name = name;
		this.isGold = false;
		this.correctCategory = null;
		this.labels = new HashSet<AssignedLabel>();

		// We initialize the probabilities vector to be uniform across categories
		this.categoryProbability = new HashMap<String, Double>();
		
		for (Category c : getCategories()) {
			this.categoryProbability.put(c.getName(), 1.0 / ds.getCategories().size());
		}
	}
	
	public DawidSkene getDs() {
		return ds;
	}
	
	private Collection<Category> getCategories() {
		return ds.getCategories().values();
	}

	public void addAssignedLabel(AssignedLabel al) {
		if (al.getObjectName().equals(name)) {
			this.labels.add(al);
		}
	}

	public Set<AssignedLabel> getAssignedLabels() {

		return this.labels;
	}

	public String getMostLikelyCategory() {

		double maxProbability = -1;
		String maxLikelihoodCategory = null;

		for (String category : this.categoryProbability.keySet()) {
			Double probability = this.categoryProbability.get(category);
			if (probability > maxProbability) {
				maxProbability = probability;
				maxLikelihoodCategory = category;
			} else if (probability == maxProbability) {
				// In case of a tie, break ties randomly
				// TODO: This is a corner case. We can also break ties
				// using the priors. But then we also need to group together
				// all the ties, and break ties probabilistically across the
				// group. Otherwise, we slightly favor the later comparisons.
				if (Math.random() > 0.5) {
					maxProbability = probability;
					maxLikelihoodCategory = category;
				}
			}
		}
		return maxLikelihoodCategory;
	}
	
	public String getMostLikelyCategory_MV() {

		double maxProbability = -1;
		String maxLikelihoodCategory = null;

		Map<String, Double> majorityVote = this.getMVCategoryProbability();
		for (String category : majorityVote.keySet()) {
			Double probability = majorityVote.get(category);
			if (probability > maxProbability) {
				maxProbability = probability;
				maxLikelihoodCategory = category;
			} else if (probability == maxProbability) {
				// In case of a tie, break ties randomly
				// TODO: This is a corner case. We can also break ties
				// using the priors. But then we also need to group together
				// all the ties, and break ties probabilistically across the
				// group. Otherwise, we slightly favor the later comparisons.
				if (Math.random() > 0.5) {
					maxProbability = probability;
					maxLikelihoodCategory = category;
				}
			}
		}

		return maxLikelihoodCategory;
	}

	

	/**
	 * @return the categoryProbability
	 */
	public Map<String, Double> getCategoryProbability() {

		return categoryProbability;
	}
	
	/**
	 * @return the categoryProbability
	 */
	public Map<String, Double> getMVCategoryProbability() {
		Map<String, Double> result = new HashMap<String, Double>();
		
		for (String c : this.categoryProbability.keySet()) {
			result.put(c, 0d);
		}
		
		int n = this.labels.size();		
		for (AssignedLabel al : this.labels) {
			String c = al.getCategoryName();
			Double current = result.get(c);
			result.put(c, current + 1.0/n);
		}
		
		return result;
	}
	
	/**
	 * @return the categoryProbability
	 */
	public Double getMVCategoryProbability(String category) {
		Map<String, Double> majorityVote = this.getMVCategoryProbability();
		return majorityVote.get(category);
		
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
}

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
import java.util.Map;


public class Helper {

	/**
	 * Gets as input a "soft label" (i.e., a distribution of probabilities over
	 * classes) and returns the expected cost of this soft label.
	 * 
	 * @param p
	 * @return The expected cost of this soft label
	 */
	public static Double getExpectedSoftLabelCost(Map<String, Double> probabilities, Map<String, Category>	categories) {

		Double c = 0.0;
		for (String c1 : probabilities.keySet()) {
			for (String c2 : probabilities.keySet()) {
				Double p1 = probabilities.get(c1);
				Double p2 = probabilities.get(c2);
				Double cost = categories.get(c1).getCost(c2);
				c += p1 * p2 * cost;
			}
		}

		return c;
	}
	
	/**
	 * Gets as input a "soft label" (i.e., a distribution of probabilities over
	 * classes) and returns the smallest possible cost for this soft label.
	 * 
	 * @param p
	 * @return The classification cost of this soft label if we classify in the class that has the minimum exp cost.
	 */
	public static Double getMinSoftLabelCost(Map<String, Double> probabilities, Map<String, Category>	categories) {

		Double min_cost = Double.NaN;

		// We know that the classification cost is minimized if we pick *one* category, as opposed to a mixture.
		// What is the category that has the lowest cost in this case?
		for (String c1 : probabilities.keySet()) {
			// So, with probability p1 the object belongs to class c1
			// Double p1 = probabilities.get(c1);
			
			Double costfor_c2 = 0.0;
			for (String c2 : probabilities.keySet()) {
				// With probability p2 it actually belongs to class c2
				Double p2 = probabilities.get(c2);
				Double cost = categories.get(c1).getCost(c2);
				costfor_c2 += p2 * cost;
			}

			if (Double.isNaN(min_cost) || costfor_c2 < min_cost) {
				min_cost = costfor_c2;
			}

		}

		return min_cost;
	}

	public static String getMinCostLabel(Map<String, Double> softLabel, Map<String, Category>	categories) {

		String result = null;
		Double min_cost = Double.MAX_VALUE;

		for (String c1 : softLabel.keySet()) {
			// So, with probability p1 it belongs to class c1
			// Double p1 = probabilities.get(c1);

			// What is the cost in this case?
			Double costfor_c1 = 0.0;
			for (String c2 : softLabel.keySet()) {
				// With probability p2 it actually belongs to class c2
				Double p2 = softLabel.get(c2);
				Double cost = categories.get(c1).getCost(c2);
				costfor_c1 += p2 * cost;

			}

			if (costfor_c1 < min_cost) {
				result = c1;
				min_cost = costfor_c1;
			}

		}

		return result;
	}	
	
	public static String getMaxLikelihoodLabel(Map<String, Double> probabilities, Map<String, Category> categories) {

		String result = null;
		double maxProbability = -1;

		for (String category : probabilities.keySet()) {
			Double probability = probabilities.get(category);
			if (probability > maxProbability) {
				maxProbability = probability;
				result = category;
			} else if (probability == maxProbability) {
				// In case of a tie, break ties randomly
				// TODO: This is a corner case. We can also break ties
				// using the priors. But then we also need to group together
				// all the ties, and break ties probabilistically across the
				// group. Otherwise, we slightly favor the later comparisons.
				if (Math.random() > 0.5) {
					maxProbability = probability;
					result = category;
				}
			}
		}

		return result;
	}
	
	
	/**
	 * Returns the minimum possible cost of a "spammer" worker, who assigns
	 * completely random labels.
	 * 
	 * @return The expected cost of a spammer worker
	 */
	public static Double getMinSpammerCost(Map<String, Category>	categories) {

		Map<String, Double> prior = new HashMap<String, Double>();
		for (Category c : categories.values()) {
			prior.put(c.getName(), c.getPrior());
		}
		return getMinSoftLabelCost(prior, categories);
	}
	

	/**
	 * Returns the cost of a "spammer" worker, who assigns completely random
	 * labels.
	 * 
	 * @return The expected cost of a spammer worker
	 */
	public static Double getSpammerCost(Map<String, Category>	categories) {

		Map<String, Double> prior = new HashMap<String, Double>();
		for (Category c : categories.values()) {
			prior.put(c.getName(), c.getPrior());
		}
		return getExpectedSoftLabelCost(prior, categories);
	}
	
}

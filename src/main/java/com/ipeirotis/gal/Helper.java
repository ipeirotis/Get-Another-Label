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
package com.ipeirotis.gal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.ipeirotis.gal.core.Category;


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
	public static Double getMinCostLabelCost(Map<String, Double> probabilities, Map<String, Category>	categories) {

		Double cost  = 0.0;

		// We know that the classification cost is minimized if we pick *one* category, as opposed to a mixture.
		// What is the category that has the lowest cost in this case?
		String to = getMinCostLabel(probabilities, categories);
		if (to == null) {
			return Double.NaN;
		}
		
			for (String from : probabilities.keySet()) {
				// With probability p it actually belongs to class from
				Double p = probabilities.get(from);
				Double c = categories.get(from).getCost(to);
				
				cost += p * c;
			}

		return cost;
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
				Double cost = categories.get(c2).getCost(c1);
				costfor_c1 += p2 * cost;

			}

			if (costfor_c1 <= min_cost) {
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
	 * Gets as input a "soft label" (i.e., a distribution of probabilities over
	 * classes) and returns the expected cost for the maximum likelihood label.
	 * 
	 * @param p
	 * @return The classification cost of this soft label if we classify in the max likelihood class
	 */
	public static Double getMaxLikelihoodCost(Map<String, Double> probabilities, Map<String, Category>	categories) {

		Double cost = 0.0;

		String to = getMaxLikelihoodLabel(probabilities, categories);
		if (to == null) {
			return Double.NaN;
		}
			
			for (String from : probabilities.keySet()) {
				// With probability p it actually belongs to class from
				Double p = probabilities.get(from);
				Double c = categories.get(from).getCost(to);
				cost += p * c;
			}

		return cost;
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
		return getMinCostLabelCost(prior, categories);
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

	public static String readFile(String FileName) {
	
		StringBuffer buffer = new StringBuffer();
	
		try {
			BufferedReader dataInput = new BufferedReader(new FileReader(new File(FileName)));
			String line;
	
			while ((line = dataInput.readLine()) != null) {
				// buffer.append(cleanLine(line.toLowerCase()));
				buffer.append(line);
				buffer.append('\n');
			}
			dataInput.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return buffer.toString();
	}

	public static Double round(double d, int decimalPlace) {
	
		// see the Javadoc about why we use a String in the constructor
		// http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
		BigDecimal bd = new BigDecimal(Double.toString(d));
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}
	
}

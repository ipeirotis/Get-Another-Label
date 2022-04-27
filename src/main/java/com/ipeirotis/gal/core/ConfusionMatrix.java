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

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import com.ipeirotis.gal.Helper;
import com.ipeirotis.gal.Stat;


class CategoryPair {

	private String from;
	private String to;

	public CategoryPair(String from, String to) {

		this.from = from;
		this.to = to;
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
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		if (!(obj instanceof CategoryPair))
			return false;
		CategoryPair other = (CategoryPair) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("from", this.from)
				.append("to", this.to).toString();
	}

}

public class ConfusionMatrix {

	private Set<String> categories;
	private HashMap<CategoryPair, Double> matrix;

	public Set<String> getCategoryNames() {
		return categories;
	}

	public ConfusionMatrix(Collection<Category> categories) {

		this.categories = new HashSet<String>();
		for (Category c : categories) {
			this.categories.add(c.getName());
		}

		this.matrix = new HashMap<CategoryPair, Double>();

		// We now initialize the confusion matrix
		// and we set it to 0.9 in the diagonal and 0.0 elsewhere
		for (String from : this.categories) {
			for (String to : this.categories) {
				if (from.equals(to)) {
					setErrorRate(from, to, 0.9);
				} else {
					setErrorRate(from, to, 0.1 / (this.categories.size() - 1));
				}
			}
		}
		this.normalize();
	}

	public void empty() {

		for (String from : this.categories) {
			for (String to : this.categories) {
				setErrorRate(from, to, 0.0);
			}
		}
	}

	/**
	 * Makes the matrix to be row-stochastic: In other words, for a given "from"
	 * category, if we sum the errors across all the "to" categories, we get 1.0
	 */
	public void normalize() {

		for (String from : this.categories) {
			double from_marginal = 0.0;
			for (String to : this.categories) {
				from_marginal += getErrorRate(from, to);
			}
			for (String to : this.categories) {
				double error = getErrorRate(from, to);
				double error_rate;

				// If the marginal across the "from" category is 0
				// this means that the worker has not even seen an object of the
				// "from"
				// category. In this case, we set the value to NaN
				if (from_marginal == 0.0) {
					error_rate = Double.NaN;
				} else {
					error_rate = error / from_marginal;
				}
				setErrorRate(from, to, error_rate);
			}
		}
	}

	/**
	 * Makes the matrix to be row-stochastic: In other words, for a given "from"
	 * category, if we sum the errors across all the "to" categories, we get
	 * 1.0.
	 * 
	 * We use Laplace smoothing
	 */
	/*
	public void normalizeLaplacean() {

		for (String from : this.categories) {
			double from_marginal = 0.0;
			for (String to : this.categories) {
				from_marginal += getErrorRate(from, to);
			}
			for (String to : this.categories) {
				double error = getErrorRate(from, to);
				setErrorRate(from, to, (error + 1)
						/ (from_marginal + this.categories.size()));
			}
		}
	}
	*/

	public void addError(String from, String to, Double error) {

		CategoryPair cp = new CategoryPair(from, to);
		Double currentError = this.matrix.get(cp);
		this.matrix.put(cp, currentError + error);
	}

	public Double getErrorRate(String from, String to) {

		CategoryPair cp = new CategoryPair(from, to);
		return matrix.get(cp);
	}

	public void setErrorRate(String from, String to, Double cost) {

		CategoryPair cp = new CategoryPair(from, to);
		matrix.put(cp, cost);
	}

	/**
	 * Given a correct label, returns probabilistically a label that can be
	 * assigned by a worker with this confusion matrix
	 * 
	 * 
	 * @param correct
	 *            The correct category for the object
	 * @return A possible assigned label for the object, assigned
	 *         probabilistically according to the confusion matrix values
	 */
	public String getAssignedLabel(String correct) {
		Double prob = Math.random();

		for (String assignedLabel : this.categories) {
			CategoryPair cp = new CategoryPair(correct, assignedLabel);
			Double p = matrix.get(cp);

			if (p > prob) {
				return assignedLabel;
			} else {
				prob -= p;
			}
		}

		// We should not really reach this point, but it can happen in case of
		// rounding errors in the error rates
		// In that case, we just call again the classification method. The
		// probability of not returning a value again
		// gets exponentially small very quickly
		return getAssignedLabel(correct);
	}
	


	/**
	 * @param priors
	 * @param draw
	 * @return
	 */
	private HashMap<String, Double> getPosterior(Map<String, Category> priors, Map<String, Integer> draw) {

		// Now compute the posterior, given the draw
		HashMap<String, Double> posterior = new  HashMap<String, Double>();
		for (String s: this.categories) {
			posterior.put(s, 0.0);
		}
		
		Double sum = 0.0;
		for (String from : this.categories) {
			double pi_from = priors.get(from).getPrior();
			double evidence_from = pi_from;
			for (String to : this.categories) {
				Integer n  = draw.get(to);
				double p = getErrorRate(from, to);
				evidence_from *= Math.pow(p, n);
			}
			posterior.put(from, evidence_from);
			sum += evidence_from;
		}
		for (String c: posterior.keySet()) {
			double existing = posterior.get(c);
			posterior.put(c, existing/sum);
		}
		return posterior;
	}

	/**
	 * Gets as input the "from" category for the object, and simulates a draw from the multinomial
	 * distribution that corresponds to that row. 
	 * 
	 * @param m
	 * @param objectCategory
	 * @return
	 */
	private HashMap<String, Integer> getRandomLabelAssignment(int m, String objectCategory) {

		Double total = 0.0;
		// Get the total sum of the corresponding row of the confusion matrix
		for (String to : this.categories) {
			total += getErrorRate(objectCategory, to);
		}
		
		HashMap<String, Integer> draw = new HashMap<String, Integer>();
		for (String s: this.categories) {
			draw.put(s, 0);
		}
		
		for (int i=0; i<m; i++) {
			// We pick now the label assigned by worker i 
			Double r = Math.random() * total;
			
			for (String to : this.categories) {
				if (r < getErrorRate(objectCategory, to)) {
					Integer existing = draw.get(to);
					draw.put(to, existing+1);
					break;
				} else {
					r -= getErrorRate(objectCategory, to);
				}
			}
		}
		
		// Double check that we assigned exactly m elements in the draw
		int sum = 0;
		for (String s : draw.keySet()) {
			sum += draw.get(s);
		}
		if (sum == m) return draw;
		else return getRandomLabelAssignment(m, objectCategory);
		
		
		
	}
	
	/** 
	 * 
	 */
	public Double getWorkerCost(int m, Map<String, Category> priors, int sample) {
		
		Double cost = 0.0;
		
		for (String objectCategory : priors.keySet()) {
			
			Double pi = priors.get(objectCategory).getPrior();

			Double c = 0.0;
			for (int i = 0; i<sample; i++) {
				Map<String, Integer> draw = getRandomLabelAssignment(m, objectCategory);
				Map<String, Double> posterior = getPosterior(priors, draw);
				c += Helper.getMinCostLabelCost(posterior, priors);
			}
			cost += pi * c / sample;
		}
		return cost;
	}
	
	public Double getWorkerWage(double qualifiedWage, double costThreshold, Map<String, Category> priors) {
		
			
		double cost;
		int m=0;
		do {
			m++;
			cost = getWorkerCost(m, priors, 1000);
			
			// If the worker is worth less than 1/100 of a qualified worker, we return a 0 payment.
			if(m>100) return 0.0;
		} while (cost > costThreshold);
		
		return qualifiedWage / m;

	}
	
	public Double getWorkerWageRegr(double qualifiedWage, double costThreshold, Map<String, Category> priors) {
		
		SimpleRegression regression = new SimpleRegression();
		
		for (int m=1; m<=41; m+=4) {
			
				double cost = getWorkerCost(m, priors, 1000);
				if (cost == 0) break;
				regression.addData(Math.log(cost), m);

		}
		
		double d = regression.predict(Math.log(costThreshold));
		
		return qualifiedWage/d;
		
	}
	
	public static void main(String[] args) {
		
	  Category a = new Category("A");
	  Category b = new Category("B");
	  
	  a.setPrior(0.5);
	  b.setPrior(0.5);
	  
	  a.setCost("A", 0.0);
	  a.setCost("B", 1.0);
	  b.setCost("A", 1.0);
	  b.setCost("B", 0.0);
	  
	  Collection<Category> categories = new HashSet<Category>();
		categories.add(a);
	  categories.add(b);
	  
	  Map<String, Category> map = new HashMap<String, Category>();
	  map.put("A", a);
	  map.put("B", b);
	  
	  ConfusionMatrix cm = new ConfusionMatrix(categories);
	  
	  // FOR TESTS: 
	  // q=1 should return 0 cost
	  // q=1 should not be affected by m
	  
	  /*
	  cm.setErrorRate("A", "A", 0.9);
	  cm.setErrorRate("A", "B", 0.1);
	  cm.setErrorRate("B", "B", 0.0);
	  cm.setErrorRate("B", "A", 1.0);
	  
	  
	  double tau=0.01;
	  double w = cm.getWorkerWage(1.0, tau, map);
	  double pwage = Double.valueOf(new DecimalFormat("#.####").format(w));
	  double pworkers = Double.valueOf(new DecimalFormat("#.####").format(1.0/w));
	  System.out.print("\t"+pwage+"\t"+pworkers);
	  */
	  
	  //double q=0.9;
	  

	  for (int Q=95; Q>=55; Q -= 5) {
	  	
	  	double q = Q/100.0;
	  	
		  cm.setErrorRate("A", "A", q);
		  cm.setErrorRate("A", "B", 1-q);
		  cm.setErrorRate("B", "B", q);
		  cm.setErrorRate("B", "A", 1-q);
		  
		  // Classification cost of a set of m workers with the confusion matrix given above 
		  /*
		  for (int m = 1; m<=40; m+=2) {
		  	System.out.print(q+"\t"+m);
		  	Double c = cm.getWorkerCost(m, map, 100*m*m);
		  	System.out.println("\t"+Math.round(100000*c)/100000.0);
		  }
		  */
		  

		  for (double tau=0.1; tau>0.0001; tau /= 1.5) {
		  	
		  	double pq = Double.valueOf(new DecimalFormat("#.##").format(q));
		  	double ptau = Double.valueOf(new DecimalFormat("#.####").format((1-tau)));
		  	System.out.print(pq+"\t"+ptau);
		  	
			  double w = cm.getWorkerWage(1.0, tau, map);
			  double pwage = Double.valueOf(new DecimalFormat("#.####").format(w));
			  double pworkers = Double.valueOf(new DecimalFormat("#.####").format(1.0/w));
			  System.out.print("\t"+pwage+"\t"+pworkers);
			  
			  double wr = cm.getWorkerWageRegr(1.0, tau, map);
			  double pwager = Double.valueOf(new DecimalFormat("#.####").format(wr));
			  double pworkersr = Double.valueOf(new DecimalFormat("#.####").format(1.0/wr));
			  System.out.print("\t"+pwager+"\t"+pworkersr);
			  System.out.println();
		  }

	  
	  
		  
	  }
	  


		
		
	}
	
}

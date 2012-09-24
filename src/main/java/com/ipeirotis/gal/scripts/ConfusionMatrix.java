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
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

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

}

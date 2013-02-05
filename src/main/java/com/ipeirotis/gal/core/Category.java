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

import java.util.HashMap;

public class Category implements Comparable<Category> {

	private String									name;

	// The prior probability for this category
	private Double									prior;

	// The misclassification cost when we classify an object of this category
	// into some other category. The HashMap key is the other category, and the Double
	// is the cost.
	private HashMap<String, Double>	misclassification_cost;

	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Category o) {

		// TODO Auto-generated method stub
		return this.name.compareTo(o.getName());
	}

	public Category(String name) {

		this.name = name;
		this.prior = -1.0;
		this.misclassification_cost = new HashMap<String, Double>();
	}

	public void setCost(String to, Double cost) {

		misclassification_cost.put(to, cost);
	}

	public Double getCost(String to) {

		return misclassification_cost.get(to);
	}

	/**
	 * @return the prior
	 */
	public Double getPrior() {

		return prior;
	}

	/**
	 * @param prior
	 *          the prior to set
	 */
	public void setPrior(Double prior) {

		assert (prior >= 0.0 && prior <= 1.0);
		this.prior = prior;
	}

	/**
	 * @param prior
	 *          the prior to set
	 */
	public boolean hasPrior() {

		return (this.prior != -1);
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
		if (!(obj instanceof Category))
			return false;
		Category other = (Category) obj;
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

package com.ipeirotis.gal.scripts;

import java.util.HashMap;

public class Category {

	private String									name;

	// The prior probability for this category
	private Double									prior;

	// The misclassification cost when we classify an object of this category
	// into some other category. The HashMap key is the other category, and the Double
	// is the cost.
	private HashMap<String, Double>	misclassification_cost;

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

		if (this.prior != -1)
			return true;
		else
			return false;
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

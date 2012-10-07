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

public class MisclassificationCost {

	private String	categoryFrom;
	private String	categoryTo;
	private Double	cost;

	public MisclassificationCost(String from, String to, Double cost) {

		this.categoryFrom = from;
		this.categoryTo = to;
		this.cost = cost;
	}

	/**
	 * @return the categoryFrom
	 */
	public String getCategoryFrom() {

		return categoryFrom;
	}

	/**
	 * @return the categoryTo
	 */
	public String getCategoryTo() {

		return categoryTo;
	}

	/**
	 * @return the cost
	 */
	public Double getCost() {

		return cost;
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
		result = prime * result + ((categoryFrom == null) ? 0 : categoryFrom.hashCode());
		result = prime * result + ((categoryTo == null) ? 0 : categoryTo.hashCode());
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
		if (!(obj instanceof MisclassificationCost))
			return false;
		MisclassificationCost other = (MisclassificationCost) obj;
		if (categoryFrom == null) {
			if (other.categoryFrom != null)
				return false;
		} else if (!categoryFrom.equals(other.categoryFrom))
			return false;
		if (categoryTo == null) {
			if (other.categoryTo != null)
				return false;
		} else if (!categoryTo.equals(other.categoryTo))
			return false;
		return true;
	}
}

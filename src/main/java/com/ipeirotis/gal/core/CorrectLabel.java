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

public class CorrectLabel implements Comparable<CorrectLabel> {

	private String	objectName;
	private String	correctCategory;

	public CorrectLabel(String objectName, String correctCategory) {

		this.objectName = objectName;
		this.correctCategory = correctCategory;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CorrectLabel o) {
		int c1 = this.objectName.compareTo(o.objectName);
		if (c1 != 0) return c1;
		
		return this.correctCategory.compareTo(o.correctCategory);
	}

	/**
	 * @return the objectName
	 */
	public String getObjectName() {

		return objectName;
	}

	/**
	 * @return the correctCategory
	 */
	public String getCorrectCategory() {

		return correctCategory;
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
		result = prime * result + ((correctCategory == null) ? 0 : correctCategory.hashCode());
		result = prime * result + ((objectName == null) ? 0 : objectName.hashCode());
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
		if (!(obj instanceof CorrectLabel))
			return false;
		CorrectLabel other = (CorrectLabel) obj;
		if (correctCategory == null) {
			if (other.correctCategory != null)
				return false;
		} else if (!correctCategory.equals(other.correctCategory))
			return false;
		if (objectName == null) {
			if (other.objectName != null)
				return false;
		} else if (!objectName.equals(other.objectName))
			return false;
		return true;
	}

}

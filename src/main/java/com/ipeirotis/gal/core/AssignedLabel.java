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

public class AssignedLabel implements Comparable<AssignedLabel> {

	private String	workerName;
	private String	objectName;
	private String	categoryName;

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AssignedLabel o) {
		int result = 0;
		
		int c1 = this.workerName.compareTo(o.workerName);
		int c2 = this.objectName.compareTo(o.objectName);
		
		if (c1 != 0) {
			result = c1;
		} else if (c2 != 0) {
			result = c2;
		} else {
			result = this.categoryName.compareTo(o.categoryName);
		}
		
		return result;
	}
	
	/**
	 * @return the workerName
	 */
	public String getWorkerName() {

		return workerName;
	}

	/**
	 * @return the objectName
	 */
	public String getObjectName() {

		return objectName;
	}

	/**
	 * @return the categoryName
	 */
	public String getCategoryName() {

		return categoryName;
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
		result = prime * result + ((objectName == null) ? 0 : objectName.hashCode());
		result = prime * result + ((workerName == null) ? 0 : workerName.hashCode());
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
		if (!(obj instanceof AssignedLabel))
			return false;
		AssignedLabel other = (AssignedLabel) obj;
		if (objectName == null) {
			if (other.objectName != null)
				return false;
		} else if (!objectName.equals(other.objectName))
			return false;
		if (workerName == null) {
			if (other.workerName != null)
				return false;
		} else if (!workerName.equals(other.workerName))
			return false;
		return true;
	}

	public AssignedLabel(String w, String d, String c) {

		this.workerName = w;
		this.objectName = d;
		this.categoryName = c;
	}

	public String toString() {

		return this.workerName + "\t" + this.objectName + "\t" +  this.categoryName; 
	}
	
}

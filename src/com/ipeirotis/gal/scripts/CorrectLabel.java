package com.ipeirotis.gal.scripts;

public class CorrectLabel {
	private String objectName;
	private String correctCategory;
	
	public CorrectLabel(String objectName, String correctCategory) {
		this.objectName = objectName;
		this.correctCategory = correctCategory;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((correctCategory == null) ? 0 : correctCategory.hashCode());
		result = prime * result
				+ ((objectName == null) ? 0 : objectName.hashCode());
		return result;
	}

	/* (non-Javadoc)
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

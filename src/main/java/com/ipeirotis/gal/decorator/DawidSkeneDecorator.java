package com.ipeirotis.gal.decorator;

import java.util.Map;
import java.util.Set;

import com.ipeirotis.gal.algorithms.DawidSkene;
import com.ipeirotis.gal.core.ConfusionMatrix;
import com.ipeirotis.gal.core.Datum;
import com.ipeirotis.gal.core.Datum.ClassificationMethod;

public class DawidSkeneDecorator extends Decorator<DawidSkene> {

	public DawidSkeneDecorator(DawidSkene wrapped) {

		super(wrapped);
	}

	public ConfusionMatrix getEstimatedConfusionMatrix(ClassificationMethod clasMethod) {

		ConfusionMatrix confMatrix = new ConfusionMatrix(object.getCategories().values());

		confMatrix.empty();
		for (Datum d : object.getObjects().values()) {
			populateConfusionMatrix(clasMethod, confMatrix, d);
		}
		confMatrix.normalize();

		return confMatrix;
	}

	/**
	 * @param clasMethod
	 * @param confMatrix
	 * @param d
	 */
	private void populateConfusionMatrix(ClassificationMethod clasMethod, ConfusionMatrix confMatrix, Datum d) {

		Set<String> categories = object.getCategories().keySet();
		for (String fromCategory : categories) {
			Double fromProb = d.getCategoryProbability(clasMethod, fromCategory);

			ClassificationMethod probEstimateMethod = getProbabilityEstimationMethod(clasMethod);
			Map<String, Double> toClassifiedAs = d.getProbabilityVector(probEstimateMethod);

			// Both from and to are probabilistic, so we have two nested loops and we 
			// compute the estimated confusion matrix here.
			for (String toCategory : categories) {
				Double toProb = toClassifiedAs.get(toCategory);
				confMatrix.addError(fromCategory, toCategory, toProb * fromProb);
			}
		}
	}

	/**
	 * @param clasMethod
	 * @return
	 */
	private ClassificationMethod getProbabilityEstimationMethod(ClassificationMethod clasMethod) {

		// If we want to report the estimated confusion matrix
		// for the DS_* techniques the "from" probability estimate
		// is the one returned by DS_Soft, and the "to" is wherever 
		// the classification method puts the item. 
		ClassificationMethod probEstimateMethod;
		if (clasMethod.name().matches("DS_.+")) {
			probEstimateMethod = ClassificationMethod.DS_Soft;
		} else if (clasMethod.name().matches("MV_.+")) {
			probEstimateMethod = ClassificationMethod.MV_Soft;
		} else {
			throw new IllegalArgumentException("Incorrect Classification Method");
		}
		return probEstimateMethod;
	}

	public ConfusionMatrix getConfusionMatrix(ClassificationMethod clasMethod) {

		ConfusionMatrix confMatrix = new ConfusionMatrix(object.getCategories().values());

		confMatrix.empty();

		for (Datum d : object.getObjects().values()) {
			if (!d.isEvaluation())
				continue;

			String fromCategory = d.getEvaluationCategory();
			Map<String, Double> toClassifiedAs = d.getProbabilityVector(clasMethod);

			for (String toCategory : toClassifiedAs.keySet()) {
				Double prob = toClassifiedAs.get(toCategory);

				confMatrix.addError(fromCategory, toCategory, prob);
			}
		}

		confMatrix.normalize();

		return confMatrix;
	}

}

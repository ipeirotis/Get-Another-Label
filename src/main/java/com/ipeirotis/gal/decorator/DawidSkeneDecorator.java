package com.ipeirotis.gal.decorator;

import java.util.Map;

import com.ipeirotis.gal.scripts.ConfusionMatrix;
import com.ipeirotis.gal.scripts.Datum;
import com.ipeirotis.gal.scripts.Datum.ClassificationMethod;
import com.ipeirotis.gal.scripts.DawidSkene;

public class DawidSkeneDecorator extends Decorator<DawidSkene> {
	public DawidSkeneDecorator(DawidSkene wrapped) {
		super(wrapped);
	}

	public ConfusionMatrix getEstimatedConfusionMatrix(
			ClassificationMethod clasMethod) {
		ConfusionMatrix confMatrix = new ConfusionMatrix(object.getCategories()
				.values());

		confMatrix.empty();

		for (Datum d : object.getObjects().values()) {
			for (String fromCategory : d.getProbabilityVector(clasMethod)
					.keySet()) {

				Double fromProb = d.getCategoryProbability(clasMethod, fromCategory);

				Map<String, Double> toClassifiedAs = d
						.getProbabilityVector(clasMethod);

				for (String toCategory : toClassifiedAs.keySet()) {
					Double toProb = toClassifiedAs.get(toCategory);

					confMatrix.addError(fromCategory, toCategory, toProb
							* fromProb);
				}
			}
		}

		confMatrix.normalize();

		return confMatrix;
	}

	public ConfusionMatrix getConfusionMatrix(ClassificationMethod clasMethod) {
		ConfusionMatrix confMatrix = new ConfusionMatrix(object.getCategories()
				.values());

		confMatrix.empty();

		for (Datum d : object.getObjects().values()) {
			if (!d.isEvaluation())
				continue;

			String fromCategory = d.getEvaluationCategory();
			Map<String, Double> toClassifiedAs = d
					.getProbabilityVector(clasMethod);

			for (String toCategory : toClassifiedAs.keySet()) {
				Double prob = toClassifiedAs.get(toCategory);

				confMatrix.addError(fromCategory, toCategory, prob);
			}
		}

		confMatrix.normalize();

		return confMatrix;
	}

}

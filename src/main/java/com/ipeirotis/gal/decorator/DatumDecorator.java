package com.ipeirotis.gal.decorator;

import com.ipeirotis.gal.scripts.Datum;
import com.ipeirotis.gal.scripts.Datum.ClassificationMethod;
import com.ipeirotis.gal.scripts.Helper;

public class DatumDecorator extends Decorator<Datum> {
	public DatumDecorator(Datum wrapped) {
		super(wrapped);
	}
	
	public String getMostLikelyCategory() {
		return object.getSingleClassClassification(ClassificationMethod.DS_MaxLikelihood);
	}
	
	public String getMostLikelyCategory_MV() {
		return object.getSingleClassClassification(ClassificationMethod.MV_MaxLikelihood);
	}

	public Double getEvalClassificationCostForDSSoft() {
		return object.getEvalClassificationCost(Datum.ClassificationMethod.DS_Soft);
	}

	public Double getEvalClassificationCostForMVSoft() {
		return object.getEvalClassificationCost(Datum.ClassificationMethod.MV_Soft);
	}

	public Double getEvalClassificationCostForDSML() {
		return object.getEvalClassificationCost(Datum.ClassificationMethod.DS_MaxLikelihood);
	}

	public Double getEvalClassificationCostForMVML() {
		return object.getEvalClassificationCost(Datum.ClassificationMethod.MV_MaxLikelihood);
	}
	
	public Double getSpammerCost() {
		// TODO: Move it to a DS Decorator
		return Helper.getSpammerCost(object.getDs().getCategories());
	}
	
	public Double getMinSpammerCost() {
		// TODO: Move it to a DS Decorator
		return Helper.getMinSpammerCost(object.getDs().getCategories());
	}

	public Double getExpectedCost() {
		return Helper.getExpectedSoftLabelCost(object.getProbabilityVector(ClassificationMethod.DS_Soft), object.getDs().getCategories());
	}
	
	public Double getExpectedMVCost() {
		return object.getExpectedMVCost();
	}
	
	public Double getMinCost() {
		return Helper.getMinSoftLabelCost(object.getProbabilityVector(ClassificationMethod.DS_Soft), object.getDs().getCategories());
	}
	
	public Double getMinMVCost() {
		return Helper.getMinSoftLabelCost(object.getProbabilityVector(ClassificationMethod.MV_Soft), object.getDs().getCategories());
	}
	
	public Double getDataQualityForDS() {
		return 1 - getExpectedCost() / getSpammerCost();
	}
	
	public Double getDataQualityForMV() {
		return 1 - getExpectedMVCost() / getSpammerCost();
	}
	
	public Double getDataQualityForDSOpt() {
		return 1 - getMinCost() / getMinSpammerCost();
	}

	public Double getDataQualityForMVOpt() {
		return 1 - getMinMVCost() / getMinSpammerCost();
	}
	
	public Double getEvalDataQualityForDSML() {
		return 1 - getEvalClassificationCostForDSML() / getMinSpammerCost();
	}

	public Double getEvalDataQualityForDSSoft() {
		return 1 - getEvalClassificationCostForDSSoft() / getMinSpammerCost();
	}

	public Double getEvalDataQualityForMVML() {
		return 1 - getEvalClassificationCostForMVML() / getMinSpammerCost();
	}

	public Double getEvalDataQualityForMVSoft() {
		return 1 - getEvalClassificationCostForMVSoft() / getMinSpammerCost();
	}
}

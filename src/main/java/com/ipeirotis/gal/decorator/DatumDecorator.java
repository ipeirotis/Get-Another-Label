package com.ipeirotis.gal.decorator;

import com.ipeirotis.gal.Helper;
import com.ipeirotis.gal.core.Datum;
import com.ipeirotis.gal.core.Datum.ClassificationMethod;

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
		return Helper.getExpectedSoftLabelCost(object.getProbabilityVector(ClassificationMethod.MV_Soft), object.getDs().getCategories());
	}
	
	public Double getDSMLCost() {
		return Helper.getMaxLikelihoodCost(object.getProbabilityVector(ClassificationMethod.DS_Soft), object.getDs().getCategories());
	}
	
	public Double getMVMLCost() {
		return Helper.getMaxLikelihoodCost(object.getProbabilityVector(ClassificationMethod.MV_Soft), object.getDs().getCategories());
	}
	
	public Double getMinCost() {
		return Helper.getMinCostLabelCost(object.getProbabilityVector(ClassificationMethod.DS_Soft), object.getDs().getCategories());
	}
	
	public Double getMinMVCost() {
		return Helper.getMinCostLabelCost(object.getProbabilityVector(ClassificationMethod.MV_Soft), object.getDs().getCategories());
	}
	
	public String getMinCostCategory_MV() {
		return object.getSingleClassClassification(ClassificationMethod.MV_MinCost);
	}

	public String getMinCostCategory_DS() {
		return object.getSingleClassClassification(ClassificationMethod.DS_MinCost);
	}
	
	public Double getEvalCostMVMin() {
		return object.getEvalClassificationCost(ClassificationMethod.MV_MinCost);
	}

	public Double getEvalCostDSMin() {
		return object.getEvalClassificationCost(ClassificationMethod.DS_MinCost);
	}
	
	public Double getDataQualityForDS() {
		return 1 - getExpectedCost() / getMinSpammerCost();
	}
	
	public Double getDataQualityForMV() {
		return 1 - getExpectedMVCost() / getMinSpammerCost();
	}
	
	public Double getDataQualityForDSOpt() {
		return 1 - getMinCost() / getMinSpammerCost();
	}

	public Double getDataQualityForMVOpt() {
		return 1 - getMinMVCost() / getMinSpammerCost();
	}
	
	public Double getDataQualityForDSML() {
		return 1 - getDSMLCost() / getMinSpammerCost();
	}

	public Double getDataQualityForMVML() {
		return 1 - getMVMLCost() / getMinSpammerCost();
	}
	
	public Double getEvalDataQualityForDSML() {
		if (! object.isEvaluation())
			return null;
		
		return 1 - getEvalClassificationCostForDSML() / getMinSpammerCost();
	}

	public Double getEvalDataQualityForDSSoft() {
		if (! object.isEvaluation())
			return null;

		return 1 - getEvalClassificationCostForDSSoft() / getMinSpammerCost();
	}

	public Double getEvalDataQualityForMVML() {
		if (! object.isEvaluation())
			return null;
		
		return 1 - getEvalClassificationCostForMVML() / getMinSpammerCost();
	}

	public Double getEvalDataQualityForMVSoft() {
		if (! object.isEvaluation())
			return null;

		return 1 - getEvalClassificationCostForMVSoft() / getMinSpammerCost();
	}
	
	public Double getEvalDataQualityForMVMinCost() {
		if (! object.isEvaluation())
			return null;
		
		return 1 - getEvalCostMVMin() / getMinSpammerCost();
	}

	public Double getEvalDataQualityForDSMinCost() {
		if (! object.isEvaluation())
			return null;
		
		return 1 - getEvalCostDSMin() / getMinSpammerCost();
	}

}

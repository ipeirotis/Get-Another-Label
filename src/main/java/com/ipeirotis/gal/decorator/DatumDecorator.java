package com.ipeirotis.gal.decorator;

import com.ipeirotis.gal.scripts.Datum;
import com.ipeirotis.gal.scripts.Helper;

public class DatumDecorator extends Decorator {
	public DatumDecorator(Object object) {
		super(object);
	}

	public Double getEvalClassificationCostForDSSoft() {
		return ((Datum) object).getEvalClassificationCost(Datum.DS_Soft);
	}

	public Double getEvalClassificationCostForMVSoft() {
		return ((Datum) object).getEvalClassificationCost(Datum.MV_Soft);
	}

	public Double getEvalClassificationCostForDSML() {
		return ((Datum) object).getEvalClassificationCost(Datum.DS_ML);
	}

	public Double getEvalClassificationCostForMVML() {
		return ((Datum) object).getEvalClassificationCost(Datum.MV_ML);
	}
	
	public Double getSpammerCost() {
		// TODO: Move it to a DS Decorator
		return Helper.getSpammerCost(((Datum) object).getDs().getCategories());
	}
	
	public Double getMinSpammerCost() {
		// TODO: Move it to a DS Decorator
		return Helper.getMinSpammerCost(((Datum) object).getDs().getCategories());
	}

	public Double getExpectedCost() {
		return Helper.getExpectedSoftLabelCost(((Datum) object).getCategoryProbability(), ((Datum) object).getDs().getCategories());
	}
	
	public Double getExpectedMVCost() {
		return ((Datum) object).getExpectedMVCost();
	}
	
	public Double getMinCost() {
		return Helper.getMinSoftLabelCost(((Datum) object).getCategoryProbability(), ((Datum) object).getDs().getCategories());
	}
	
	public Double getMinMVCost() {
		return Helper.getMinSoftLabelCost(((Datum) object).getMVCategoryProbability(), ((Datum) object).getDs().getCategories());
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

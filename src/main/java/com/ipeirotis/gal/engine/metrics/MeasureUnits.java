package com.ipeirotis.gal.engine.metrics;

public class MeasureUnits {
	public static final MeasureUnit DS_Exp_Cost = new MeasureUnit(
			"DS_Exp_Cost",
			"Expected misclassification cost (for EM algorithm)");

	public static final MeasureUnit MV_Exp_Cost = new MeasureUnit(
			"MV_Exp_Cost",
			"Expected misclassification cost (for Majority Voting algorithm)");

	public static final MeasureUnit NoVote_Exp_Cost = new MeasureUnit(
			"NoVote_Exp_Cost",
			"Expected misclassification cost (random classification)");

	public static final MeasureUnit DS_Opt_Cost = new MeasureUnit(
			"DS_Opt_Cost",
			"Minimized misclassification cost (for EM algorithm)");

	public static final MeasureUnit MV_Opt_Cost = new MeasureUnit(
			"MV_Opt_Cost",
			"Minimized misclassification cost (for Majority Voting algorithm)");

	public static final MeasureUnit NoVote_Opt_Cost = new MeasureUnit(
			"NoVote_Opt_Cost",
			"Minimized misclassification cost (random classification)");
	
	public static final MeasureUnit Eval_Cost_MV_ML = new MeasureUnit(
			"Eval_Cost_MV_ML",
			"Classification cost for single-class classification, using Majority Voting (evaluation data)"
			);
	
	public static final MeasureUnit Eval_Cost_DS_ML = new MeasureUnit(
			"Eval_Cost_DS_ML",
			"Classification cost for single-class classification, using EM (evaluation data)"
			);

	public static final MeasureUnit Eval_Cost_MV_Soft = new MeasureUnit(
			"Eval_Cost_MV_Soft",
			"Classification cost for naive soft-label classification (evaluation data)"
			);

	public static final MeasureUnit Eval_Cost_DS_Soft = new MeasureUnit(
			"Eval_Cost_DS_Soft",
			"Classification cost for soft-label classification, using EM (evaluation data)"
			);
}

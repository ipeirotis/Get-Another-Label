package com.ipeirotis.gal.decorator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import com.ipeirotis.gal.Helper;
import com.ipeirotis.gal.algorithms.DawidSkene;
import com.ipeirotis.gal.core.Datum;
import com.ipeirotis.gal.core.Worker;

public class FieldAccessors {
	public static abstract class FieldAccessor {
		String name;

		public String getName() {
			return name;
		}

		String desc;

		public String getDesc() {
			return desc;
		}

		public abstract Object getValue(Object wrapped);

		FieldAccessor(String name) {
			this.name = this.desc = name;
		}

		FieldAccessor(String name, String desc) {
			this.name = name;
			this.desc = desc;
		}

		public String getStringValue(Object wrapped) {
			return ObjectUtils.toString(getValue(wrapped));
		}

		boolean averaged;

		public boolean isAveraged() {
			return averaged;
		}

		String summaryDescription;

		public String getSummaryDescription() {
			return summaryDescription;
		}

		MetricsFormatter formatter = null;
		
		public Integer getWeight(Object o) {
			if (name.startsWith("weighted")) {
				return ((Worker) o).getAssignedLabels().size();
			}
			
			return 1;
		}

		public MetricsFormatter getFormatter() {
			return formatter;
		}

		public void setFormatter(MetricsFormatter formatter) {
			this.formatter = formatter;
		}
	}

	public static class DecoratorFieldAccessor extends FieldAccessor {
		private Class<?> decoratorClass;

		public DecoratorFieldAccessor(String name, Class<?> decoratorClass) {
			super(name);
			this.decoratorClass = decoratorClass;
		}

		public DecoratorFieldAccessor(String name, String desc,
				Class<?> decoratorClass) {
			super(name, desc);
			this.decoratorClass = decoratorClass;
		}

		@Override
		public Object getValue(Object wrapped) {
			try {
				Constructor<?> ctor = decoratorClass.getConstructors()[0];
				Decorator<?> decorator = (Decorator<?>) ctor
						.newInstance(wrapped);

				Object result = decorator.lookupObject(name);

				return result;
			} catch (Exception exc) {
				throw new RuntimeException(exc);
			}
		}
	}

	public static class EntityFieldAccessor extends DecoratorFieldAccessor {
		public EntityFieldAccessor(String name, String desc) {
			super(name, desc, DatumDecorator.class);
		}

		public EntityFieldAccessor(String name, String desc,
				Class<?> decoratorClass) {
			super(name, desc, decoratorClass);
		}

		public EntityFieldAccessor withSummaryAveraged(String summaryDescription) {
			this.summaryDescription = summaryDescription;
			this.averaged = true;

			return this;
		}
	}

	public static class CategoryDatumFieldAccessor extends EntityFieldAccessor {
		private String c;

		CategoryDatumFieldAccessor(String c) {
			super(String.format("DS_PR[%s]", c), String.format("DS_Pr[%s]", c));

			this.c = c;
			setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			withSummaryAveraged(String.format(
					"DS estimate for prior probability of category %s", c));
		}

		@Override
		public Object getValue(Object wrapped) {
			return ((Datum) wrapped).getCategoryProbability(
					Datum.ClassificationMethod.DS_Soft, c);
		}
	}

	public static class MVCategoryDatumFieldAccessor extends
			EntityFieldAccessor {
		private String c;

		MVCategoryDatumFieldAccessor(String c) {
			super(String.format("MV_PR[%s]", c), String.format("MV_Pr[%s]", c));

			this.c = c;
			setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			withSummaryAveraged(String
					.format("Majority Vote estimate for prior probability of category %s",
							c));
		}

		@Override
		public Object getValue(Object wrapped) {
			return ((Datum) wrapped).getCategoryProbability(
					Datum.ClassificationMethod.MV_Soft, c);
		}
	}

	public static class EvalDatumFieldAccessor extends EntityFieldAccessor {
		public EvalDatumFieldAccessor(String name, String desc) {
			super(name, desc);
		}

		@Override
		public String getStringValue(Object _wrapped) {
			Datum wrapped = (Datum) _wrapped;

			if (wrapped.isEvaluation()) {
				Object v = super.getValue(wrapped);

				if (v instanceof Double) {
					Double value = (Double) v;

					return Double.toString(Helper.round(value, 3));
				} else if (v != null) {
					return ObjectUtils.toString(v);
				}

			}

			return "---";
		}
	}

	public static final class DATUM_ACCESSORS {
		public static final//
		EntityFieldAccessor NAME = new EntityFieldAccessor("name", "Object");

		public static final EntityFieldAccessor//
		CORRECT_CATEGORY = new EvalDatumFieldAccessor("evaluationCategory",
				"Correct_Category");
		
		public static final//
		EntityFieldAccessor DS_CATEGORY = new EntityFieldAccessor(
				"mostLikelyCategory", "DS_MaxLikelihood_Category");

		public static final//
		EntityFieldAccessor MV_CATEGORY = new EntityFieldAccessor(
				"mostLikelyCategory_MV", "MV_MaxLikelihood_Category");

		public static final//
		EntityFieldAccessor MV_MINCOST = new EntityFieldAccessor(
				"minCostCategory_MV", "MV_MinCost_Category");

		public static final//
		EntityFieldAccessor DS_MINCOST = new EntityFieldAccessor(
				"minCostCategory_DS", "DS_MinCost_Category");

		public static final//
		EntityFieldAccessor DS_EXP_COST = new EntityFieldAccessor(
				"expectedCost", "DataCost_Estm_DS_Exp"){
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Estimated classification cost (DS_Exp metric)");

		public static final//
		EntityFieldAccessor MV_EXP_COST = new EntityFieldAccessor(
				"expectedMVCost", "DataCost_Estm_MV_Exp"){
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Estimated classification cost (MV_Exp metric)");

		public static final//
		EntityFieldAccessor NOVOTE_EXP_COST = new EntityFieldAccessor(
				"spammerCost", "DataCost_Estm_NoVote_Exp"){
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Baseline classification cost (random spammer)");

		public static final//
		EntityFieldAccessor DS_MIN_COST = new EntityFieldAccessor("minCost",
				"DataCost_Estm_DS_Min"){
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Estimated classification cost (DS_Min metric)");

		public static final//
		EntityFieldAccessor MV_MIN_COST = new EntityFieldAccessor("minMVCost",
				"DataCost_Estm_MV_Min"){
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Estimated classification cost (MV_Min metric)");
		

		public static final//
		EntityFieldAccessor DS_ML_COST = new EntityFieldAccessor("DSMLCost",
				"DataCost_Estm_DS_ML"){
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Estimated classification cost (DS_ML metric)");

		public static final//
		EntityFieldAccessor MV_ML_COST = new EntityFieldAccessor("MVMLCost",
				"DataCost_Estm_MV_ML"){
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Estimated classification cost (MV_ML metric)");
		

		
		
		
		public static final//
		EntityFieldAccessor NOVOTE_MIN_COST = new EntityFieldAccessor(
				"minSpammerCost", "DataCost_Estm_NoVote_Min"){
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Baseline classification cost (strategic spammer)");



		// Data Quality

		public static final//
		EntityFieldAccessor DATAQUALITY_DS = new EntityFieldAccessor(
				"dataQualityForDS", "DataQuality_Estm_DS_Exp") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Estimated data quality, EM algorithm, soft label");

		public static final//
		EntityFieldAccessor DATAQUALITY_MV = new EntityFieldAccessor(
				"dataQualityForMV", "DataQuality_Estm_MV_Exp") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Estimated data quality, naive soft label");

		public static final//
		EntityFieldAccessor DATAQUALITY_DS_OPT = new EntityFieldAccessor(
				"dataQualityForDSOpt", "DataQuality_Estm_DS_Min") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Estimated data quality, EM algorithm, mincost");

		public static final//
		EntityFieldAccessor DATAQUALITY_MV_OPT = new EntityFieldAccessor(
				"dataQualityForMVOpt", "DataQuality_Estm_MV_Min") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Estimated data quality, naive mincost label");
		
		public static final//
		EntityFieldAccessor DATAQUALITY_DS_ML = new EntityFieldAccessor(
				"dataQualityForDSML", "DataQuality_Estm_DS_ML") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Estimated data quality, EM algorithm, maximum likelihood");

		public static final//
		EntityFieldAccessor DATAQUALITY_MV_ML = new EntityFieldAccessor(
				"dataQualityForMVML", "DataQuality_Estm_MV_ML") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Estimated data quality, naive majority label");
		

		// Eval

		public static final EntityFieldAccessor//
		EVAL_COST_MV_ML = new EvalDatumFieldAccessor(
				"evalClassificationCostForMVML", "DataCost_Eval_MV_ML"){
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Actual classification cost for majority vote classification");

		public static final EntityFieldAccessor//
		EVAL_COST_DS_ML = new EvalDatumFieldAccessor(
				"evalClassificationCostForDSML", "DataCost_Eval_DS_ML"){
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Actual classification cost for EM, maximum likelihood classification");

		public static final EntityFieldAccessor//
		EVAL_COST_MV_SOFT = new EvalDatumFieldAccessor(
				"evalClassificationCostForMVSoft", "DataCost_Eval_MV_Soft"){
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Actual classification cost for naive soft-label classification");

		public static final EntityFieldAccessor//
		EVAL_COST_DS_SOFT = new EvalDatumFieldAccessor(
				"evalClassificationCostForDSSoft", "DataCost_Eval_DS_Soft"){
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Actual classification cost for EM, soft-label classification");

		public static final EntityFieldAccessor//
		EVAL_COST_MV_MIN = new EntityFieldAccessor(
				"evalCostMVMin", "DataCost_Eval_MV_Min"){
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Actual classification cost for naive min-cost classification");

		public static final	EntityFieldAccessor//
		EVAL_COST_DS_MIN = new EntityFieldAccessor(
				"evalCostDSMin", "DataCost_Eval_DS_Min"){
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Actual classification cost for EM, min-cost classification");

		
		public static final EntityFieldAccessor//
		DATAQUALITY_EVAL_COST_DS_ML = new EvalDatumFieldAccessor(
				"evalDataQualityForDSML", "DataQuality_Eval_DS_ML") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Actual data quality, EM algorithm, maximum likelihood");

		public static final EntityFieldAccessor//
		DATAQUALITY_EVAL_COST_DS_MINCOST = new EvalDatumFieldAccessor(
				"evalDataQualityForDSMinCost", "DataQuality_Eval_DS_Min") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Actual data quality, EM algorithm, mincost");
		
		
		public static final EntityFieldAccessor//
		DATAQUALITY_EVAL_COST_DS_SOFT = new EvalDatumFieldAccessor(
				"evalDataQualityForDSSoft", "DataQuality_Eval_DS_Soft") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Actual data quality, EM algorithm, soft label");

		public static final EntityFieldAccessor//
		DATAQUALITY_EVAL_COST_MV_ML = new EvalDatumFieldAccessor(
				"evalDataQualityForMVML", "DataQuality_Eval_MV_ML") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Actual data quality, naive majority label");
		
		public static final EntityFieldAccessor//
		DATAQUALITY_EVAL_COST_MV_MINCOST = new EvalDatumFieldAccessor(
				"evalDataQualityForMVMinCost", "DataQuality_Eval_MV_Min") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Actual data quality, naive mincost label");

		public static final EntityFieldAccessor//
		DATAQUALITY_EVAL_COST_MV_SOFT = new EvalDatumFieldAccessor(
				"evalDataQualityForMVSoft", "DataQuality_Eval_MV_Soft") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Actual data quality, naive soft label");

		public static Collection<FieldAccessor> getFieldAccessors(DawidSkene ds) {
			List<FieldAccessor> result = new ArrayList<FieldAccessor>();

			result.add(NAME);

			result.add(CORRECT_CATEGORY);
			result.add(DS_CATEGORY);
			result.add(MV_CATEGORY);
			result.add(DS_MINCOST);
			result.add(MV_MINCOST);

			for (String c : ds.getCategories().keySet())
				result.add(new CategoryDatumFieldAccessor(c));

			
			for (String c : ds.getCategories().keySet())
				result.add(new MVCategoryDatumFieldAccessor(c));


			result.add(DS_EXP_COST);
			result.add(MV_EXP_COST);
			result.add(DS_ML_COST);
			result.add(MV_ML_COST);
			result.add(DS_MIN_COST);
			result.add(MV_MIN_COST);
			
			result.add(NOVOTE_EXP_COST);
			result.add(NOVOTE_MIN_COST);


			result.add(EVAL_COST_DS_ML);
			result.add(EVAL_COST_MV_ML);
			result.add(EVAL_COST_DS_MIN);
			result.add(EVAL_COST_MV_MIN);
			result.add(EVAL_COST_DS_SOFT);
			result.add(EVAL_COST_MV_SOFT);
			
			result.add(DATAQUALITY_DS_ML);
			result.add(DATAQUALITY_MV_ML);
			result.add(DATAQUALITY_DS);
			result.add(DATAQUALITY_MV);
			result.add(DATAQUALITY_DS_OPT);
			result.add(DATAQUALITY_MV_OPT);
			

			result.add(DATAQUALITY_EVAL_COST_DS_ML);
			result.add(DATAQUALITY_EVAL_COST_MV_ML);
			result.add(DATAQUALITY_EVAL_COST_DS_MINCOST);
			result.add(DATAQUALITY_EVAL_COST_MV_MINCOST);
			result.add(DATAQUALITY_EVAL_COST_DS_SOFT);
			result.add(DATAQUALITY_EVAL_COST_MV_SOFT);

			return result;
		}
	}

	public static final class WORKER_ACCESSORS {
		public static final//
		EntityFieldAccessor NAME = new EntityFieldAccessor("name", "Worker", WorkerDecorator.class);

		public static final EntityFieldAccessor EST_QUALITY_EXP = new EntityFieldAccessor(
				"expectedCost", "WorkerQuality_Estm_DS_Exp_n",
				WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Estimated worker quality (non-weighted, DS_Exp metric)");
		
		public static final EntityFieldAccessor EST_QUALITY_ML = new EntityFieldAccessor(
				"maxLikelihoodCost", "WorkerQuality_Estm_DS_ML_n",
				WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Estimated worker quality (non-weighted, DS_ML metric)");
		
		public static final EntityFieldAccessor EST_QUALITY_ML_W = new EntityFieldAccessor(
				"weightedMaxLikelihoodCost", "WorkerQuality_Estm_DS_ML_w",
				WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Estimated worker quality (weighted, DS_ML metric)");

		public static final EntityFieldAccessor EST_QUALITY_OPT = new EntityFieldAccessor(
				"minCost", "WorkerQuality_Estm_DS_Min_n", WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Estimated worker quality (non-weighted, DS_Min metric)");
		
		public static final EntityFieldAccessor EVAL_QUALITY_EXP = new EntityFieldAccessor(
				"expCostEval", "WorkerQuality_Eval_DS_Exp_n", WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}
		}.withSummaryAveraged("Actual worker quality (non-weighted, DS_Exp metric)");
		
		public static final EntityFieldAccessor EVAL_QUALITY_ML = new EntityFieldAccessor(
				"maxLikelihoodCostEval", "WorkerQuality_Eval_DS_ML_n", WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}
		}.withSummaryAveraged("Actual worker quality (non-weighted, DS_ML metric)");
		
		public static final EntityFieldAccessor EVAL_QUALITY_ML_W = new EntityFieldAccessor(
				"weightedMaxLikelihoodCostEval", "WorkerQuality_Eval_DS_ML_w", WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}
		}.withSummaryAveraged("Actual worker quality (weighted, DS_ML metric)");

		
		
		public static final EntityFieldAccessor EVAL_QUALITY_OPT = new EntityFieldAccessor(
				"minCostEval", "WorkerQuality_Eval_DS_Min_n",
				WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Actual worker quality (non-weighted, DS_Min metric)");
		
		public static final//
		EntityFieldAccessor EST_QUALITY_EXP_W = new EntityFieldAccessor(
				"weightedQualityForEstQualityExp",
				"WorkerQuality_Estm_DS_Exp_w", WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}
		}.withSummaryAveraged("Estimated worker quality (weighted, DS_Exp metric)");

		public static final//
		EntityFieldAccessor EST_QUALITY_OPT_W = new EntityFieldAccessor(
				"weightedQualityForEstQualityOpt",
				"WorkerQuality_Estm_DS_Min_w", WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}
		}.withSummaryAveraged("Estimated worker quality (weighted, DS_Min metric)");

		public static final//
		EntityFieldAccessor EVAL_QUALITY_EXP_W = new EntityFieldAccessor(
				"weightedQualityForEvalQualityExp",
				"WorkerQuality_Eval_DS_Exp_w",
				WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}
		}.withSummaryAveraged("Actual worker quality (weighted, DS_Exp metric)");

		public static final//
		EntityFieldAccessor EVAL_QUALITY_OPT_W = new EntityFieldAccessor(
				"weightedQualityForEvalQualityOpt",
				"WorkerQuality_Eval_DS_Min_w",
				WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}
		}.withSummaryAveraged("Actual worker quality (weighted, DS_Min metric)");

		public static final EntityFieldAccessor COUNT_ANNOTATION = new EntityFieldAccessor(
				"numContributions", "Number of labels",
				WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Labels per worker");

		public static final EntityFieldAccessor COUNT_GOLD_TEST = new EntityFieldAccessor(
				"numGoldTests", "Gold Tests", WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.DECIMAL_FORMATTER);
			}
		}.withSummaryAveraged("Gold tests per worker");

		public static Collection<FieldAccessor> getFieldAccessors(DawidSkene ds) {
			List<FieldAccessor> result = new ArrayList<FieldAccessor>();

			result.add(NAME);

			result.add(EST_QUALITY_EXP);
			result.add(EST_QUALITY_EXP_W);

			result.add(EST_QUALITY_ML);
			result.add(EST_QUALITY_ML_W);

			result.add(EST_QUALITY_OPT);
			result.add(EST_QUALITY_OPT_W);
			
			result.add(EVAL_QUALITY_EXP);
			result.add(EVAL_QUALITY_EXP_W);
			
			result.add(EVAL_QUALITY_ML);
			result.add(EVAL_QUALITY_ML_W);
		
			result.add(EVAL_QUALITY_OPT);
			result.add(EVAL_QUALITY_OPT_W);
			
			result.add(COUNT_ANNOTATION);
			result.add(COUNT_GOLD_TEST);

			return result;
		}
	}
}

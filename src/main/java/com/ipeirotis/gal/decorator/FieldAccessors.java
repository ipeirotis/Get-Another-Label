package com.ipeirotis.gal.decorator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import com.ipeirotis.gal.scripts.Datum;
import com.ipeirotis.gal.scripts.DawidSkene;
import com.ipeirotis.utils.Utils;

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

					return Double.toString(Utils.round(value, 3));
				} else if (v != null) {
					return ObjectUtils.toString(v);
				}

			}

			return "---";
		}
	}

	public static final class DS_ACCESSORS {
		public static final//
		EntityFieldAccessor WEIGHTED_QUALITY_FOR_EST_QUALITY_EXP = new EntityFieldAccessor(
				"weightedQualityForEstQualityExp",
				"Weighted Quality (Est Quality Exp)", DawidSkeneDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}
		}.withSummaryAveraged("Weighted Quality w/ Expected cost, according to the algorithm estimates");

		public static final//
		EntityFieldAccessor WEIGHTED_QUALITY_FOR_EST_QUALITY_OPT = new EntityFieldAccessor(
				"weightedQualityForEstQualityOpt",
				"Weighted Quality (Est Quality Opt)", DawidSkeneDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}
		}.withSummaryAveraged("Weighted Quality w/ Minimized cost, according to the algorithm estimates");

		public static final//
		EntityFieldAccessor WEIGHTED_QUALITY_FOR_EVAL_QUALITY_EXP = new EntityFieldAccessor(
				"weightedQualityForEvalQualityExp",
				"Weighted Quality (Eval Quality Exp)",
				DawidSkeneDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}
		}.withSummaryAveraged("Weighted Quality w/ Expected cost, according to the evaluation data");

		public static final//
		EntityFieldAccessor WEIGHTED_QUALITY_FOR_EVAL_QUALITY_OPT = new EntityFieldAccessor(
				"weightedQualityForEvalQualityOpt",
				"Weighted Quality (Eval Quality Opt)",
				DawidSkeneDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Weighted Quality w/ Minimized cost, according to the evaluation data");

		public static Collection<FieldAccessor> getFieldAcessors(DawidSkene ds) {
			List<FieldAccessor> result = new ArrayList<FieldAccessor>();

			result.add(WEIGHTED_QUALITY_FOR_EST_QUALITY_EXP);
			result.add(WEIGHTED_QUALITY_FOR_EST_QUALITY_OPT);
			result.add(WEIGHTED_QUALITY_FOR_EVAL_QUALITY_EXP);
			result.add(WEIGHTED_QUALITY_FOR_EVAL_QUALITY_OPT);

			return result;
		}

	}

	public static final class DATUM_ACCESSORS {
		public static final//
		EntityFieldAccessor NAME = new EntityFieldAccessor("name", "Object");

		public static final//
		EntityFieldAccessor DS_CATEGORY = new EntityFieldAccessor(
				"mostLikelyCategory", "DS_Category");

		public static final//
		EntityFieldAccessor MV_CATEGORY = new EntityFieldAccessor(
				"mostLikelyCategory_MV", "MV_Category");// .withSummaryAveraged("Majorify Vote estimate for prior probability of category");

		public static final//
		EntityFieldAccessor MINCOST_MV = new EntityFieldAccessor(
				"minCostCategory_MV", "MIN_COST_Category_MV");

		public static final//
		EntityFieldAccessor MINCOST_DS = new EntityFieldAccessor(
				"minCostCategory_DS", "MIN_COST_Category_DS");

		public static final//
		EntityFieldAccessor DS_EXP_COST = new EntityFieldAccessor(
				"expectedCost", "DS_Exp_Cost")
				.withSummaryAveraged("Expected misclassification cost (for EM algorithm)");

		public static final//
		EntityFieldAccessor MV_EXP_COST = new EntityFieldAccessor(
				"expectedMVCost", "MV_Exp_Cost")
				.withSummaryAveraged("Expected misclassification cost (for Majority Voting algorithm)");

		public static final//
		EntityFieldAccessor NOVOTE_EXP_COST = new EntityFieldAccessor(
				"spammerCost", "NoVote_Opt_Cost")
				.withSummaryAveraged("Expected misclassification cost (random classification)");

		public static final//
		EntityFieldAccessor DS_OPT_COST = new EntityFieldAccessor("minCost",
				"DS_Opt_Cost")
				.withSummaryAveraged("Minimized misclassification cost (for EM algorithm)");

		public static final//
		EntityFieldAccessor MV_OPT_COST = new EntityFieldAccessor("minMVCost",
				"MV_Opt_Cost")
				.withSummaryAveraged("Minimized misclassification cost (for Majority Voting algorithm)");
		
		public static final//
		EntityFieldAccessor EVAL_COST_MV_MIN = new EntityFieldAccessor(
				"evalCostMVMin", "Eval_Cost_MV_Min") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Classification cost for min-cost classification (evaluation data)");

		public static final//
		EntityFieldAccessor EVAL_COST_DS_MIN = new EntityFieldAccessor(
				"evalCostDSMin", "Eval_Cost_DS_Min") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}
		.withSummaryAveraged("Classification cost for min-cost classification using EM (evaluation data)");

		public static final//
		EntityFieldAccessor NOVOTE_OPT_COST = new EntityFieldAccessor(
				"minSpammerCost", "NoVote_Opt_Cost")
				.withSummaryAveraged("Minimized misclassification cost (random classification)");

		public static final EntityFieldAccessor//
		CORRECT_CATEGORY = new EvalDatumFieldAccessor("evaluationCategory",
				"Correct_Category");

		// Data Quality

		public static final//
		EntityFieldAccessor DATAQUALITY_DS = new EntityFieldAccessor(
				"dataQualityForDS", "DataQuality_DS") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Data quality (estimated according to DS_Exp metric)");

		public static final//
		EntityFieldAccessor DATAQUALITY_MV = new EntityFieldAccessor(
				"dataQualityForMV", "DataQuality_MV") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Data quality (estimated according to Mv_Exp metric)");

		public static final//
		EntityFieldAccessor DATAQUALITY_DS_OPT = new EntityFieldAccessor(
				"dataQualityForDSOpt", "DataQuality_DS_OPT") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Data quality (estimated according to DS_Opt metric)");

		public static final//
		EntityFieldAccessor DATAQUALITY_MV_OPT = new EntityFieldAccessor(
				"dataQualityForMVOpt", "DataQuality_MV_OPT") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Data quality (estimated according to MV_Opt metric)");

		// Eval

		public static final EntityFieldAccessor//
		EVAL_COST_MV_ML = new EvalDatumFieldAccessor(
				"evalClassificationCostForMVML", "Eval_Cost_MV_ML")
				.withSummaryAveraged("Classification cost for naive single-class classification, using majority voting (evaluation data)");

		public static final EntityFieldAccessor//
		EVAL_COST_DS_ML = new EvalDatumFieldAccessor(
				"evalClassificationCostForDSML", "Eval_Cost_DS_ML")
				.withSummaryAveraged("Classification cost for single-class classification, using EM (evaluation data)");

		public static final EntityFieldAccessor//
		EVAL_COST_MV_SOFT = new EvalDatumFieldAccessor(
				"evalClassificationCostForMVSoft", "Eval_Cost_MV_Soft")
				.withSummaryAveraged("Classification cost for naive soft-label classification (evaluation data)");

		public static final EntityFieldAccessor//
		EVAL_COST_DS_SOFT = new EvalDatumFieldAccessor(
				"evalClassificationCostForDSSoft", "Eval_Cost_DS_Soft")
				.withSummaryAveraged("Classification cost for soft-label classification, using EM (evaluation data)");

		public static final EntityFieldAccessor//
		DATAQUALITY_EVAL_COST_DS_ML = new EvalDatumFieldAccessor(
				"evalDataQualityForDSML", "DataQuality_Eval_Cost_DS_ML") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Data quality, DS algorithm, maximum likelihood");

		public static final EntityFieldAccessor//
		DATAQUALITY_EVAL_COST_DS_SOFT = new EvalDatumFieldAccessor(
				"evalDataQualityForDSSoft", "DataQuality_Eval_Cost_DS_Soft") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Data quality, DS algorithm, soft label");

		public static final EntityFieldAccessor//
		DATAQUALITY_EVAL_COST_MV_ML = new EvalDatumFieldAccessor(
				"evalDataQualityForMVML", "DataQuality_Eval_Cost_MV_ML") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Data quality, naive majority voting algorithm");

		public static final EntityFieldAccessor//
		DATAQUALITY_EVAL_COST_MV_SOFT = new EvalDatumFieldAccessor(
				"evalDataQualityForMVSoft", "DataQuality_Eval_Cost_MV_Soft") {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Data quality, naive soft label");

		public static Collection<FieldAccessor> getFieldAcessors(DawidSkene ds) {
			List<FieldAccessor> result = new ArrayList<FieldAccessor>();

			result.add(NAME);

			for (String c : ds.getCategories().keySet())
				result.add(new CategoryDatumFieldAccessor(c));

			result.add(DS_CATEGORY);

			for (String c : ds.getCategories().keySet())
				result.add(new MVCategoryDatumFieldAccessor(c));

			result.add(MV_CATEGORY);
			
			result.add(MINCOST_MV);
			result.add(MINCOST_DS);

			result.add(DS_EXP_COST);
			result.add(MV_EXP_COST);
			result.add(NOVOTE_EXP_COST);
			result.add(DS_OPT_COST);
			result.add(MV_OPT_COST);
			
			result.add(EVAL_COST_MV_MIN);
			result.add(EVAL_COST_DS_MIN);
			
			result.add(NOVOTE_OPT_COST);
			result.add(CORRECT_CATEGORY);

			result.add(DATAQUALITY_DS);
			result.add(DATAQUALITY_MV);
			result.add(DATAQUALITY_DS_OPT);
			result.add(DATAQUALITY_MV_OPT);

			result.add(EVAL_COST_MV_ML);
			result.add(EVAL_COST_DS_ML);
			result.add(EVAL_COST_MV_SOFT);
			result.add(EVAL_COST_DS_SOFT);

			result.add(DATAQUALITY_EVAL_COST_DS_ML);
			result.add(DATAQUALITY_EVAL_COST_DS_SOFT);
			result.add(DATAQUALITY_EVAL_COST_MV_ML);
			result.add(DATAQUALITY_EVAL_COST_MV_SOFT);

			return result;
		}
	}

	public static final class WORKER_ACESSORS {
		public static final//
		EntityFieldAccessor NAME = new EntityFieldAccessor("name", "Worker");

		public static final EntityFieldAccessor EST_QUALITY_EXP = new EntityFieldAccessor(
				"expectedCost", "Est. Quality (Expected)",
				WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Expected cost, according to the algorithm estimates");

		public static final EntityFieldAccessor EST_QUALITY_OPT = new EntityFieldAccessor(
				"minCost", "Est. Quality (Optimized)", WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Minimized cost, according to the algorithm estimates");

		public static final EntityFieldAccessor EVAL_QUALITY_EXP = new EntityFieldAccessor(
				"expCostEval", "Est. Quality (Expected)", WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}
		}.withSummaryAveraged("Expected cost, according to the evaluation data");

		public static final EntityFieldAccessor EVAL_QUALITY_OPT = new EntityFieldAccessor(
				"minCostEval", "Est. Quality (Optimized)",
				WorkerDecorator.class) {
			{
				setFormatter(MetricsFormatter.PERCENT_FORMATTER);
			}

		}.withSummaryAveraged("Minimized cost, according to evaluation data");

		public static final EntityFieldAccessor COUNT_ANNOTATION = new EntityFieldAccessor(
				"numContributions", "Number of Annotations",
				WorkerDecorator.class)
				.withSummaryAveraged("Number of Annotations");

		public static final EntityFieldAccessor COUNT_GOLD_TEST = new EntityFieldAccessor(
				"numGoldTests", "Gold Tests", WorkerDecorator.class)
				.withSummaryAveraged("Number of Gold Tests");

		public static Collection<FieldAccessor> getFieldAcessors(DawidSkene ds) {
			List<FieldAccessor> result = new ArrayList<FieldAccessor>();

			result.add(NAME);
			result.add(EST_QUALITY_EXP);
			result.add(EST_QUALITY_OPT);
			result.add(EVAL_QUALITY_EXP);
			result.add(EVAL_QUALITY_OPT);
			result.add(COUNT_ANNOTATION);
			result.add(COUNT_GOLD_TEST);

			return result;
		}
	}
}

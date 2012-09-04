package com.ipeirotis.gal.decorator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import com.ipeirotis.gal.scripts.Datum;
import com.ipeirotis.gal.scripts.DawidSkene;
import com.ipeirotis.utils.Utils;

public class FieldAccessors {
	public static abstract class FieldAccessor<T> {
		String name;

		public String getName() {
			return name;
		}

		String desc;

		public String getDesc() {
			return desc;
		}

		public abstract Object getValue(T wrapped);

		FieldAccessor(String name) {
			this.name = this.desc = name;
		}

		FieldAccessor(String name, String desc) {
			this.name = name;
			this.desc = desc;
		}

		public String getStringValue(T wrapped) {
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
	}

	public static class DecoratorFieldAccessor<T> extends FieldAccessor<T> {
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
		public Object getValue(T wrapped) {
			try {
				@SuppressWarnings("unchecked")
				Decorator<T> decorator = (Decorator<T>) decoratorClass
						.getConstructor(wrapped.getClass())
						.newInstance(wrapped);

				return decorator.lookupObject(name);
			} catch (Exception exc) {
				throw new RuntimeException(exc);
			}
		}
	}

	public static class DatumFieldAccessor extends DecoratorFieldAccessor<Datum> {
		public DatumFieldAccessor(String name, String desc) {
			super(name, desc, DatumDecorator.class);
		}

		public DatumFieldAccessor withSummaryAveraged(String summaryDescription) {
			this.summaryDescription = summaryDescription;
			this.averaged = true;

			return this;
		}
	}

	public static class CategoryDatumFieldAccessor extends DatumFieldAccessor {
		private String c;

		CategoryDatumFieldAccessor(String c) {
			super(String.format("DS_PR[%s]", c), String.format("DS_Pr[%s]", c));
			
			this.c = c;
			
			withSummaryAveraged(String.format("DS estimate for prior probability of category %s", c));
		}

		@Override
		public Object getValue(Datum wrapped) {
			return wrapped.getCategoryProbability(c);
		}
	}

	public static class MVCategoryDatumFieldAccessor extends DatumFieldAccessor {
		private String c;

		MVCategoryDatumFieldAccessor(String c) {
			super(String.format("MV_PR[%s]", c), String.format("MV_Pr[%s]", c));

			this.c = c;

			withSummaryAveraged(String.format("Majority Vote estimate for prior probability of category %s", c));
		}

		@Override
		public Object getValue(Datum wrapped) {
			return wrapped.getMVCategoryProbability(c);
		}
	}

	public static class EvalDatumFieldAccessor extends DatumFieldAccessor {
		public EvalDatumFieldAccessor(String name, String desc) {
			super(name, desc);
		}

		@Override
		public String getStringValue(Datum wrapped) {
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

	public static final class DATUM_ACCESSORS {
		public static final//
		DatumFieldAccessor NAME = new DatumFieldAccessor("name", "Object");

		public static final//
		DatumFieldAccessor DS_CATEGORY = new DatumFieldAccessor(
				"mostLikelyCategory", "DS_Category");

		public static final//
		DatumFieldAccessor MV_CATEGORY = new DatumFieldAccessor(
				"mostLikelyCategory_MV", "MV_Category");//.withSummaryAveraged("Majorify Vote estimate for prior probability of category");

		public static final//
		DatumFieldAccessor DS_EXP_COST = new DatumFieldAccessor("expectedCost",
				"DS_Exp_Cost").withSummaryAveraged("Expected misclassification cost (for EM algorithm)");

		public static final//
		DatumFieldAccessor MV_EXP_COST = new DatumFieldAccessor("expectedMVCost",
				"MV_Exp_Cost").withSummaryAveraged("Expected misclassification cost (for Majority Voting algorithm)");

		public static final//
		DatumFieldAccessor NOVOTE_EXP_COST = new DatumFieldAccessor(
				"spammerCost", "NoVote_Opt_Cost").withSummaryAveraged("Expected misclassification cost (random classification)");

		public static final//
		DatumFieldAccessor DS_OPT_COST = new DatumFieldAccessor("minCost",
				"DS_Opt_Cost").withSummaryAveraged("Minimized misclassification cost (for EM algorithm)");

		public static final//
		DatumFieldAccessor MV_OPT_COST = new DatumFieldAccessor("minMVCost",
				"MV_Opt_Cost").withSummaryAveraged("Minimized misclassification cost (for Majority Voting algorithm)");

		public static final//
		DatumFieldAccessor NOVOTE_OPT_COST = new DatumFieldAccessor(
				"minSpammerCost", "NoVote_Opt_Cost").withSummaryAveraged("Minimized misclassification cost (random classification)");
		
		public static final DatumFieldAccessor//
		CORRECT_CATEGORY = new EvalDatumFieldAccessor("evaluationCategory",
				"Correct_Category");

		// Data Quality

		public static final//
		DatumFieldAccessor DATAQUALITY_DS = new DatumFieldAccessor(
				"dataQualityForDS", "DataQuality_DS").withSummaryAveraged("Data quality (estimated according to DS_Exp metric)");

		public static final//
		DatumFieldAccessor DATAQUALITY_MV = new DatumFieldAccessor(
				"dataQualityForMV", "DataQuality_MV").withSummaryAveraged("Data quality (estimated according to Mv_Exp metric)");

		public static final//
		DatumFieldAccessor DATAQUALITY_DS_OPT = new DatumFieldAccessor(
				"dataQualityForDSOpt", "DataQuality_DS_OPT").withSummaryAveraged("Data quality (estimated according to DS_Opt metric)");

		public static final//
		DatumFieldAccessor DATAQUALITY_MV_OPT = new DatumFieldAccessor(
				"dataQualityForMVOpt", "DataQuality_MV_OPT").withSummaryAveraged("Data quality (estimated according to MV_Opt metric)");
		
		// Eval
		
		public static final DatumFieldAccessor//
		EVAL_COST_MV_ML = new EvalDatumFieldAccessor(
				"evalClassificationCostForMVML", "Eval_Cost_MV_ML").withSummaryAveraged("Classification cost for naïve single-class classification, using majority voting (evaluation data)");

		public static final DatumFieldAccessor//
		EVAL_COST_DS_ML = new EvalDatumFieldAccessor(
				"evalClassificationCostForDSML", "Eval_Cost_DS_ML").withSummaryAveraged("Classification cost for single-class classification, using EM (evaluation data)");

		public static final DatumFieldAccessor//
		EVAL_COST_MV_SOFT = new EvalDatumFieldAccessor(
				"evalClassificationCostForMVSoft", "Eval_Cost_MV_Soft").withSummaryAveraged("Classification cost for naïve soft-label classification (evaluation data)");

		public static final DatumFieldAccessor//
		EVAL_COST_DS_SOFT = new EvalDatumFieldAccessor(
				"evalClassificationCostForDSSoft", "Eval_Cost_DS_Soft").withSummaryAveraged("Classification cost for soft-label classification, using EM (evaluation data)");

		public static final DatumFieldAccessor//
		DATAQUALITY_EVAL_COST_DS_ML = new EvalDatumFieldAccessor(
				"evalDataQualityForDSML", "DataQuality_Eval_Cost_DS_ML").withSummaryAveraged("Data quality, DS algorithm, maximum likelihood");

		public static final DatumFieldAccessor//
		DATAQUALITY_EVAL_COST_DS_SOFT = new EvalDatumFieldAccessor(
				"evalDataQualityForDSSoft", "DataQuality_Eval_Cost_DS_Soft").withSummaryAveraged("Data quality, DS algorithm, soft label");
		
		public static final DatumFieldAccessor//
		DATAQUALITY_EVAL_COST_MV_ML = new EvalDatumFieldAccessor(
				"evalDataQualityForMVML", "DataQuality_Eval_Cost_MV_ML").withSummaryAveraged("Data quality, naive majority voting algorithm");

		public static final DatumFieldAccessor//
		DATAQUALITY_EVAL_COST_MV_SOFT = new EvalDatumFieldAccessor(
				"evalDataQualityForMVSoft", "DataQuality_Eval_Cost_MV_Soft").withSummaryAveraged("Data quality, naive soft label");

		public static Collection<FieldAccessor<Datum>> getFieldAcessors(
				DawidSkene ds) {
			List<FieldAccessor<Datum>> result = new ArrayList<FieldAccessor<Datum>>();

			result.add(NAME);

			for (String c : ds.getCategories().keySet())
				result.add(new CategoryDatumFieldAccessor(c));

			result.add(DS_CATEGORY);

			for (String c : ds.getCategories().keySet())
				result.add(new MVCategoryDatumFieldAccessor(c));

			result.add(MV_CATEGORY);

			result.add(DS_EXP_COST);
			result.add(MV_EXP_COST);
			result.add(NOVOTE_EXP_COST);
			result.add(DS_OPT_COST);
			result.add(MV_OPT_COST);
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

}

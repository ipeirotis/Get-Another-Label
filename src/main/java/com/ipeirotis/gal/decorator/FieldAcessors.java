package com.ipeirotis.gal.decorator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import com.ipeirotis.gal.scripts.Datum;
import com.ipeirotis.gal.scripts.DawidSkene;
import com.ipeirotis.utils.Utils;

public class FieldAcessors {
	public static abstract class FieldAcessor<T> {
		String name;

		public String getName() {
			return name;
		}

		String desc;

		public String getDesc() {
			return desc;
		}

		public abstract Object getValue(T wrapped);

		FieldAcessor(String name) {
			this.name = this.desc = name;
		}

		FieldAcessor(String name, String desc) {
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

	public static class MemoizingFieldAcessor<T> extends FieldAcessor<T> {
		private Class<?> decoratorClass;

		public MemoizingFieldAcessor(String name, Class<?> decoratorClass) {
			super(name);
			this.decoratorClass = decoratorClass;
		}

		public MemoizingFieldAcessor(String name, String desc,
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

	public static class DatumFieldAcessor extends MemoizingFieldAcessor<Datum> {
		public DatumFieldAcessor(String name, String desc) {
			super(name, desc, DatumDecorator.class);
		}

		public DatumFieldAcessor withSummaryAveraged(String summaryDescription) {
			this.summaryDescription = summaryDescription;
			this.averaged = true;

			return this;
		}
	}


	public static class CategoryDatumFieldAcessor extends DatumFieldAcessor {
		private String c;

		CategoryDatumFieldAcessor(String c) {
			super(String.format("DS_PR[%s]", c), String.format("DS_Pr[%s]", c));
			
			this.c = c;
			
			withSummaryAveraged(String.format("DS estimate for prior probability of category %s", c));
		}

		@Override
		public Object getValue(Datum wrapped) {
			return wrapped.getCategoryProbability(c);
		}
	}

	public static class MVCategoryDatumFieldAcessor extends DatumFieldAcessor {
		private String c;

		MVCategoryDatumFieldAcessor(String c) {
			super(String.format("MV_PR[%s]", c), String.format("MV_Pr[%s]", c));

			this.c = c;

			withSummaryAveraged(String.format("Majority Vote estimate for prior probability of category %s", c));
		}

		@Override
		public Object getValue(Datum wrapped) {
			return wrapped.getMVCategoryProbability(c);
		}
	}

	public static class EvalDatumFieldAcessor extends DatumFieldAcessor {
		public EvalDatumFieldAcessor(String name, String desc) {
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

	public static final class DATUM_ACESSORS {
		public static final//
		DatumFieldAcessor NAME = new DatumFieldAcessor("name", "Object");

		public static final//
		DatumFieldAcessor DS_CATEGORY = new DatumFieldAcessor(
				"mostLikelyCategory", "DS_Category");

		public static final//
		DatumFieldAcessor MV_CATEGORY = new DatumFieldAcessor(
				"mostLikelyCategory_MV", "MV_Category");//.withSummaryAveraged("Majorify Vote estimate for prior probability of category");

		public static final//
		DatumFieldAcessor DS_EXP_COST = new DatumFieldAcessor("expectedCost",
				"DS_Exp_Cost").withSummaryAveraged("Expected misclassification cost (for EM algorithm)");

		public static final//
		DatumFieldAcessor MV_EXP_COST = new DatumFieldAcessor("expectedMVCost",
				"MV_Exp_Cost").withSummaryAveraged("Expected misclassification cost (for Majority Voting algorithm)");

		public static final//
		DatumFieldAcessor NOVOTE_EXP_COST = new DatumFieldAcessor(
				"spammerCost", "NoVote_Opt_Cost").withSummaryAveraged("Expected misclassification cost (random classification)");

		public static final//
		DatumFieldAcessor DS_OPT_COST = new DatumFieldAcessor("minCost",
				"DS_Opt_Cost").withSummaryAveraged("Minimized misclassification cost (for EM algorithm)");

		public static final//
		DatumFieldAcessor MV_OPT_COST = new DatumFieldAcessor("minMVCost",
				"MV_Opt_Cost").withSummaryAveraged("Minimized misclassification cost (for Majority Voting algorithm)");

		public static final//
		DatumFieldAcessor NOVOTE_OPT_COST = new DatumFieldAcessor(
				"minSpammerCost", "NoVote_Opt_Cost").withSummaryAveraged("Minimized misclassification cost (random classification)");
		
		public static final DatumFieldAcessor//
		CORRECT_CATEGORY = new EvalDatumFieldAcessor("evaluationCategory",
				"Correct_Category");

		// Data Quality

		public static final//
		DatumFieldAcessor DATAQUALITY_DS = new DatumFieldAcessor(
				"dataQualityForDS", "DataQuality_DS").withSummaryAveraged("Data quality (estimated according to DS_Exp metric)");

		public static final//
		DatumFieldAcessor DATAQUALITY_MV = new DatumFieldAcessor(
				"dataQualityForMV", "DataQuality_MV").withSummaryAveraged("Data quality (estimated according to Mv_Exp metric)");

		public static final//
		DatumFieldAcessor DATAQUALITY_DS_OPT = new DatumFieldAcessor(
				"dataQualityForDSOpt", "DataQuality_DS_OPT").withSummaryAveraged("Data quality (estimated according to DS_Opt metric)");

		public static final//
		DatumFieldAcessor DATAQUALITY_MV_OPT = new DatumFieldAcessor(
				"dataQualityForMVOpt", "DataQuality_MV_OPT").withSummaryAveraged("Data quality (estimated according to MV_Opt metric)");
		
		// Eval
		
		public static final DatumFieldAcessor//
		EVAL_COST_MV_ML = new EvalDatumFieldAcessor(
				"evalClassificationCostForMVML", "Eval_Cost_MV_ML").withSummaryAveraged("Classification cost for naïve single-class classification, using majority voting (evaluation data)");

		public static final DatumFieldAcessor//
		EVAL_COST_DS_ML = new EvalDatumFieldAcessor(
				"evalClassificationCostForDSML", "Eval_Cost_DS_ML").withSummaryAveraged("Classification cost for single-class classification, using EM (evaluation data)");

		public static final DatumFieldAcessor//
		EVAL_COST_MV_SOFT = new EvalDatumFieldAcessor(
				"evalClassificationCostForMVSoft", "Eval_Cost_MV_Soft").withSummaryAveraged("Classification cost for naïve soft-label classification (evaluation data)");

		public static final DatumFieldAcessor//
		EVAL_COST_DS_SOFT = new EvalDatumFieldAcessor(
				"evalClassificationCostForDSSoft", "Eval_Cost_DS_Soft").withSummaryAveraged("Classification cost for soft-label classification, using EM (evaluation data)");

		public static final DatumFieldAcessor//
		DATAQUALITY_EVAL_COST_DS_ML = new EvalDatumFieldAcessor(
				"evalDataQualityForDSML", "DataQuality_Eval_Cost_DS_ML").withSummaryAveraged("Data quality, DS algorithm, maximum likelihood");

		public static final DatumFieldAcessor//
		DATAQUALITY_EVAL_COST_DS_SOFT = new EvalDatumFieldAcessor(
				"evalDataQualityForDSSoft", "DataQuality_Eval_Cost_DS_Soft").withSummaryAveraged("Data quality, DS algorithm, soft label");
		
		public static final DatumFieldAcessor//
		DATAQUALITY_EVAL_COST_MV_ML = new EvalDatumFieldAcessor(
				"evalDataQualityForMVML", "DataQuality_Eval_Cost_MV_ML").withSummaryAveraged("Data quality, naive majority voting algorithm");

		public static final DatumFieldAcessor//
		DATAQUALITY_EVAL_COST_MV_SOFT = new EvalDatumFieldAcessor(
				"evalDataQualityForMVSoft", "DataQuality_Eval_Cost_MV_Soft").withSummaryAveraged("Data quality, naive soft label");

		public static Collection<FieldAcessor<Datum>> getFieldAcessors(
				DawidSkene ds) {
			List<FieldAcessor<Datum>> result = new ArrayList<FieldAcessor<Datum>>();

			result.add(NAME);

			for (String c : ds.getCategories().keySet())
				result.add(new CategoryDatumFieldAcessor(c));

			result.add(DS_CATEGORY);

			for (String c : ds.getCategories().keySet())
				result.add(new MVCategoryDatumFieldAcessor(c));

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

package com.ipeirotis.gal.scripts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.ipeirotis.utils.Utils;

public class DawidSkene {

	private HashMap<String, Category>	categories;

	private Boolean										fixedPriors;

	private HashMap<String, Datum>		objects;
	private HashMap<String, Worker>		workers;

	public DawidSkene(Set<Category> categories) {

		this.objects = new HashMap<String, Datum>();
		this.workers = new HashMap<String, Worker>();

		this.fixedPriors = false;
		this.categories = new HashMap<String, Category>();
		for (Category c : categories) {
			this.categories.put(c.getName(), c);
			if (c.hasPrior()) {
				this.fixedPriors = true;
			}
		}
		// We initialize the priors to be uniform across classes
		// if the user did not pass any information about the prior values
		if (!fixedPriors)
			initializePriors();

		// By default, we initialize the misclassification costs
		// assuming a 0/1 loss function. The costs can be customized
		// using the corresponding file
		initializeCosts();

	}

	public void addAssignedLabel(AssignedLabel al) {

		String workerName = al.getWorkerName();
		String objectName = al.getObjectName();

		String categoryName = al.getCategoryName();
		assert (this.categories.keySet().contains(categoryName));

		// If we already have the object, then just add the label
		// in the set of labels for the object.
		// If it is the first time we see the object, then create
		// the appropriate entry in the objects hashmap
		Datum d;
		if (this.objects.containsKey(objectName)) {
			d = this.objects.get(objectName);
		} else {
			Set<Category> categories = new HashSet<Category>(this.categories.values());
			d = new Datum(objectName, categories);
		}
		d.addAssignedLabel(al);
		this.objects.put(objectName, d);

		// If we already have the worker, then just add the label
		// in the set of labels assigned by the worker.
		// If it is the first time we see the object, then create
		// the appropriate entry in the objects hashmap
		Worker w;
		if (this.workers.containsKey(workerName)) {
			w = this.workers.get(workerName);
		} else {
			Set<Category> categories = new HashSet<Category>(this.categories.values());
			w = new Worker(workerName, categories);
		}
		w.addAssignedLabel(al);
		this.workers.put(workerName, w);

	}

	public void addCorrectLabel(CorrectLabel cl) {

		String objectName = cl.getObjectName();
		String correctCategory = cl.getCorrectCategory();

		Datum d;
		if (this.objects.containsKey(objectName)) {
			d = this.objects.get(objectName);
			d.setGold(true);
			d.setCorrectCategory(correctCategory);
		} else {
			Set<Category> categories = new HashSet<Category>(this.categories.values());
			d = new Datum(objectName, categories);
			d.setGold(true);
			d.setCorrectCategory(correctCategory);
		}
		this.objects.put(objectName, d);
	}
	
	public void addEvaluationLabel(CorrectLabel cl) {

		String objectName = cl.getObjectName();
		String correctCategory = cl.getCorrectCategory();
		Datum d = this.objects.get(objectName);
		assert( d != null); // All objects in the evaluation should be rated by at least one worker
		d.setEvaluation(true);
		d.setEvaluationCategory(correctCategory);
		this.objects.put(objectName, d);
	}

	/**
	 * @return the fixedPriors
	 */
	public Boolean fixedPriors() {

		return fixedPriors;
	}

	public void addMisclassificationCost(MisclassificationCost cl) {

		String from = cl.getCategoryFrom();
		String to = cl.getCategoryTo();
		Double cost = cl.getCost();
		
		Category c = this.categories.get(from);
		c.setCost(to, cost);
		this.categories.put(from, c);

	}

	/**
	 * Runs the algorithm, iterating the specified number of times
	 * TODO: Estimate the model log-likelihood and stop once the log-likelihood values converge
	 * 
	 * @param iterations
	 */
	public void estimate(int iterations) {

		for (int i = 0; i < iterations; i++) {
			updateObjectClassProbabilities();
			updatePriors();
			updateWorkerConfusionMatrices();
		}
	}

	public HashMap<String, String> getMajorityVote() {

		HashMap<String, String> result = new HashMap<String, String>();

		for (String objectName : this.objects.keySet()) {
			Datum d = this.objects.get(objectName);
			String category = d.getMostLikelyCategory();
			result.put(objectName, category);
		}
		return result;
	}






	private HashMap<String, Double> getObjectClassProbabilities(String objectName, String workerToIgnore) {

		HashMap<String, Double> result = new HashMap<String, Double>();

		Datum d = this.objects.get(objectName);

		// If this is a gold example, just put the probability estimate to be
		// 1.0
		// for the correct class
		if (d.isGold()) {
			for (String category : this.categories.keySet()) {
				String correctCategory = d.getCorrectCategory();
				if (category.equals(correctCategory)) {
					result.put(category, 1.0);
				} else {
					result.put(category, 0.0);
				}
			}
			return result;
		}

		// Let's check first if we have any workers who have labeled this item,
		// except for the worker that we ignore
		Set<AssignedLabel> labels = d.getAssignedLabels();
		if (labels.isEmpty())
			return null;
		if (workerToIgnore != null && labels.size() == 1) {
			for (AssignedLabel al : labels) {
				if (al.getWorkerName().equals(workerToIgnore))
					return null;
			}
		}

		// If it is not gold, then we proceed to estimate the class
		// probabilities using the method of Dawid and Skene and we proceed as
		// usual with the M-phase of the EM-algorithm of Dawid&Skene

		// Estimate denominator for Eq 2.5 of Dawid&Skene, which is the same
		// across all categories
		Double denominator = 0.0;

		// To compute the denominator, we also compute the nominators across
		// all categories, so it saves us time to save the nominators as we
		// compute them
		HashMap<String, Double> categoryNominators = new HashMap<String, Double>();

		for (Category category : categories.values()) {

			// We estimate now Equation 2.5 of Dawid & Skene
			Double categoryNominator = category.getPrior();

			// We go through all the labels assigned to the d object
			for (AssignedLabel al : d.getAssignedLabels()) {
				Worker w = workers.get(al.getWorkerName());

				// If we are trying to estimate the category probability
				// distribution
				// to estimate the quality of a given worker, then we need to
				// ignore
				// the labels submitted by this worker.
				if (workerToIgnore != null && w.getName().equals(workerToIgnore))
					continue;

				String assigned_category = al.getCategoryName();
				double evidence_for_category = w.getErrorRate(category.getName(), assigned_category);
				if (Double.isNaN(evidence_for_category))
					continue;
				categoryNominator *= evidence_for_category;
			}

			categoryNominators.put(category.getName(), categoryNominator);
			denominator += categoryNominator;
		}

		for (String category : categories.keySet()) {
			Double nominator = categoryNominators.get(category);
			if (denominator == 0.0) {
				// result.put(category, 0.0);
				return null;
			} else {
				Double probability = Utils.round(nominator / denominator, 5);
				result.put(category, probability);
			}
		}
		return result;

	}

	/**
	 * Estimates the cost for annotator k without attempting corrections of
	 * labels
	 * 
	 * @param w
	 *          The worker
	 * @return The expected cost of misclassifications of worker
	 */
	public Double getAnnotatorCostNaive(Worker w) {

		Double c = 0.0;
		Double s = 0.0;
		for (Category from : this.categories.values()) {
			for (Category to : this.categories.values()) {
				c += from.getPrior() * from.getCost(to.getName()) * w.getErrorRate(from.getName(), to.getName());
				s += from.getPrior() * from.getCost(to.getName());
			}
		}
		return (s > 0) ? c / s : 0.0;
	}



	public int getNumberOfWorkers() {

		return this.workers.size();
	}

	public int getNumberOfObjects() {

		return this.objects.size();
	}




	/**
	 * We initialize the misclassification costs using the 0/1 loss
	 * 
	 * @param categories
	 */
	private void initializeCosts() {

		for (String from : categories.keySet()) {
			for (String to : categories.keySet()) {
				Category c = categories.get(from);
				if (from.equals(to)) {
					c.setCost(to, 0.0);
				} else {
					c.setCost(to, 1.0);
				}
				categories.put(from, c);
			}
		}
	}

	private void initializePriors() {

		for (String cat : categories.keySet()) {
			Category c = categories.get(cat);
			c.setPrior(1.0 / categories.keySet().size());
			categories.put(cat, c);
		}
	}
	
	public void evaluateWorkers() {

		for (Worker w : this.workers.values()) {
			computeEvalConfusionMatrix(w);
		}
	}
	
	private void computeEvalConfusionMatrix(Worker w) {
		ConfusionMatrix eval_cm = new ConfusionMatrix(this.categories.values());
		eval_cm.empty();
		for (AssignedLabel l : w.getAssignedLabels()){
			
			String objectName = l.getObjectName();
			Datum d = this.objects.get(objectName);
			assert(d != null);
			if (d.getEvaluation() == false) continue;
			
			String assignedCategory = l.getCategoryName();
			String correctCategory = d.getEvaluationCategory();
			
			//Double currentCount = eval_cm.getErrorRate(correctCategory, assignedCategory);
			eval_cm.addError(correctCategory, assignedCategory, 1.0);
		}
		eval_cm.normalize();
		w.setEvalConfusionMatrix(eval_cm);
	}

	/*
	public String printDiffVote(HashMap<String, String> prior_voting, HashMap<String, String> posterior_voting) {

		StringBuffer sb = new StringBuffer();

		for (String obj : (new TreeSet<String>(prior_voting.keySet()))) {
			String prior_vote = prior_voting.get(obj);
			String posterior_vote = posterior_voting.get(obj);

			if (prior_vote.equals(posterior_vote)) {
				sb.append("SAME\t" + obj + "\t" + prior_vote);
			} else {
				sb.append("DIFF\t" + obj + "\t" + prior_vote + "->" + posterior_vote);
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	*/

	public String printAllWorkerScores(boolean detailed) {

		StringBuffer sb = new StringBuffer();

		if (!detailed) {
			sb.append("Worker\tError Rate\tExpected Quality (Est.)\tOptimized Quality (Est.)\tExpected Quality (Eval.)\tOptimized Quality (Eval.)\tNumber of Annotations\tGold Tests\n");
		}
		for (String workername : new TreeSet<String>(this.workers.keySet())) {
			Worker w = this.workers.get(workername);
			sb.append(printWorkerScore(w, detailed));
		}
		return sb.toString();
	}

	private Integer countGoldTests(Set<AssignedLabel> labels) {

		Integer result = 0;
		for (AssignedLabel al : labels) {
			String name = al.getObjectName();
			Datum d = this.objects.get(name);
			if (d.isGold())
				result++;

		}
		return result;
	}

	public String printWorkerScore(Worker w, boolean detailed) {

		StringBuffer sb = new StringBuffer();

		String workerName = w.getName();

		Double cost_naive = this.getAnnotatorCostNaive(w);
		String s_cost_naive = (Double.isNaN(cost_naive)) ? "---" : Utils.round(100 * cost_naive, 2) + "%";

		Double cost_exp = w.getWorkerCost(categories, Worker.EXP_COST_EST);
		String s_cost_exp = (Double.isNaN(cost_exp)) ? "---" : Math.round(100 * (1 - cost_exp)) + "%";

		Double cost_min = w.getWorkerCost(categories, Worker.MIN_COST_EST);
		String s_cost_min = (Double.isNaN(cost_min)) ? "---" : Math.round(100 * (1 - cost_min)) + "%";

		Double cost_exp_eval = w.getWorkerCost(categories, Worker.EXP_COST_EVAL);
		String s_cost_exp_eval = (Double.isNaN(cost_exp_eval)) ? "---" : Math.round(100 * (1 - cost_exp_eval)) + "%";

		Double cost_min_eval = w.getWorkerCost(categories, Worker.MIN_COST_EVAL);
		String s_cost_min_eval = (Double.isNaN(cost_min_eval)) ? "---" : Math.round(100 * (1 - cost_min_eval)) + "%";
			
		Integer contributions = w.getAssignedLabels().size();
		Integer gold_tests = this.countGoldTests(w.getAssignedLabels());

		if (detailed) {
			sb.append("Worker: " + workerName + "\n");
			sb.append("Est. Error Rate: " + s_cost_naive + "\n");
			sb.append("Est. Quality (Expected): " + s_cost_exp + "\n");
			sb.append("Est. Quality (Optimized): " + s_cost_min + "\n");
			sb.append("Eval. Quality (Expected): " + s_cost_exp_eval + "\n");
			sb.append("Eval. Quality (Optimized): " + s_cost_min_eval + "\n");
			sb.append("Number of Annotations: " + contributions + "\n");
			sb.append("Number of Gold Tests: " + gold_tests + "\n");

			sb.append("Confusion Matrix (Estimated): \n");
			for (String correct_name : this.categories.keySet()) {
				for (String assigned_name : this.categories.keySet()) {
					Double cm_entry = w.getErrorRate(correct_name, assigned_name);
					String s_cm_entry = Double.isNaN(cm_entry) ? "---" : Utils.round(100 * cm_entry, 3).toString();
					sb.append("P[" + correct_name + "->" + assigned_name + "]=" + s_cm_entry + "%\t");
				}
				sb.append("\n");
			}		
			sb.append("Confusion Matrix (Evaluation data): \n");
			for (String correct_name : this.categories.keySet()) {
				for (String assigned_name : this.categories.keySet()) {
					Double cm_entry = w.getErrorRate_Eval(correct_name, assigned_name);
					String s_cm_entry = Double.isNaN(cm_entry) ? "---" : Utils.round(100 * cm_entry, 3).toString();
					sb.append("P[" + correct_name + "->" + assigned_name + "]=" + s_cm_entry + "%\t");
				}
				sb.append("\n");
			}	
			sb.append("\n");
		} else {
			sb.append(workerName + "\t" + s_cost_naive + "\t" + s_cost_exp + "\t" + s_cost_min + "\t" + s_cost_exp_eval + 
					"\t" + s_cost_min_eval + "\t" + contributions + "\t" + gold_tests + "\n");
		}

		return sb.toString();
	}

	/**
	 * Prints the objects that have probability distributions with entropy
	 * higher than the given threshold
	 */
	public String printObjectClassProbabilities() {

		StringBuffer sb = new StringBuffer();
		sb.append("Object\t");
		for (String c : this.categories.keySet()) {
			sb.append("DS_Pr[" + c + "]\t");
		}
		sb.append("DS_Category\t");
		for (String c : this.categories.keySet()) {
			sb.append("MV_Pr[" + c + "]\t");
		}
		sb.append("MV_Category\t");
		// TODO: Also print majority label and the min-cost label, pre-DS and post-DS
		sb.append("DS_Exp_Cost\tMV_Exp_Cost\tNoVote_Exp_Cost\t");
		sb.append("DS_Opt_Cost\tMV_Opt_Cost\tNoVote_Opt_Cost\n");

		
		for (String object_name : new TreeSet<String>(this.objects.keySet())) {
			Datum d = this.objects.get(object_name);

			//Double entropy = d.getEntropy();
			//if (entropy < entropy_threshold)
			//	continue;

			sb.append(object_name + "\t");
			for (String c : this.categories.keySet()) {
				sb.append(d.getCategoryProbability(c) + "\t");
			}
			sb.append(d.getMostLikelyCategory() + "\t");
			for (String c : this.categories.keySet()) {
				sb.append(d.getMVCategoryProbability(c) + "\t");
			}
			sb.append(d.getMostLikelyCategory_MV() + "\t");
			
			sb.append(d.getExpectedCost(categories) + "\t");
			sb.append(d.getExpectedMVCost(categories) + "\t");
			sb.append(Helper.getSpammerCost(categories) + "\t");
			
			sb.append(d.getMinCost(categories) + "\t");
			sb.append(d.getMinMVCost(categories) + "\t");
			sb.append(Helper.getMinSpammerCost(categories) + "\n");
			
			// TODO: Print evaluation label, actual cost for "ML" version of MV and DS, cost for the soft versions of MV and DS
			
		}

		return sb.toString();

	}

	public String printPriors() {

		StringBuffer sb = new StringBuffer();
		for (Category c : this.categories.values()) {
			sb.append("Prior[" + c.getName() + "]=" + c.getPrior() + "\n");
		}
		return sb.toString();
	}

	public String printVote() {

		StringBuffer sb = new StringBuffer();

		HashMap<String, String> vote = getMajorityVote();

		for (String obj : (new TreeSet<String>(vote.keySet()))) {
			String majority_vote = vote.get(obj);
			sb.append(obj + "\t" + majority_vote + "\n");
		}
		return sb.toString();
	}

	public void setFixedPriors(HashMap<String, Double> priors) {

		this.fixedPriors = true;
		setPriors(priors);
	}

	private void setPriors(HashMap<String, Double> priors) {

		for (String c : this.categories.keySet()) {
			Category category = this.categories.get(c);
			Double prior = priors.get(c);
			category.setPrior(prior);
			this.categories.put(c, category);
		}
	}

	public void unsetFixedPriors() {

		this.fixedPriors = false;
		updatePriors();
	}

	private void updateObjectClassProbabilities() {

		for (String objectName : this.objects.keySet()) {
			this.updateObjectClassProbabilities(objectName);
		}
	}

	private void updateObjectClassProbabilities(String objectName) {

		Datum d = this.objects.get(objectName);
		HashMap<String, Double> probabilities = getObjectClassProbabilities(objectName, null);
		if (probabilities == null)
			return;
		for (String category : probabilities.keySet()) {
			Double probability = probabilities.get(category);
			d.setCategoryProbability(category, probability);
		}
		this.objects.put(objectName, d);
	}

	/**
	 * 
	 */
	private void updatePriors() {

		if (fixedPriors)
			return;

		HashMap<String, Double> priors = new HashMap<String, Double>();
		for (String c : this.categories.keySet()) {
			priors.put(c, 0.0);
		}

		int totalObjects = this.objects.size();
		for (Datum d : this.objects.values()) {
			for (String c : this.categories.keySet()) {
				Double prior = priors.get(c);
				Double objectProb = d.getCategoryProbability(c);
				prior += objectProb / totalObjects;
				priors.put(c, prior);
			}
		}
		setPriors(priors);
	}

	private void updateWorkerConfusionMatrices() {

		for (String workerName : this.workers.keySet()) {
			updateWorkerConfusionMatrix(workerName);
		}
	}

	/**
	 * @param lid
	 */
	private void updateWorkerConfusionMatrix(String workerName) {

		Worker w = this.workers.get(workerName);

		ConfusionMatrix cm = new ConfusionMatrix(this.categories.values());
		cm.empty();

		// Scan all objects and change the confusion matrix for each worker
		// using the class probability for each object
		for (AssignedLabel al : w.getAssignedLabels()) {

			// Get the name of the object and the category it
			// is classified from this worker.
			String objectName = al.getObjectName();
			String destination = al.getCategoryName();

			// We get the classification of the object
			// based on the votes of all the other workers
			// We treat this classification as the "correct" one
			HashMap<String, Double> probabilities = this.getObjectClassProbabilities(objectName, workerName);
			if (probabilities == null)
				continue; // No other worker labeled the object

			for (String source : probabilities.keySet()) {
				Double error = probabilities.get(source);
				cm.addError(source, destination, error);
			}

		}
		cm.normalize();

		w.setConfusionMatrix(cm);

	}

}

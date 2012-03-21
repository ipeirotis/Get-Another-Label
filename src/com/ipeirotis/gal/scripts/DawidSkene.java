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
			String category = d.getMajorityCategory();
			result.put(objectName, category);
		}
		return result;
	}

	private String getMinCostLabel(HashMap<String, Double> softLabel) {

		String result = null;
		Double min_cost = Double.MAX_VALUE;

		for (String c1 : softLabel.keySet()) {
			// So, with probability p1 it belongs to class c1
			// Double p1 = probabilities.get(c1);

			// What is the cost in this case?
			Double costfor_c1 = 0.0;
			for (String c2 : softLabel.keySet()) {
				// With probability p2 it actually belongs to class c2
				Double p2 = softLabel.get(c2);
				Double cost = this.categories.get(c1).getCost(c2);
				costfor_c1 += p2 * cost;

			}

			if (costfor_c1 < min_cost) {
				result = c1;
				min_cost = costfor_c1;
			}

		}

		return result;
	}

	/**
	 * Gets as input a "soft label" (i.e., a distribution of probabilities over
	 * classes) and returns the smallest possible cost for this soft label.
	 * 
	 * @param p
	 * @return The expected cost of this soft label
	 */
	private Double getMinSoftLabelCost(HashMap<String, Double> probabilities) {

		Double min_cost = Double.NaN;

		for (String c1 : probabilities.keySet()) {
			// So, with probability p1 it belongs to class c1
			// Double p1 = probabilities.get(c1);

			// What is the cost in this case?
			Double costfor_c2 = 0.0;
			for (String c2 : probabilities.keySet()) {
				// With probability p2 it actually belongs to class c2
				Double p2 = probabilities.get(c2);
				Double cost = this.categories.get(c1).getCost(c2);
				costfor_c2 += p2 * cost;

			}

			if (Double.isNaN(min_cost) || costfor_c2 < min_cost) {
				min_cost = costfor_c2;
			}

		}

		return min_cost;
	}

	/**
	 * Returns the minimum possible cost of a "spammer" worker, who assigns
	 * completely random labels.
	 * 
	 * @return The expected cost of a spammer worker
	 */
	private double getMinSpammerCost() {

		HashMap<String, Double> prior = new HashMap<String, Double>();
		for (Category c : this.categories.values()) {
			prior.put(c.getName(), c.getPrior());
		}
		return getMinSoftLabelCost(prior);
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

	private HashMap<String, Double> getSoftLabelForHardCategoryLabel(Worker w, String label) {

		// Pr(c | label) = Pr(label | c) * Pr (c) / Pr(label)
		
		HashMap<String, Double> worker_prior = w.getPrior(new HashSet<Category>(this.categories.values()));

		HashMap<String, Double> result = new HashMap<String, Double>();
		for (Category source : this.categories.values()) {
			Double error = w.getErrorRate(source.getName(), label);
			Double soft = source.getPrior() * error / worker_prior.get(label);
			result.put(source.getName(), soft);
		}

		return result;
	}

	public int getNumberOfWorkers() {
		return this.workers.size();
	}
	
	public int getNumberOfObjects() {
		return this.objects.size();
	}
	
	public static int	COST_NAIVE							= 0;
	public static int	COST_ADJUSTED						= 1;
	public static int	COST_NAIVE_MINIMIZED		= 2; // Effectively, classifying into the "majority class" (class with lowest expected cost)
	public static int	COST_ADJUSTED_MINIMIZED	= 3;

	/**
	 * 
	 * Estimates the cost for worker using various methods: COST_NAIVE: We do
	 * not adjust the label assigned by the worker (i.e., use the "hard" label)
	 * COST_ADJUSTED: We use the error rates of the worker and compute the
	 * posterior probability vector for each object COST_MINIMIZED: Like
	 * COST_ADJUSTED but we also assign the object to the category that
	 * generates the minimum expected error
	 * 
	 * @param w
	 *          The worker object
	 * @param method
	 *          One of DawidSkene.COST_NAIVE, DawidSkene.COST_ADJUSTED,
	 *          DawidSkene.COST_MINIMIZED
	 * @return The expected cost of the worker, normalized to be between 0 and
	 *         1, where 1 is the cost of a "spam" worker
	 */
	public Double getWorkerCost(Worker w, int method) {

		assert (method == COST_NAIVE || method == COST_ADJUSTED || method == COST_NAIVE_MINIMIZED || method == COST_ADJUSTED_MINIMIZED);

		Double cost = 0.0;

		// We estimate first how often the worker assigns each category label
		
		// If we do not have a fixed prior, we can just use the data about the worker
		HashMap<String, Double> worker_prior = w.getPrior(new HashSet<Category>(this.categories.values()));

		// We now know the frequency with which we will see a label
		// "assigned_label" from worker
		// Each of this "hard" labels from the annotator k will corresponds to a
		// corrected
		// "soft" label
		for (Category assigned : this.categories.values()) {
			// Let's find the soft label that corresponds to assigned_label
			String assignedCategory = assigned.getName();

			if (method == COST_NAIVE) {
				// TODO: Check this for correctness. Compare results wth tested
				// implementation first
				HashMap<String, Double> naiveSoftLabel = getNaiveSoftLabel(w, assignedCategory);
				cost += getNaiveSoftLabelCost(assigned.getName(), naiveSoftLabel) * assigned.getPrior();
			} else if (method == COST_NAIVE_MINIMIZED) {
				// TODO: Check this for correctness. Compare results wth tested
				// implementation first
				HashMap<String, Double> naiveSoftLabel = getNaiveSoftLabel(w, assignedCategory);
				cost += getNaiveSoftLabelCost(assigned.getName(), naiveSoftLabel) * assigned.getPrior();
			} else if (method == COST_ADJUSTED) {
				HashMap<String, Double> softLabel = getSoftLabelForHardCategoryLabel(w, assignedCategory);
				cost += getSoftLabelCost(softLabel) * worker_prior.get(assignedCategory);
			} else if (method == COST_ADJUSTED_MINIMIZED) {
				HashMap<String, Double> softLabel = getSoftLabelForHardCategoryLabel(w, assignedCategory);
				cost += getMinSoftLabelCost(softLabel) * worker_prior.get(assignedCategory);
			} else {
				// We should never reach this
				System.err.println("Error: Incorrect method for cost");
			}

			// And add the cost of this label, weighted with the prior of seeing
			// this label.

		}

		if (method == COST_NAIVE || method == COST_NAIVE_MINIMIZED) {
			return cost;
		} else if (method == COST_ADJUSTED) {
			return cost / getSpammerCost();
		} else if (method == COST_ADJUSTED_MINIMIZED) {
			return cost / getMinSpammerCost();
		} else {
			// We should never reach this
			System.err.println("Error: We should have never reached this in getWorkerCost");
			return Double.NaN;
		}

	}

	/**
	 * @param w
	 * @param objectCategory
	 * @return
	 */
	private HashMap<String, Double> getNaiveSoftLabel(Worker w, String objectCategory) {

		HashMap<String, Double> naiveSoftLabel = new HashMap<String, Double>();
		for (String cat : this.categories.keySet()) {
			naiveSoftLabel.put(cat, w.getErrorRate(objectCategory, cat));
		}
		return naiveSoftLabel;
	}

	/**
	 * Gets as input a "soft label" (i.e., a distribution of probabilities over
	 * classes) and returns the expected cost of this soft label.
	 * 
	 * @param p
	 * @return The expected cost of this soft label
	 */
	private Double getNaiveSoftLabelCost(String source, HashMap<String, Double> destProbabilities) {

		Double c = 0.0;
		for (String destination : destProbabilities.keySet()) {
			Double p = destProbabilities.get(destination);
			Double cost = this.categories.get(source).getCost(destination);
			c += p * cost;
		}

		return c;
	}

	/**
	 * Gets as input a "soft label" (i.e., a distribution of probabilities over
	 * classes) and returns the expected cost of this soft label.
	 * 
	 * @param p
	 * @return The expected cost of this soft label
	 */
	private Double getSoftLabelCost(HashMap<String, Double> probabilities) {

		Double c = 0.0;
		for (String c1 : probabilities.keySet()) {
			for (String c2 : probabilities.keySet()) {
				Double p1 = probabilities.get(c1);
				Double p2 = probabilities.get(c2);
				Double cost = this.categories.get(c1).getCost(c2);
				c += p1 * p2 * cost;
			}
		}

		return c;
	}

	/**
	 * Returns the cost of a "spammer" worker, who assigns completely random
	 * labels.
	 * 
	 * @return The expected cost of a spammer worker
	 */
	private double getSpammerCost() {

		HashMap<String, Double> prior = new HashMap<String, Double>();
		for (Category c : this.categories.values()) {
			prior.put(c.getName(), c.getPrior());
		}
		return getSoftLabelCost(prior);
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

	public String printAllWorkerScores(boolean detailed) {

		StringBuffer sb = new StringBuffer();

		if (!detailed) {
			sb.append("Worker\tError Rate\tQuality (Expected)\tQuality (Optimized)\tNumber of Annotations\tGold Tests\n");
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
		String s_cost_naive = (Double.isNaN(cost_naive))? "---" : Utils.round(100 * cost_naive, 2) + "%";
		
		Double cost_adj = this.getWorkerCost(w, DawidSkene.COST_ADJUSTED);
		String s_cost_adj = (Double.isNaN(cost_adj))? "---" : Math.round(100 * (1 - cost_adj)) + "%";
		
		Double cost_min = this.getWorkerCost(w, DawidSkene.COST_ADJUSTED_MINIMIZED);
		String s_cost_min = (Double.isNaN(cost_min))? "---" : Math.round(100 * (1 - cost_min)) + "%";
		
		Integer contributions = w.getAssignedLabels().size();
		Integer gold_tests = this.countGoldTests(w.getAssignedLabels());

		if (detailed) {
			sb.append("Worker: " + workerName + "\n");
			sb.append("Error Rate: " + s_cost_naive + "\n");
			sb.append("Quality (Expected): " + s_cost_adj + "\n");
			sb.append("Quality (Optimized): " + s_cost_min + "\n");
			sb.append("Number of Annotations: " + contributions + "\n");
			sb.append("Number of Gold Tests: " + gold_tests + "\n");
			
			sb.append("Confusion Matrix: \n");
			for (String correct_name : this.categories.keySet()) {
				for (String assigned_name : this.categories.keySet()) {
					Double cm_entry =  w.getErrorRate(correct_name, assigned_name);
					String s_cm_entry =  Double.isNaN(cm_entry)? "---" : Utils.round(100 * cm_entry, 3).toString();
					sb.append("P[" + correct_name + "->" + assigned_name + "]="	+ s_cm_entry + "%\t");
				}
				sb.append("\n");
			}
			sb.append("\n");
		} else {
			sb.append(workerName + "\t" + s_cost_naive + "\t" + s_cost_adj + "\t"	+ s_cost_min + "\t" + contributions + "\t" + gold_tests + "\n");
		}

		return sb.toString();
	}

	/**
	 * Prints the objects that have probability distributions with entropy
	 * higher than the given threshold
	 * 
	 * @param entropy_threshold
	 */
	public String printObjectClassProbabilities(double entropy_threshold) {

		StringBuffer sb = new StringBuffer();
		sb.append("Object\t");
		for (String c : this.categories.keySet()) {
			sb.append("Pr[" + c + "]\t");
		}
		// TODO: Also print majority label and the min-cost label, pre-DS and post-DS
		sb.append("Pre-DS Majority Label\tPre-DS Min Cost Label\tPost-DS Majority Label\tPost-DS Min Cost Label\n");

		for (String object_name : new TreeSet<String>(this.objects.keySet())) {
			Datum d = this.objects.get(object_name);

			Double entropy = d.getEntropy();
			if (entropy < entropy_threshold)
				continue;

			sb.append(object_name + "\t");
			for (String c : this.categories.keySet()) {
				sb.append(d.getCategoryProbability(c) + "\t");
			}
			sb.append("\n");
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

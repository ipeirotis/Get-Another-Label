/*******************************************************************************
 * Copyright 2012 Panos Ipeirotis
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.ipeirotis.gal.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.ipeirotis.gal.Helper;
import com.ipeirotis.gal.core.AssignedLabel;
import com.ipeirotis.gal.core.Category;
import com.ipeirotis.gal.core.ConfusionMatrix;
import com.ipeirotis.gal.core.CorrectLabel;
import com.ipeirotis.gal.core.Datum;
import com.ipeirotis.gal.core.Datum.ClassificationMethod;
import com.ipeirotis.gal.core.MisclassificationCost;
import com.ipeirotis.gal.core.Worker;
import com.ipeirotis.gal.decorator.FieldAccessors;
import com.ipeirotis.gal.decorator.FieldAccessors.FieldAccessor;

public class DawidSkene {

	private Map<String, Category> categories;

	private Boolean fixedPriors;

	private Map<String, Datum> objects;
	private Map<String, Worker> workers;

	private Collection<FieldAccessor> datumFieldAccessors;

	public Collection<FieldAccessor> getFieldAccessors(Class<?> entityClass) {
		if (Datum.class.isAssignableFrom(entityClass)) {
			return datumFieldAccessors;
		} else if (Worker.class.isAssignableFrom(entityClass)) {
			return FieldAccessors.WORKER_ACCESSORS.getFieldAccessors(this);
		}

		return null;
	}

	public DawidSkene(Set<Category> categories) {

		this.objects = new TreeMap<String, Datum>();
		this.workers = new TreeMap<String, Worker>();

		this.fixedPriors = false;
		this.categories = new HashMap<String, Category>();

		for (Category c : categories) {
			this.categories.put(c.getName(), c);
			if (c.hasPrior()) {
				this.fixedPriors = true;
			}
		}

		datumFieldAccessors = FieldAccessors.DATUM_ACCESSORS.getFieldAccessors(this);

		// We initialize the priors to be uniform across classes
		// if the user did not pass any information about the prior values
		if (!fixedPriors)
			initializePriors();

		// By default, we initialize the misclassification costs
		// assuming a 0/1 loss function. The costs can be customized
		// using the corresponding file
		initializeCosts();
	}

	public Double getLogLikelihood() {
		double result = 0;
		
		for (Datum d : this.objects.values()) {
			for (AssignedLabel al: d.getAssignedLabels()) {
				String workerName = al.getWorkerName();
				String assignedLabel = al.getCategoryName();
				
				Map<String, Double> estimatedCorrectLabel = d.getProbabilityVector(ClassificationMethod.DS_Soft);
				
				for (String from: estimatedCorrectLabel.keySet()) {
					Worker w = this.getWorkers().get(workerName);
					Double categoryProbability = estimatedCorrectLabel.get(from);
					Double labelingProbability = w.getConfusionMatrix().getErrorRate(from, assignedLabel);
					if (categoryProbability == 0.0 || Double.isNaN(labelingProbability) || labelingProbability == 0.0 ) 
						continue; 
					else
						result += Math.log(categoryProbability) + Math.log(labelingProbability);
				}
			}
		}
		
		
		return result;
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
			d = new Datum(objectName, this);
			this.objects.put(objectName, d);
		}

		d.addAssignedLabel(al);

		// If we already have the worker, then just add the label
		// in the set of labels assigned by the worker.
		// If it is the first time we see the object, then create
		// the appropriate entry in the objects hashmap
		Worker w;
		if (this.workers.containsKey(workerName)) {
			w = this.workers.get(workerName);
		} else {
			w = new Worker(workerName, this);
		}
		
		w.addAssignedLabel(al);
		this.workers.put(workerName, w);

	}

	public void addCorrectLabel(CorrectLabel cl) {

		String objectName = cl.getObjectName();
		String correctCategory = cl.getCorrectCategory();

		Datum d = objects.get(objectName);

		if (null == d) {
			d = new Datum(objectName, this);
			this.objects.put(objectName, d);
		}

		d.setGold(true);
		d.setGoldCategory(correctCategory);
	}

	public void addEvaluationLabel(CorrectLabel cl) {
		String objectName = cl.getObjectName();
		String correctCategory = cl.getCorrectCategory();
		Datum d = this.objects.get(objectName);
		assert (d != null); // All objects in the evaluation should be rated by
							// at least one worker
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
	 * Runs the algorithm, iterating until convergence, i.e., the difference
	 * in the log likelihood between two consecutive iterations is lower
	 * than the specified threshold epsilon, or until executing more than maxIterations
	 * 
	 * @param maxIterations 
	 */
	public double estimate(int maxIterations, double epsilon) {
		
		double pastLogLikelihood = Double.POSITIVE_INFINITY;
		double logLikelihood = 0d;
		
		int cnt = 0;
		
		
		while (cnt <maxIterations && Math.abs(logLikelihood - pastLogLikelihood) > epsilon) {
			cnt++;
			pastLogLikelihood = getLogLikelihood();
			updateObjectClassProbabilities();
			updatePriors();
			updateWorkerConfusionMatrices();
			logLikelihood = getLogLikelihood();
			System.out.println(cnt + "\t" + logLikelihood);
		}

		datumFieldAccessors = FieldAccessors.DATUM_ACCESSORS.getFieldAccessors(this);
		
		return logLikelihood;
	}

	private HashMap<String, Double> getObjectClassProbabilities(
			String objectName, String workerToIgnore) {

		HashMap<String, Double> result = new HashMap<String, Double>();

		Datum d = this.objects.get(objectName);

		// If this is a gold example, just put the probability estimate to be
		// 1.0
		// for the correct class
		if (d.isGold()) {
			for (String category : this.categories.keySet()) {
				String correctCategory = d.getGoldCategory();
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
				if (workerToIgnore != null
						&& w.getName().equals(workerToIgnore))
					continue;

				String assigned_category = al.getCategoryName();
				double evidence_for_category = w.getErrorRate(
						category.getName(), assigned_category);
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
				Double probability = Helper.round(nominator / denominator, 5);
				result.put(category, probability);
			}
		}
		return result;

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
	 * @param engine
	 *            .getCategories()
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
		for (AssignedLabel l : w.getAssignedLabels()) {

			String objectName = l.getObjectName();
			Datum d = this.objects.get(objectName);
			assert (d != null);
			if (!d.isEvaluation())
				continue;

			String assignedCategory = l.getCategoryName();
			String correctCategory = d.getEvaluationCategory();

			// Double currentCount = eval_cm.getErrorRate(correctCategory,
			// assignedCategory);
			eval_cm.addError(correctCategory, assignedCategory, 1.0);
		}
		eval_cm.normalize();
		w.setEvalConfusionMatrix(eval_cm);
	}
	

	public Integer countGoldTests(Set<AssignedLabel> labels) {
		Integer result = 0;
		for (AssignedLabel al : labels) {
			String name = al.getObjectName();
			Datum d = this.objects.get(name);
			if (d.isGold())
				result++;
		}
		return result;
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
		HashMap<String, Double> probabilities = getObjectClassProbabilities(
				objectName, null);
		if (probabilities == null)
			return;
		for (String category : probabilities.keySet()) {
			Double probability = probabilities.get(category);
			d.setCategoryProbability(category, probability);
		}
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
				Double objectProb = d.getCategoryProbability(
						Datum.ClassificationMethod.DS_Soft, c);
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
			HashMap<String, Double> probabilities = this
					.getObjectClassProbabilities(objectName, workerName);
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

	public Map<String, Category> getCategories() {
		return categories;
	}

	public Boolean getFixedPriors() {
		return fixedPriors;
	}

	public Map<String, Datum> getObjects() {
		return objects;
	}

	public Map<String, Worker> getWorkers() {
		return workers;
	}
}

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
package com.ipeirotis.gal.engine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.ipeirotis.gal.Helper;
import com.ipeirotis.gal.algorithms.DawidSkene;
import com.ipeirotis.gal.core.AssignedLabel;
import com.ipeirotis.gal.core.Category;
import com.ipeirotis.gal.core.CorrectLabel;
import com.ipeirotis.gal.core.MisclassificationCost;
import com.ipeirotis.gal.engine.rpt.CategoryPriorsReport;
import com.ipeirotis.gal.engine.rpt.ConfusionMatrixReport;
import com.ipeirotis.gal.engine.rpt.ObjectResultReport;
import com.ipeirotis.gal.engine.rpt.Report;
import com.ipeirotis.gal.engine.rpt.ReportingContext;
import com.ipeirotis.gal.engine.rpt.SummaryReport;
import com.ipeirotis.gal.engine.rpt.WorkerQualityReport;

public class Engine {
	private Set<Category> categories;

	private DawidSkene ds;

	private Set<MisclassificationCost> costs;

	private Set<AssignedLabel> labels;

	private Set<CorrectLabel> correct;

	private Set<CorrectLabel> evaluation;

	private EngineContext ctx;

	private ReportingContext rptCtx;

	private Set<Report> reports = new LinkedHashSet<Report>();

	public Engine(EngineContext ctx) {
		this.ctx = ctx;
		this.rptCtx = new ReportingContext(this);
		this.reports.addAll(Arrays.asList(new WorkerQualityReport(),
				new ObjectResultReport()));
	}
	
	public EngineContext getEngineContext() {
		return ctx;
	}

	public Set<Category> getCategories() {
		return categories;
	}

	public void setCategories(Set<Category> categories) {
		this.categories = categories;
	}

	public DawidSkene getDs() {
		return ds;
	}

	public void setDawidSkene(DawidSkene ds) {
		this.ds = ds;
	}

	public Set<MisclassificationCost> getCosts() {
		return costs;
	}

	public void setCosts(Set<MisclassificationCost> costs) {
		this.costs = costs;
	}

	public Set<AssignedLabel> getLabels() {
		return labels;
	}

	public void setLabels(Set<AssignedLabel> labels) {
		this.labels = labels;
	}

	public Set<CorrectLabel> getCorrect() {
		return correct;
	}

	public void setCorrect(Set<CorrectLabel> correct) {
		this.correct = correct;
	}

	public Set<CorrectLabel> getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(Set<CorrectLabel> evaluation) {
		this.evaluation = evaluation;
	}

	public void execute() {
		setCategories(loadCategories(ctx.getCategoriesFile()));

		setDawidSkene(new DawidSkene(getCategories()));
		
		if (getDs().fixedPriors() == true)
			println("Using fixed priors.");
		else
			println("Using data-inferred priors.");

		if (ctx.hasCosts()) {
			setCosts(loadCosts(ctx.getCostFile()));

			for (MisclassificationCost mcc : getCosts()) {
				getDs().addMisclassificationCost(mcc);
			}
		}

		setLabels(loadWorkerAssignedLabels(ctx.getInputFile()));

		int al = 0;

		for (AssignedLabel l : getLabels()) {
			if (++al % 1000 == 0)
				print(".");
			getDs().addAssignedLabel(l);
		}
		println("%d worker-assigned labels loaded.", getLabels().size());

		if (ctx.hasGoldFile()) {
			setCorrect(loadGoldLabels(ctx.getGoldFile()));

			int cl = 0;
			for (CorrectLabel l : getCorrect()) {
				if (++cl % 1000 == 0)
					print(".");
				getDs().addCorrectLabel(l);
			}
			println("%d correct labels loaded.", getCorrect().size());
		}

		if (ctx.hasEvaluations()) {
			setEvaluation(loadEvaluationLabels(ctx.getEvaluationFile()));
			int el = 0;
			for (CorrectLabel l : getEvaluation()) {
				if (++el % 1000 == 0)
					print(".");
				getDs().addEvaluationLabel(l);
			}
			println(getEvaluation().size() + " evaluation labels loaded.");
		}

		// We compute the evaluation-based confusion matrix for the workers
		getDs().evaluateWorkers();

		println("");
		println("Running the Dawid&Skene algorithm");
		double epsilon = ctx.getEpsilon();
		int maxIterations = ctx.getNumIterations();
		double ll = getDs().getLogLikelihood();
		println("Initial Log-likelihood: %3.6f", ll);
		ll = getDs().estimate(maxIterations, epsilon);
		println("Final Log-likelihood: %3.6f", ll);
		println("Done\n");

		if (ctx.hasEvaluateResultsAgainstFile()) {
			rptCtx.setExpectedEvaluation(loadGoldLabels(ctx
					.getEvaluateResultsAgainstFile()));
		}

		executeReports();
	}

	private void executeReports() {
		if (!ctx.isDryRun()) {
			reports.add(new CategoryPriorsReport());
		}
		
		reports.add(new SummaryReport());
		reports.add(new ConfusionMatrixReport());
		
		try {
			File outputDir = new File("results");
			
			if (! outputDir.exists())
				outputDir.mkdir();
			
			
			for (Report report : reports) {
				report.execute(rptCtx);
			}
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	/**
	 * @param correctfile
	 * @return
	 */
	private Set<CorrectLabel> loadGoldLabels(String correctfile) {
		// We load the "gold" cases (if any)
		println("");
		println("Loading file with correct labels. ");
		String[] lines_correct = Helper.readFile(correctfile).split("\n");
		println("File contained %d entries.", lines_correct.length);
		Set<CorrectLabel> correct = loadCorrectLabels(lines_correct);
		return correct;
	}

	/**
	 * @param evalfile
	 * @return
	 */
	private Set<CorrectLabel> loadEvaluationLabels(String evalfile) {

		// We load the "gold" cases (if any)
		println("");
		println("Loading file with evaluation labels. ");
		String[] lines_correct = Helper.readFile(evalfile).split("\n");
		println("File contained %d entries.", lines_correct.length);
		Set<CorrectLabel> correct = loadEvaluationLabels(lines_correct);
		return correct;
	}

	public Set<AssignedLabel> loadAssignedLabels(String[] lines) {

		Set<AssignedLabel> labels = new HashSet<AssignedLabel>();
		int cnt = 1;
		for (String line : lines) {
			String[] entries = line.split("\t");
			if (entries.length != 3) {
				throw new IllegalArgumentException(
						"Error while loading from assigned labels file (line #"
								+ cnt + "): " + line);
			}
			cnt++;

			String workername = entries[0];
			String objectname = entries[1];
			String categoryname = entries[2];

			AssignedLabel al = new AssignedLabel(workername, objectname,
					categoryname);
			labels.add(al);
		}
		return labels;
	}

	public Set<Category> loadCategories(String[] lines) {

		Set<Category> categories = new HashSet<Category>();
		for (String line : lines) {
			// First we check if we have fixed priors or not
			// If we have fixed priors, we have a TAB character
			// after the name of each category, followed by the prior value
			String[] l = line.split("\t");
			if (l.length == 1) {
				Category c = new Category(line);
				categories.add(c);
			} else if (l.length == 2) {
				String name = l[0];
				Double prior = new Double(l[1]);
				Category c = new Category(name);
				c.setPrior(prior);
				categories.add(c);
			}
		}
		return categories;
	}

	public Set<MisclassificationCost> loadClassificationCost(String[] lines) {

		Set<MisclassificationCost> labels = new HashSet<MisclassificationCost>();
		int cnt = 1;
		for (String line : lines) {
			String[] entries = line.split("\t");
			if (entries.length != 3) {
				throw new IllegalArgumentException(
						"Error while loading from assigned labels file (line "
								+ cnt + "):" + line);
			}
			cnt++;

			String from = entries[0];
			String to = entries[1];
			Double cost = Double.parseDouble(entries[2]);

			MisclassificationCost mcc = new MisclassificationCost(from, to,
					cost);
			labels.add(mcc);
		}
		return labels;
	}

	public Set<CorrectLabel> loadCorrectLabels(String[] lines) {

		Set<CorrectLabel> labels = new HashSet<CorrectLabel>();
		int cnt = 1;
		for (String line : lines) {
			String[] entries = line.split("\t");
			if (entries.length != 2) {
				throw new IllegalArgumentException(
						"Error while loading from correct labels file (line "
								+ cnt + "):" + line);
			}
			cnt++;

			String objectname = entries[0];
			String categoryname = entries[1];

			CorrectLabel cl = new CorrectLabel(objectname, categoryname);
			labels.add(cl);
		}
		return labels;
	}

	public Set<CorrectLabel> loadEvaluationLabels(String[] lines) {

		Set<CorrectLabel> labels = new HashSet<CorrectLabel>();
		for (String line : lines) {
			String[] entries = line.split("\t");
			if (entries.length != 2) {
				// evaluation file is optional
				break;
			}

			String objectname = entries[0];
			String categoryname = entries[1];

			CorrectLabel cl = new CorrectLabel(objectname, categoryname);
			labels.add(cl);
		}
		return labels;
	}

	/**
	 * @param inputfile
	 * @return
	 */
	private Set<AssignedLabel> loadWorkerAssignedLabels(String inputfile) {

		// We load the labels assigned by the workers on the different objects
		println("");
		println("Loading file with assigned labels. ");
		String[] lines_input = Helper.readFile(inputfile).split("\n");
		println("File contains " + lines_input.length + " entries.");
		Set<AssignedLabel> labels = loadAssignedLabels(lines_input);
		return labels;
	}

	/**
	 * @param costfile
	 * @return
	 */
	private Set<MisclassificationCost> loadCosts(String costfile) {

		// We load the cost file. The file should have exactly n^2 lines
		// where n is the number of categories.
		println("");
		println("Loading cost file.");
		String[] lines_cost = Helper.readFile(costfile).split("\n");
		// assert (lines_cost.length == categories.size() * categories.size());
		println("File contains " + lines_cost.length + " entries.");
		Set<MisclassificationCost> costs = loadClassificationCost(lines_cost);
		return costs;
	}

	/**
	 * @param categoriesfile
	 * @return
	 */
	private Set<Category> loadCategories(String categoriesfile) {
		println("");
		println("Loading categories file.");
		String[] lines_categories = Helper.readFile(categoriesfile).split("\n");
		println("File contains " + lines_categories.length + " categories.");
		Set<Category> categories = loadCategories(lines_categories);
		return categories;
	}

	public void println(String mask, Object... args) {
		print(mask + "\n", args);
	}

	public void print(String mask, Object... args) {
		if (!ctx.isVerbose())
			return;

		String message;

		if (args.length > 0) {
			message = String.format(mask, args);
		} else {
			// without format arguments, print the mask/string as-is
			message = mask;
		}

		System.out.println(message);
	}
}

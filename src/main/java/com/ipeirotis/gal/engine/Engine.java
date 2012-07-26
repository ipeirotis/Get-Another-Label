package com.ipeirotis.gal.engine;

import java.util.HashSet;
import java.util.Set;

import com.ipeirotis.gal.scripts.AssignedLabel;
import com.ipeirotis.gal.scripts.Category;
import com.ipeirotis.gal.scripts.CorrectLabel;
import com.ipeirotis.gal.scripts.DawidSkene;
import com.ipeirotis.gal.scripts.MisclassificationCost;
import com.ipeirotis.utils.Utils;

public class Engine {
	private Set<Category> categories;

	private DawidSkene ds;

	private Set<MisclassificationCost> costs;

	private Set<AssignedLabel> labels;

	private Set<CorrectLabel> correct;

	private Set<CorrectLabel> evaluation;

	private EngineContext ctx;

	public Engine(EngineContext ctx) {
		this.ctx = ctx;
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

	public void setDs(DawidSkene ds) {
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

		setDs(new DawidSkene(getCategories()));
		if (getDs().fixedPriors() == true)
			println("Using fixed priors.");
		else
			println("Using data-inferred priors.");

		setCosts(loadCosts(ctx.getCostFile()));

		assert (getCosts().size() == getCategories().size() * getCategories().size());

		for (MisclassificationCost mcc : getCosts()) {
			getDs().addMisclassificationCost(mcc);
		}

		setLabels(loadWorkerAssignedLabels(ctx.getInputFile()));

		int al = 0;

		for (AssignedLabel l : getLabels()) {
			if (++al % 1000 == 0)
				print(".");
			getDs().addAssignedLabel(l);
		}
		println("%d worker-assigned labels loaded.", getLabels().size());

		setCorrect(loadGoldLabels(ctx.getCorrectFile()));

		int cl = 0;
		for (CorrectLabel l : getCorrect()) {
			if (++cl % 1000 == 0)
				print(".");
			getDs().addCorrectLabel(l);
		}
		println("%d correct labels loaded.", getCorrect().size());

		setEvaluation(loadEvaluationLabels(ctx.getEvaluationFile()));
		int el = 0;
		for (CorrectLabel l : getEvaluation()) {
			if (++el % 1000 == 0)
				print(".");
			getDs().addEvaluationLabel(l);
		}
		println(getEvaluation().size() + " evaluation labels loaded.");

		// We compute the evaluation-based confusion matrix for the workers
		getDs().evaluateWorkers();

		//ds.estimate(1);
		//HashMap<String, String> prior_voting = saveMajorityVote(verbose, ds);

		println("");
		println("Running the Dawid&Skene algorithm");
		for (int i = 0; i < ctx.getNumIterations(); i++) {
			println("Iteration: %d", i);
			// ds.estimate(iterations);
			getDs().estimate(1);
		}
		println("Done\n");

		saveWorkerQuality(getDs());

		saveObjectResults(getDs());

		saveCategoryPriors(getDs());

		//HashMap<String, String> posterior_voting = saveDawidSkeneVote(verbose, ds);

		//saveDifferences(verbose, ds, prior_voting, posterior_voting);
	}


	/*
	private static void saveDifferences(boolean verbose, DawidSkene ds, HashMap<String, String> prior_voting,
			HashMap<String, String> posterior_voting) {

		println("");
		System.out
				.println("Computing the differences between naive majority vote and Dawid&Skene (see also file results/differences-with-majority-vote.txt)");
		String differences = ds.printDiffVote(prior_voting, posterior_voting);
		if (verbose) {
			println("=======DIFFERENCES WITH MAJORITY VOTE========");
			println(differences);
			println("=============================================");
		}
		Utils.writeFile(differences, "results/differences-with-majority-vote.txt");
	}
	*/

	/**
	 * @param verbose
	 * @param ds
	 * @return
	 */
	/*
	private static HashMap<String, String> saveDawidSkeneVote(boolean verbose, DawidSkene ds) {

		// Save the vote after the D&S estimation
		println("");
		println("Estimating the Dawid & Skene object labels (see also file results/dawid-skene-results.txt)");
		HashMap<String, String> posterior_voting = ds.getMajorityVote();
		String dawidskene = ds.printVote();
		if (verbose) {
			println("=======DAWID&SKENE RESULTS========");
			println(dawidskene);
			println("==================================");
		}
		Utils.writeFile(dawidskene, "results/dawid-skene-results.txt");
		return posterior_voting;
	}
	*/

	/**
	 * @param ds
	 */
	private void saveCategoryPriors(DawidSkene ds) {

		// Save the probability that an object belongs to each class
		println("");
		println("Printing prior probabilities (see also file results/priors.txt)");
		String priors = ds.printPriors();
		if (ctx.isVerbose()) {
			println("=======PRIOR PROBABILITIES========");
			println(priors);
			println("==================================");
		}
		Utils.writeFile(priors, "results/priors.txt");
	}

	/**
	 * @param ds
	 */
	private void saveObjectResults(DawidSkene ds) {

		// Save the probability that an object belongs to each class
		println("");
		println("Printing category probabilities for objects (see also file results/object-probabilities.txt)");
		String objectProbs = ds.printObjectClassProbabilities();
		if (ctx.isVerbose()) {
			println("=======CATEGORY PROBABILITIES========");
			println(objectProbs);
			println("=====================================");
		}
		Utils.writeFile(objectProbs, "results/object-probabilities.txt");
	}

	/**
	 * @param ds
	 */
	private void saveWorkerQuality(DawidSkene ds) {

		// Save the estimated quality characteristics for each worker
		println("");
		print("Estimating worker quality");
		System.out
				.println(" (see also file results/worker-statistics-summary.txt and results/worker-statistics-detailed.txt)");
		boolean detailed = false;
		String summary_report = ds.printAllWorkerScores(detailed);
		detailed = true;
		String detailed_report = ds.printAllWorkerScores(detailed);
		if (ctx.isVerbose()) {
			println("=======WORKER QUALITY STATISTICS=======");
			println(summary_report);
			println("=======================================");
		}
		Utils.writeFile(summary_report, "results/worker-statistics-summary.txt");
		Utils.writeFile(detailed_report, "results/worker-statistics-detailed.txt");
	}

	/**
	 * @param verbose
	 * @param ds
	 * @return
	 */
	/*
	private static HashMap<String, String> saveMajorityVote(boolean verbose, DawidSkene ds) {

		// Save the majority vote before the D&S estimation
		println("");
		println("Estimating the naive majority vote (see also file results/naive-majority-vote.txt)");
		HashMap<String, String> prior_voting = ds.getMajorityVote();
		String majority = ds.printVote();
		if (verbose) {
			println("=======NAIVE MAJORITY VOTE========");
			println(majority);
			println("==================================");
		}
		Utils.writeFile(majority, "results/naive-majority-vote.txt");
		return prior_voting;
	}*/

	/**
	 * @param correctfile
	 * @return
	 */
	private Set<CorrectLabel> loadGoldLabels(String correctfile) {

		// We load the "gold" cases (if any)
		println("");
		println("Loading file with correct labels. ");
		String[] lines_correct = Utils.getFile(correctfile).split("\n");
		println("File contained %d entries.", lines_correct.length);
		Set<CorrectLabel> correct = getCorrectLabels(lines_correct);
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
        String[] lines_correct = Utils.getFile(evalfile).split("\n");
        println("File contained %d entries.", lines_correct.length);
        Set<CorrectLabel> correct = getEvaluationLabels(lines_correct);
        return correct;
    }

	public Set<AssignedLabel> getAssignedLabels(String[] lines) {

		Set<AssignedLabel> labels = new HashSet<AssignedLabel>();
		int cnt = 1;
		for (String line : lines) {
			String[] entries = line.split("\t");
			if (entries.length != 3) {
				throw new IllegalArgumentException("Error while loading from assigned labels file (line #" + cnt + "): " + line);
			}
			cnt++;

			String workername = entries[0];
			String objectname = entries[1];
			String categoryname = entries[2];

			AssignedLabel al = new AssignedLabel(workername, objectname, categoryname);
			labels.add(al);
		}
		return labels;
	}

	public Set<Category> getCategories(String[] lines) {

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

	public Set<MisclassificationCost> getClassificationCost(String[] lines) {

		Set<MisclassificationCost> labels = new HashSet<MisclassificationCost>();
		int cnt = 1;
		for (String line : lines) {
			String[] entries = line.split("\t");
			if (entries.length != 3) {
				throw new IllegalArgumentException("Error while loading from assigned labels file (line " + cnt + "):" + line);
			}
			cnt++;

			String from = entries[0];
			String to = entries[1];
			Double cost = Double.parseDouble(entries[2]);

			MisclassificationCost mcc = new MisclassificationCost(from, to, cost);
			labels.add(mcc);
		}
		return labels;
	}

	public Set<CorrectLabel> getCorrectLabels(String[] lines) {

		Set<CorrectLabel> labels = new HashSet<CorrectLabel>();
		int cnt = 1;
		for (String line : lines) {
			String[] entries = line.split("\t");
			if (entries.length != 2) {
				throw new IllegalArgumentException("Error while loading from correct labels file (line " + cnt + "):" + line);
			}
			cnt++;

			String objectname = entries[0];
			String categoryname = entries[1];

			CorrectLabel cl = new CorrectLabel(objectname, categoryname);
			labels.add(cl);
		}
		return labels;
	}

    public Set<CorrectLabel> getEvaluationLabels(String[] lines) {

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
		String[] lines_input = Utils.getFile(inputfile).split("\n");
		println("File contains " + lines_input.length + " entries.");
		Set<AssignedLabel> labels = getAssignedLabels(lines_input);
		return labels;
	}

	/**
	 * @param costfile
	 * @return
	 */
	private Set<MisclassificationCost> loadCosts(String costfile) {

		// We load the cost file. The file should have exactly n^2 lines
		// where n is the number of categories.
		// TODO: Later, we can also allow an empty file, and assume a default 0/1 loss function.
		println("");
		println("Loading cost file.");
		String[] lines_cost = Utils.getFile(costfile).split("\n");
		// assert (lines_cost.length == categories.size() * categories.size());
		println("File contains " + lines_cost.length + " entries.");
		Set<MisclassificationCost> costs = getClassificationCost(lines_cost);
		return costs;
	}

	/**
	 * @param categoriesfile
	 * @return
	 */
	private Set<Category> loadCategories(String categoriesfile) {
		println("");
		println("Loading categories file.");
		String[] lines_categories = Utils.getFile(categoriesfile).split("\n");
		println("File contains " + lines_categories.length + " categories.");
		Set<Category> categories = getCategories(lines_categories);
		return categories;
	}

	public void println(String mask, Object... args) {
		print(mask + "\n", args);
	}

	public void print(String mask, Object... args) {
		if (! ctx.isVerbose())
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
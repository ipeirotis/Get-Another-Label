package com.ipeirotis.gal.scripts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.ipeirotis.utils.Utils;

public class Main {

	public static void main(String[] args) {

		// TODO: Use a "getopt"-like library to process options
		// TODO: Have a "verbose" option that

		String categoriesfile = "";
		String inputfile = "";
		String correctfile = "";
		String costfile = "";
		int iterations = 0;
		boolean verbose = false;

		if (args.length != 5) {
			System.out
					.println("Usage: java -jar getanotherlabel.jar <categoriesfile> <inputfile> <correctfile> <costfile> <iterations>");
			System.out.println("");
			System.out
					.println("Example: java -jar getanotherlabel.jar data\\categories.txt data\\unlabeled.txt data\\labeled.txt  data\\costs.txt 10");
			System.out.println("");
			System.out
					.println("The <categoriesfile> is a text file and contains the list of categories used to annotate the objects.");
			System.out.println("It contains one category per line.");
			System.out
					.println("The <categoriesfile> can also be used to define the prior values for the different categories, instead of letting the priors be defined by the data.");
			System.out
					.println("In that case, it becomes a tab-separated file and each line has the form <category><tab><prior>");
			System.out.println("");
			System.out.println("The <inputfile> is a tab-separated text file.");
			System.out.println("Each line has the form <workerid><tab><objectid><tab><assigned_label>");
			System.out.println("and records the label that the given worker gave to that object");
			System.out.println("");
			System.out.println("The <correctfile> is a tab-separated text file.");
			System.out.println("Each line has the form <objectid><tab><assigned_label>");
			System.out.println("and records the correct labels for whatever objects we have them.");
			System.out.println("");
			System.out.println("The <costfile> is a tab-separated text file.");
			System.out.println("Each line has the form <from_class><tab><to_class><tab><classification_cost>");
			System.out.println("and records the classification cost of classifying an object that");
			System.out.println("belongs to the `from_class` into the `to_class`.");
			System.out.println("");
			System.out.println("<iterations> is the number of times to run the algorithm. Even a value of 1 works well.");
			System.exit(-1);
		} else {
			categoriesfile = args[0];
			inputfile = args[1];
			correctfile = args[2];
			costfile = args[3];
			iterations = Integer.parseInt(args[4]);
		}

		// We start by defining the set of categories in which the DS algorithm
		// will operate. We do this first, so that we can initialize properly
		// the confusion matrixes of the workers, the probability vectors for
		// the objects etc. While it is possible to modify these later on, when we
		// see new categories, it is a PITA and leads to many bugs, especially
		// in an environment where there is persistence of the objects.
		// Plus, it makes it easier to implement the algorithm in a streaming mode.

		Set<Category> categories = loadCategories(categoriesfile);

		DawidSkene ds = new DawidSkene(categories);
		if (ds.fixedPriors() == true)
			System.out.println("Using fixed priors.");
		else
			System.out.println("Using data-inferred priors.");

		Set<MisclassificationCost> costs = loadCosts(costfile);
		assert (costs.size() == categories.size() * categories.size());
		for (MisclassificationCost mcc : costs) {
			ds.addMisclassificationCost(mcc);
		}

		Set<AssignedLabel> labels = loadWorkerAssignedLabels(inputfile);
		int al = 0;
		for (AssignedLabel l : labels) {
			if (++al % 1000 == 0)
				System.out.print(".");
			ds.addAssignedLabel(l);
		}
		System.out.println(labels.size() + " worker-assigned labels loaded.");

		Set<CorrectLabel> correct = loadGoldLabels(correctfile);
		int cl = 0;
		for (CorrectLabel l : correct) {
			if (++cl % 1000 == 0)
				System.out.print(".");
			ds.addCorrectLabel(l);
		}
		System.out.println(correct.size() + " correct labels loaded.");

		ds.estimate(1);
		HashMap<String, String> prior_voting = saveMajorityVote(verbose, ds);

		System.out.println("");
		System.out.println("Running the Dawid&Skene algorithm");
		for (int i = 0; i < iterations; i++) {
			System.out.println("Iteration: " + i);
			// ds.estimate(iterations);
			ds.estimate(1);
		}
		System.out.println("Done\n");

		saveWorkerQuality(verbose, ds);

		saveObjectResults(verbose, ds);

		saveCategoryPriors(verbose, ds);

		HashMap<String, String> posterior_voting = saveDawidSkeneVote(verbose, ds);

		saveDifferences(verbose, ds, prior_voting, posterior_voting);

	}

	/**
	 * @param verbose
	 * @param ds
	 * @param prior_voting
	 * @param posterior_voting
	 */
	private static void saveDifferences(boolean verbose, DawidSkene ds, HashMap<String, String> prior_voting,
			HashMap<String, String> posterior_voting) {

		System.out.println("");
		System.out
				.println("Computing the differences between naive majority vote and Dawid&Skene (see also file results/differences-with-majority-vote.txt)");
		String differences = ds.printDiffVote(prior_voting, posterior_voting);
		if (verbose) {
			System.out.println("=======DIFFERENCES WITH MAJORITY VOTE========");
			System.out.println(differences);
			System.out.println("=============================================");
		}
		Utils.writeFile(differences, "results/differences-with-majority-vote.txt");
	}

	/**
	 * @param verbose
	 * @param ds
	 * @return
	 */
	private static HashMap<String, String> saveDawidSkeneVote(boolean verbose, DawidSkene ds) {

		// Save the vote after the D&S estimation
		System.out.println("");
		System.out.println("Estimating the Dawid & Skene object labels (see also file results/dawid-skene-results.txt)");
		HashMap<String, String> posterior_voting = ds.getMajorityVote();
		String dawidskene = ds.printVote();
		if (verbose) {
			System.out.println("=======DAWID&SKENE RESULTS========");
			System.out.println(dawidskene);
			System.out.println("==================================");
		}
		Utils.writeFile(dawidskene, "results/dawid-skene-results.txt");
		return posterior_voting;
	}

	/**
	 * @param verbose
	 * @param ds
	 */
	private static void saveCategoryPriors(boolean verbose, DawidSkene ds) {

		// Save the probability that an object belongs to each class
		System.out.println("");
		System.out.println("Printing prior probabilities (see also file results/priors.txt)");
		String priors = ds.printPriors();
		if (verbose) {
			System.out.println("=======PRIOR PROBABILITIES========");
			System.out.println(priors);
			System.out.println("==================================");
		}
		Utils.writeFile(priors, "results/priors.txt");
	}

	/**
	 * @param verbose
	 * @param ds
	 */
	private static void saveObjectResults(boolean verbose, DawidSkene ds) {

		// Save the probability that an object belongs to each class
		System.out.println("");
		System.out.println("Printing category probabilities for objects (see also file results/object-probabilities.txt)");
		String objectProbs = ds.printObjectClassProbabilities(0.0);
		if (verbose) {
			System.out.println("=======CATEGORY PROBABILITIES========");
			System.out.println(objectProbs);
			System.out.println("=====================================");
		}
		Utils.writeFile(objectProbs, "results/object-probabilities.txt");
	}

	/**
	 * @param verbose
	 * @param ds
	 */
	private static void saveWorkerQuality(boolean verbose, DawidSkene ds) {

		// Save the estimated quality characteristics for each worker
		System.out.println("");
		System.out.print("Estimating worker quality");
		System.out
				.println(" (see also file results/worker-statistics-summary.txt and results/worker-statistics-detailed.txt)");
		boolean detailed = false;
		String summary_report = ds.printAllWorkerScores(detailed);
		detailed = true;
		String detailed_report = ds.printAllWorkerScores(detailed);
		if (verbose) {
			System.out.println("=======WORKER QUALITY STATISTICS=======");
			System.out.println(summary_report);
			System.out.println("=======================================");
		}
		Utils.writeFile(summary_report, "results/worker-statistics-summary.txt");
		Utils.writeFile(detailed_report, "results/worker-statistics-detailed.txt");
	}

	/**
	 * @param verbose
	 * @param ds
	 * @return
	 */
	private static HashMap<String, String> saveMajorityVote(boolean verbose, DawidSkene ds) {

		// Save the majority vote before the D&S estimation
		System.out.println("");
		System.out.println("Estimating the naive majority vote (see also file results/naive-majority-vote.txt)");
		HashMap<String, String> prior_voting = ds.getMajorityVote();
		String majority = ds.printVote();
		if (verbose) {
			System.out.println("=======NAIVE MAJORITY VOTE========");
			System.out.println(majority);
			System.out.println("==================================");
		}
		Utils.writeFile(majority, "results/naive-majority-vote.txt");
		return prior_voting;
	}

	/**
	 * @param correctfile
	 * @return
	 */
	private static Set<CorrectLabel> loadGoldLabels(String correctfile) {

		// We load the "gold" cases (if any)
		System.out.println("");
		System.out.println("Loading file with correct labels. ");
		String[] lines_correct = Utils.getFile(correctfile).split("\n");
		System.out.println("File contained " + lines_correct.length + " entries.");
		Set<CorrectLabel> correct = getCorrectLabels(lines_correct);
		return correct;
	}

	public static Set<AssignedLabel> getAssignedLabels(String[] lines) {

		Set<AssignedLabel> labels = new HashSet<AssignedLabel>();
		int cnt = 1;
		for (String line : lines) {
			String[] entries = line.split("\t");
			if (entries.length != 3) {
				System.err.println("Error while loading from assigned labels file (line " + cnt + "):" + line);
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

	public static Set<Category> getCategories(String[] lines) {

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

	public static Set<MisclassificationCost> getClassificationCost(String[] lines) {

		Set<MisclassificationCost> labels = new HashSet<MisclassificationCost>();
		int cnt = 1;
		for (String line : lines) {
			String[] entries = line.split("\t");
			if (entries.length != 3) {
				System.err.println("Error while loading from assigned labels file (line " + cnt + "):" + line);
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

	public static Set<CorrectLabel> getCorrectLabels(String[] lines) {

		Set<CorrectLabel> labels = new HashSet<CorrectLabel>();
		int cnt = 1;
		for (String line : lines) {
			String[] entries = line.split("\t");
			if (entries.length != 2) {
				System.err.println("Error while loading from correct labels file (line " + cnt + "):" + line);
			}
			cnt++;

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
	private static Set<AssignedLabel> loadWorkerAssignedLabels(String inputfile) {

		// We load the labels assigned by the workers on the different objects
		System.out.println("");
		System.out.println("Loading file with assigned labels. ");
		String[] lines_input = Utils.getFile(inputfile).split("\n");
		System.out.println("File contains " + lines_input.length + " entries.");
		Set<AssignedLabel> labels = getAssignedLabels(lines_input);
		return labels;
	}

	/**
	 * @param costfile
	 * @return
	 */
	private static Set<MisclassificationCost> loadCosts(String costfile) {

		// We load the cost file. The file should have exactly n^2 lines
		// where n is the number of categories.
		// TODO: Later, we can also allow an empty file, and assume a default 0/1 loss function.
		System.out.println("");
		System.out.println("Loading cost file.");
		String[] lines_cost = Utils.getFile(costfile).split("\n");
		// assert (lines_cost.length == categories.size() * categories.size());
		System.out.println("File contains " + lines_cost.length + " entries.");
		Set<MisclassificationCost> costs = getClassificationCost(lines_cost);
		return costs;
	}

	/**
	 * @param categoriesfile
	 * @return
	 */
	private static Set<Category> loadCategories(String categoriesfile) {

		System.out.println("");
		System.out.println("Loading categories file.");
		String[] lines_categories = Utils.getFile(categoriesfile).split("\n");
		System.out.println("File contains " + lines_categories.length + " categories.");
		Set<Category> categories = getCategories(lines_categories);
		return categories;
	}
}

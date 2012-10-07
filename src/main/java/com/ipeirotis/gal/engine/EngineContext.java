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

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.kohsuke.args4j.Option;

public class EngineContext {
	@Option(name = "--categories", metaVar = "<categoriesfile>", required = true, usage = "The <categoriesfile> can also be used to define the prior values for the different categories, instead of letting the priors be defined by the data. In that case, it becomes a tab-separated file and each line has the form <category><tab><prior>")
	String categoriesFile = "";

	public String getCategoriesFile() {
		return categoriesFile;
	}

	public void setCategoriesFile(String categoriesfile) {
		this.categoriesFile = categoriesfile;
	}

	@Option(name = "--input", metaVar = "<inputfile>", required = true, usage = "A tab-separated text file. Each line has the form <workerid><tab><objectid><tab><assigned_label> and records the label that the given worker gave to that object")
	String inputFile = "";

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputfile) {
		this.inputFile = inputfile;
	}

	@Option(name = "--gold", metaVar = "<goldfile>", usage = "A tab-separated text file. Each line has the form <objectid><tab><gold_label> and records the gold label for whatever objects we have them.")
	String goldFile;

	public String getGoldFile() {
		return goldFile;
	}

	public boolean hasGoldFile() {
		return isNotBlank(goldFile);
	}

	public void setCorrectFile(String correctfile) {
		this.goldFile = correctfile;
	}

	
	
	
	@Option(name = "--cost", metaVar = "<costfile>", usage = "A tab-separated text file. Each line has the form <from_class><tab><to_class><tab><classification_cost> and records the classification cost of classifying an object thatbelongs to the `from_class` into the `to_class`.")
	String costFile;

	public String getCostFile() {
		return costFile;
	}

	public boolean hasCosts() {
		return isNotBlank(costFile);
	}

	public void setCostFile(String costfile) {
		this.costFile = costfile;
	}

	@Option(name = "--eval", metaVar = "<evaluationfile>", usage = "Evaluation File (TBD)")
	String evaluationFile;

	public String getEvaluationFile() {
		return evaluationFile;
	}

	public boolean hasEvaluations() {
		return isNotBlank(evaluationFile);
	}

	public void setEvaluationFile(String evaluationfile) {
		this.evaluationFile = evaluationfile;
	}

	@Option(name = "--iterations", usage = "is the maximum number of iterations to execute the EM algorithm. The default value of 50 often works well. No need to change it unless you believe that the algorithm is not converging.", metaVar = "<num-iterations>")
	int numIterations = 50;

	public int getNumIterations() {
		return numIterations;
	}

	public void setNumIterations(int iterations) {
		this.numIterations = iterations;
	}
	
	@Option(name = "--epsilon", usage = "the difference in the log-likelihood between two consecutive iterations of the EM algorithm. The default value of 10E-6 often works well. . No need to change it unless you believe that the algorithm is not converging.", metaVar = "<epsilon>")
	double epsilon = 10E-6;

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	@Option(name = "--verbose", usage = "Verbose Mode?")
	boolean verbose = false;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Option(name = "--dry-run", usage = "Dry Run (run, but not save results?)")
	boolean dryRun = false;

	public boolean isDryRun() {
		return dryRun;
	}

	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

	@Option(name = "--evaluate-results-against", metaVar = "<era-file>", usage = "File with the correct labels for the objects. Used to evaluate the outcome of the algorithm. (In constract to the gold-data file, these labels are never used by the algorithm during the estimation process.)")
	String evaluateResultsAgainstFile;

	public String getEvaluateResultsAgainstFile() {
		return evaluateResultsAgainstFile;
	}

	public boolean hasEvaluateResultsAgainstFile() {
		return isNotBlank(getEvaluateResultsAgainstFile());
	}

	public void setEvaluateResultsAgainstFile(String evaluateResultsAgainstFile) {
		this.evaluateResultsAgainstFile = evaluateResultsAgainstFile;
	}
}

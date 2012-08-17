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
package com.ipeirotis.gal.scripts;

import com.ipeirotis.utils.Stat;

public class Pricing {

	public static double getNormalizedP_simple(double q_exp) {

		return 0.5 + Math.sqrt(q_exp) / 2;
	}

	public static double getNormalizedQexp_simple(double p) {

		return Math.pow(2 * (p - 0.5), 2);
	}

	/**
	 * Takes as input the probability q of a single worker being correct, and the number of workers k that we use
	 * and returns the probability that the majority of the workers will return back the correct answer
	 * 
	 * @param p
	 *          The quality of an individual worker
	 * @param k
	 *          The number of independent workers
	 * @return The probability that the majority of the workers will return back the correct answer
	 */
	public static Double probabilityCorrect(Double p, Integer k) {

		if (k < 0)
			return null;
		if (k == 0)
			return 0.5;

		if (p > 1)
			return null;
		if (p < 0)
			return null;

		if (p == 0.5)
			return 0.5;
		if (p < 0.5) {
			p = 1 - p;
		}

		Double result = 0.0;
		int low = (int) Math.ceil(0.5 * k + 0.5);
		for (int i = low; i <= k; i++) {
			// Computing product with logs and then taking the exp, otherwise the Stat.binom over/under-flows.
			result += Math.exp(Stat.logBinom(k, i) + i * Math.log(p) + (k - i) * Math.log(1 - p));
		}

		// In pure math form, we could also multiple this factor with
		// ((1/2)*ceil((1/2)*k+1/2)-(1/2)*ceil((1/2)*k))
		if (k % 2 == 0) {
			result += Math.exp(Stat.logBinom(k, k / 2) + (k / 2) * Math.log(p) + (k / 2) * Math.log(1 - p));
		}

		return result;
	}

	public static Double pricingFactor(double from, double to) {

		// We do not handle any corner cases (e.g., to==1, or from<=0.5
		if (from <= 0.5 || from >= 1)
			return null;
		if (to <= 0.5 || to >= 1)
			return null;
		if (from == to)
			return 1.0;

		// We ensure that from is lower than to. If not, we reverse and return the inverse at the end
		boolean reverse = (from > to);
		if (reverse) {
			double temp = to;
			to = from;
			from = temp;
		}

		// First, let's find the minimum number of workers of quality "from" required to reach quality "to"
		// We do it very naively here, with a simple, linear search.
		// Can be done much more efficiently with a search that increases
		// the step over time, but for most cases our loops should return very small
		// numbers (less than 50) so it should not be a significant improvement
		int minK = 1;
		double prob = probabilityCorrect(from, minK);
		while (prob < to) {
			minK += 2; // No need to increase by 1, as groups of size 2k are the same as 2k-1
			prob = probabilityCorrect(from, minK);
		}

		// The logodds of the probabilityCorrect increase almost linearly with "min_k"
		// so we can find a good approximation of the factor by interpolating between
		// the values for min_k and min_k-1
		double l = Stat.logoggs(probabilityCorrect(from, minK - 2));
		double h = Stat.logoggs(probabilityCorrect(from, minK));
		double t = Stat.logoggs(to);

		// This strange formula is a trivially simple interpolation using the fact that
		// the sin of the triangle (l,min_k-2), (h,min_k), (l,min_k)
		// is the same as the one in the triangle (l,min_k-2), (t,min_k-2+x), (l,min_k-2+x)
		double x = 2 * (t - l) / (h - l);

		double result = minK - 2 + x;

		if (reverse) {
			result = 1 / result;
		}
		return result;

	}

	public static void main(String[] args) {

		// Let's say that we have a worker with Q_exp = q1 and we pay this worker $1
		// How much should we pay someone with Q_exp = q2?
		double q1 = 0.36;
		double q2 = 0.49;

		double p1 = getNormalizedP_simple(q1);
		double p2 = getNormalizedP_simple(q2);
		System.out.println("p1=" + p1);
		System.out.println("p2=" + p2);
		System.out.println("We need to pay $" + pricingFactor(p1, p2));

	}
}

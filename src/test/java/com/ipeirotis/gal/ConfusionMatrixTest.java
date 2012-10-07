package com.ipeirotis.gal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.ipeirotis.gal.core.Category;
import com.ipeirotis.gal.core.ConfusionMatrix;

public class ConfusionMatrixTest {
	private ConfusionMatrix cm;


	@Before
	public void before() {
		
		Collection<Category> categories = new HashSet<Category>();
		
		
		for (int i=1; i<5; i++) {
			Category c = new Category("cat"+i);
			categories.add(c);
		}
		
		cm = new ConfusionMatrix(categories);
		cm.empty();
		
		for (String from: cm.getCategoryNames()) {
			for (String to: cm.getCategoryNames()) {
				Double error = Math.random();
				if (from.equals(to)) cm.addError(from, to, 5*error);
				else cm.addError(from, to, error);
			}
		}
		cm.normalize();

	}

	@Test
	public void testGetAssignedLabel() throws Exception {
		
		Long N=10000L; // Number of trials in the Bernoulli
		
		
		for (String correct: cm.getCategoryNames()) {

				// We will count how many times we will classify correct into different categories
				HashMap<String, Long> results = new  HashMap<String, Long>();
				for (String c : cm.getCategoryNames()) {
					results.put(c, 0L);
				}
			
				// Running the Bernoulli experiment(s)
				for (int i=0; i<N; i++) {
					String classified = cm.getAssignedLabel(correct);
					Long cnt = results.get(classified);
					results.put(classified, cnt+1);
				}	
				
				for (String classified: cm.getCategoryNames()) {
					Double p = cm.getErrorRate(correct, classified);
					Long actual = results.get(classified);

					Double error = Helper.round(cm.getErrorRate(correct, classified), 4);
					System.out.print("CM["+correct+ "->"+classified+"]="+error);
					System.out.print("\tActual = "+actual);
					System.out.print("\tExpected = "+Math.round(N*p));
					System.out.print("\tErr = " + Helper.round(1.0*Math.abs(actual-Math.round(N*p))/Math.round(N*p),2));
					System.out.println("\tDiff = " + Math.abs(actual-Math.round(N*p)));
					
				}
				
			}
		}
	
}

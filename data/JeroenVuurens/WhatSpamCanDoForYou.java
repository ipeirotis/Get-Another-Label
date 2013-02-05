package TrecEval;

import com.ipeirotis.gal.scripts.AssignedLabel;
import com.ipeirotis.gal.scripts.Category;
import com.ipeirotis.gal.scripts.DawidSkene;
import com.ipeirotis.gal.scripts.MisclassificationCost;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;


public class WhatSpamCanDoForYou {
    static class Vote {
        int sequence;
        String unit;
        String worker;
        String label;
        
        Vote ( int sequence, String unit, String worker, String label) {
            this.sequence = sequence;
            this.unit = unit;
            this.worker = worker;
            this.label = label;
        }
    }
    
    
    public static void main(String args[]) throws FileNotFoundException {
        ArrayList<Vote> gtvotes = new ArrayList<Vote>();
        Scanner inputfile = new Scanner( new File("otherparticipantsvotes"));
        while ( inputfile.hasNextLine() ) {
            String line = inputfile.nextLine();
            String columns[] = line.split("\t");
            gtvotes.add( new Vote( 0, columns[0], columns[1], columns[2] ) );
        }
        
        Map<String, String> groundtruth = gal( gtvotes, Integer.MAX_VALUE );
        
        ArrayList<Vote> votes = new ArrayList<Vote>();
        inputfile = new Scanner( new File("votes"));
        while ( inputfile.hasNextLine() ) {
            String line = inputfile.nextLine();
            String columns[] = line.split("\t");
            votes.add( new Vote( Integer.parseInt(columns[0]), columns[1], columns[2], columns[3] ) );
        }
        
        for (int threshold = 1; threshold < 10; threshold++) { // the number of votes to use per question
           Map<String, String> results = gal( votes, threshold);
           int correct = 0, incorrect = 0;
           for ( Entry<String, String> result : results.entrySet() ) {
              String goldlabel = groundtruth.get( result.getKey() );
              if ( result.getValue().equals(goldlabel) )
                  correct++;
              else
                  incorrect++;
           }
           System.out.println( threshold + " " + correct / (double)(correct + incorrect) );
        }
    }
    


    public static Map<String, String> gal( ArrayList<Vote> votes, int threshold ) {
        Set<Category> categories = new HashSet<Category>();
        Category relevant = new Category("1");
        Category irrelevant = new Category("0");
        categories.add(relevant);
        categories.add(irrelevant);
        DawidSkene ds = new DawidSkene(categories);

        ds.addMisclassificationCost(new MisclassificationCost(relevant.getName(), relevant.getName(), 0.0));
        ds.addMisclassificationCost(new MisclassificationCost(irrelevant.getName(), irrelevant.getName(), 0.0));
        ds.addMisclassificationCost(new MisclassificationCost(relevant.getName(), irrelevant.getName(), 1.0));
        ds.addMisclassificationCost(new MisclassificationCost(irrelevant.getName(), relevant.getName(), 1.0));

        for (Vote vote : votes ) {
           if (vote.sequence <= threshold ) {
              AssignedLabel l = new AssignedLabel( vote.worker, vote.unit, vote.label);
              ds.addAssignedLabel(l);
            }
        }

        ds.estimate( 20 );
        
        return ds.getMajorityVote();
    }
}

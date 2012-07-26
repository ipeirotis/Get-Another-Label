package com.ipeirotis.gal.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import br.com.ingenieux.launchify.api.LaunchifyAs;

import com.ipeirotis.gal.engine.Engine;
import com.ipeirotis.gal.engine.EngineContext;

@LaunchifyAs("get-another-label")
public class Main {
	/**
	 * Main Entry Point
	 * 
	 * TODO: Split between Main and Engine for further reuse
	 * 
	 * @param args arguments
	 */
	public static void main(String[] args) {
		EngineContext ctx = new EngineContext();
		
		List<String> argList = new ArrayList<String>(Arrays.asList(args));
		
		{
			/*
			 * Currently, the hashdot stubs for launchify contain an error. 
			 * 
			 * It will be fixed in 2.0, but right now, we only need to shift arguments
			 *
			 * Argument of index 0 is always the program name... standard UNIX convention; I don't know why should
			 * hashdot ever change this behavior
			 *      -- Paweł Romanowski, 07-26-2012
			 */
			if (!argList.isEmpty()) {
				argList.remove(0);
			}
		}
		
		CmdLineParser parser = new CmdLineParser(ctx);
		
		if (argList.isEmpty()) {
			showUsage(parser);
			
			return;
		}
		
		try {
			parser.parseArgument(argList);
		} catch (CmdLineException e) {
			System.err.println(e);
			
			showUsage(parser);
			
			return;
		}

		// We start by defining the set of categories in which the DS algorithm
		// will operate. We do this first, so that we can initialize properly
		// the confusion matrixes of the workers, the probability vectors for
		// the objects etc. While it is possible to modify these later on, when we
		// see new categories, it is a PITA and leads to many bugs, especially
		// in an environment where there is persistence of the objects.
		// Plus, it makes it easier to implement the algorithm in a streaming mode.
		
		Engine engine = new Engine(ctx);
		
		engine.execute();
	}

	private static void showUsage(CmdLineParser parser) {
		System.err.println("Usage: \n");
		parser.printUsage(System.err);
		System.err.println();
	}
}

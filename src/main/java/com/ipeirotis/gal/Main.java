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
package com.ipeirotis.gal;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.ipeirotis.gal.engine.Engine;
import com.ipeirotis.gal.engine.EngineContext;

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
		
		CmdLineParser parser = new CmdLineParser(ctx);
		
		try {
			parser.parseArgument(args);
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

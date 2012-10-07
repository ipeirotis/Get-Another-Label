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

		Engine engine = new Engine(ctx);
		
		engine.execute();
	}

	private static void showUsage(CmdLineParser parser) {
		System.err.println("Usage: \n");
		parser.printUsage(System.err);
		System.err.println();
	}
}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.ipeirotis.gal.engine.EngineContext;

public class CLIParserTest {
	private EngineContext ctx;

	private CmdLineParser parser;

	@Before
	public void before() {
		ctx = new EngineContext();

		parser = new CmdLineParser(ctx);
	}

	@Test
	public void testHappyPath() throws Exception {
		parser.parseArgument("--categories data\\categories.txt --input data\\unlabeled.txt --gold data\\labeled.txt --cost data\\costs.txt --eval data\\evaluationdata.txt --iterations 10"
				.split("\\s+"));

		assertEquals("data\\categories.txt", ctx.getCategoriesFile());
		assertEquals("data\\unlabeled.txt", ctx.getInputFile());
		assertEquals("data\\labeled.txt", ctx.getGoldFile());
		assertEquals("data\\costs.txt", ctx.getCostFile());
		assertEquals("data\\evaluationdata.txt", ctx.getEvaluationFile());
		assertEquals(10, ctx.getNumIterations());
	}

	@Test(expected = CmdLineException.class)
	public void testMissingArgument() throws Exception {
		parser.parseArgument("--categories data\\unlabeled.txt --cost data\\costs.txt --eval data\\evaluationdata.txt --iterations 10"
				.split("\\s+"));
	}

	@Test(expected = CmdLineException.class)
	public void testHelpUsage() throws Exception {
		parser.parseArgument(Collections.<String> emptyList());

		fail();
	}
}

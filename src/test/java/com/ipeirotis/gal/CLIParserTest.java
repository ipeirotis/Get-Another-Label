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
		parser.parseArgument("data\\categories.txt data\\unlabeled.txt data\\labeled.txt  data\\costs.txt data\\evaluationdata.txt -iterations 10".split("\\s+"));
		
		assertEquals("data\\categories.txt", ctx.getCategoriesFile());
		assertEquals("data\\unlabeled.txt", ctx.getInputFile());
		assertEquals("data\\labeled.txt", ctx.getCorrectFile());
		assertEquals("data\\costs.txt", ctx.getCostFile());
		assertEquals("data\\evaluationdata.txt", ctx.getEvaluationFile());
		assertEquals(10, ctx.getNumIterations());
	}

	@Test(expected=CmdLineException.class)
	public void testMissingArgument() throws Exception {
		parser.parseArgument("data\\unlabeled.txt data\\labeled.txt  data\\costs.txt data\\evaluationdata.txt -iterations 10".split("\\s+"));
	}
	
	@Test
	public void testHelpUsage() {
		try {
			parser.parseArgument(Collections.<String>emptyList());
			
			fail();
		} catch (CmdLineException exc) {
			parser.printUsage(System.err);
		}
	}
}

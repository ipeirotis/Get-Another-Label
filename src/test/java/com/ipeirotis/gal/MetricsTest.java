package com.ipeirotis.gal;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineParser;

import com.ipeirotis.gal.engine.Engine;
import com.ipeirotis.gal.engine.EngineContext;

public class MetricsTest {
	private EngineContext ctx;

	private CmdLineParser parser;

	@Before
	public void before() {
		ctx = new EngineContext();

		parser = new CmdLineParser(ctx);
	}

	@Test
	public void testHappyPath() throws Exception {
		parser.parseArgument("--input data/AdultContent/labels.txt --categories data/AdultContent/categories.txt --dry-run"
				.split("\\s+"));

		assertEquals(ctx.getInputFile(), "data/AdultContent/labels.txt");
		assertEquals(ctx.getCategoriesFile(), "data/AdultContent/categories.txt");
		
		Engine engine = new Engine(ctx);
		
		engine.execute();
	}
	
	@Test
	public void testMoreComplexPath() throws Exception {
		parser.parseArgument("--input data/BarzanMozafari/labels.txt --categories data/BarzanMozafari/categories.txt --eval data/BarzanMozafari/evaluation.txt --dry-run"
				.split("\\s+"));
		
		Engine engine = new Engine(ctx);
		
		engine.execute();
	}
}

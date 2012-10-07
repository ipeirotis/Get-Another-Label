package com.ipeirotis.gal;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineParser;

import com.ipeirotis.gal.engine.Engine;
import com.ipeirotis.gal.engine.EngineContext;

public class EngineTest {
	private EngineContext ctx;

	private CmdLineParser parser;

	@Before
	public void before() {
		ctx = new EngineContext();

		parser = new CmdLineParser(ctx);
	}

	@Test
	public void testHappyPath() throws Exception {
		parser.parseArgument("--input data/AdultContent/test-unlabeled.txt --categories data/AdultContent/test-categories.txt"
				.split("\\s+"));

		assertEquals(ctx.getInputFile(), "data/AdultContent/test-unlabeled.txt");
		assertEquals(ctx.getCategoriesFile(), "data/AdultContent/test-categories.txt");
		
		Engine engine = new Engine(ctx);
		
		engine.execute();
	}
	
	@Test
	public void testMoreComplexPath() throws Exception {
		parser.parseArgument("--input data/BarzanMozafari/input.txt --categories data/BarzanMozafari/categories-prior.txt --eval data/BarzanMozafari/evaluation.txt"
				.split("\\s+"));
		
		Engine engine = new Engine(ctx);
		
		engine.execute();
	}
	
	@Test
	public void testFullCase() throws Exception {
		parser.parseArgument("--input data/BarzanMozafari/input.txt --categories data/BarzanMozafari/categories-prior.txt --gold data/BarzanMozafari/gold-30items.txt --cost data/BarzanMozafari/costs.txt --eval data/BarzanMozafari/evaluation.txt"
				.split("\\s+"));
		
		Engine engine = new Engine(ctx);
		
		engine.execute();
	}
	
	@Test
	public void test5KDataPriorGold() throws Exception {
		parser.parseArgument("--iterations 100 --verbose --input data/BarzanMozafari/input.txt --categories data/BarzanMozafari/categories-prior.txt --gold data/BarzanMozafari/gold-30items.txt --eval data/BarzanMozafari/evaluation.txt"
				.split("\\s+"));
		
		Engine engine = new Engine(ctx);
		
		engine.execute();
	}
	
	@Test
	public void test5KDataNoPriorGold() throws Exception {
		parser.parseArgument("--input data/BarzanMozafari/input.txt --categories data/BarzanMozafari/categories.txt --gold data/BarzanMozafari/gold-30items.txt --eval data/BarzanMozafari/evaluation.txt"
				.split("\\s+"));
		
		Engine engine = new Engine(ctx);
		
		engine.execute();
	}
	
	@Test
	public void test5KDataPriorNoGold() throws Exception {
		parser.parseArgument("--input data/BarzanMozafari/input.txt --categories data/BarzanMozafari/categories-prior.txt --eval data/BarzanMozafari/evaluation.txt"
				.split("\\s+"));
		
		Engine engine = new Engine(ctx);
		
		engine.execute();
	}
	
	@Test
	public void test5KDataNoPriorNoGold() throws Exception {
		parser.parseArgument("--input data/BarzanMozafari/input.txt --categories data/BarzanMozafari/categories.txt --eval data/BarzanMozafari/evaluation.txt"
				.split("\\s+"));
		
		Engine engine = new Engine(ctx);
		
		engine.execute();
		
		//engine.getDs().g
	}
	
}

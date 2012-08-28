package com.ipeirotis.gal;

import org.junit.Before;
import org.junit.Test;

import com.ipeirotis.gal.engine.Engine;
import com.ipeirotis.gal.engine.EngineContext;

public class EngineTest {
	private EngineContext ctx;

	@Before
	public void before() {
		ctx = new EngineContext();
	}

	@Test
	public void testHappyPath() throws Exception {
		ctx.setInputFile("data/test-unlabeled.txt");
		ctx.setCategoriesFile("data/test-categories.txt");
		
		Engine engine = new Engine(ctx);
		
		engine.execute();
	}
}

package com.fyp.autologistics;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.simulation.scheduler-enabled=false")
class AutologisticsApplicationTests {

	@Test
	void contextLoads() {
	}

}

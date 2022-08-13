package org.ipsedixit.javatestcontainerssmtpexample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CandyTest {
	private ConfEmail confEmail;
	private Dashboard dashboard;
	private CandyBox candyBox;

	@BeforeEach
	void init() {
		confEmail = new ConfEmail(false, "fake-mail-server", 25, "me@example.com", List.of("you@example.com"));
		dashboard = new Dashboard(confEmail);
		candyBox = new CandyBox(dashboard, 3);
	}

	@Test
	void testCandyBoxNoEat() {
		assertTrue(candyBox.hasMoreCandy());
	}

	@Test
	void testCandyBoxEatOne() {
		candyBox.eatOne();
		assertTrue(candyBox.hasMoreCandy());
		assertEquals(2, candyBox.remainingCandy());
		assertEquals(Collections.EMPTY_LIST, dashboard.getAllAlarms());
	}

	@Test
	void testCandyBoxEatAllCandy() {
		candyBox.eatOne();
		candyBox.eatOne();
		candyBox.eatOne();
		assertFalse(candyBox.hasMoreCandy());
		assertEquals(0, candyBox.remainingCandy());
		assertEquals(Collections.EMPTY_LIST, dashboard.getAllAlarms());
	}

	@Test
	void testCandyBoxEatActivateAlarmOneTime() {
		candyBox.eatOne();
		candyBox.eatOne();
		candyBox.eatOne();
		candyBox.eatOne();
		assertFalse(candyBox.hasMoreCandy());
		assertEquals(0, candyBox.remainingCandy());
		assertEquals(List.of(Dashboard.ALARM_CANDY), dashboard.getAllAlarms());
	}

	@Test
	void testCandyBoxEatActivateAlarmTwoTimes() {
		candyBox.eatOne();
		candyBox.eatOne();
		candyBox.eatOne();
		candyBox.eatOne();
		candyBox.eatOne();
		assertFalse(candyBox.hasMoreCandy());
		assertEquals(0, candyBox.remainingCandy());
		assertEquals(List.of(Dashboard.ALARM_CANDY, Dashboard.ALARM_CANDY), dashboard.getAllAlarms());
	}

}

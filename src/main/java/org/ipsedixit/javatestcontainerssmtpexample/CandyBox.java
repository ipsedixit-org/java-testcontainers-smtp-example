package org.ipsedixit.javatestcontainerssmtpexample;

/***
 * CandyBox is charge of control and expose a, no surprise, candy box. So expose
 * API to eat one (someone get one and eat it), and also notify dashboard in
 * case that there is no more candy.
 */
public class CandyBox {

	private final Dashboard dashboard;
	private int numberCandy;

	public CandyBox(Dashboard dashboard, int numberCandy) {
		this.dashboard = dashboard;
		this.numberCandy = numberCandy;
	}

	public boolean hasMoreCandy() {
		return this.numberCandy > 0;
	}

	public void eatOne() {
		if (this.hasMoreCandy()) {
			this.numberCandy -= 1;
		} else {
			this.dashboard.alarmNoCandy();
		}
	}

	public int remainingCandy() {
		return this.numberCandy;
	}
}

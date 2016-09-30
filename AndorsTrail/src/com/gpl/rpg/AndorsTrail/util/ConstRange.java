package com.gpl.rpg.AndorsTrail.util;


public final class ConstRange {
	public final int max;
	public final int current;

	public ConstRange(Range r) {
		this.max = r.max;
		this.current = r.current;
	}
	public ConstRange(ConstRange r) {
		this.max = r.max;
		this.current = r.current;
	}
	public ConstRange(int max, int current) {
		this.max = max;
		this.current = current;
	}

	public String toString() { return current + "/" + max; }
	public String toMinMaxString() {
		if (isMax()) return Integer.toString(max);
		else return current + "-" + max;
	}
	public String toMinMaxAbsString() {
		if (isMax()) return Integer.toString(Math.abs(max));
		else return Math.abs(current) + "-" + Math.abs(max);
	}
	public boolean isMax() { return max == current;	}
	public int average() {
		return (max + current) / 2;
	}
	public float averagef() {
		return ((float) max + current) / 2f;
	}
	public String toPercentString() {
		return Integer.toString((int) Math.ceil((float)current * 100 / max)) + '%';
	}
}

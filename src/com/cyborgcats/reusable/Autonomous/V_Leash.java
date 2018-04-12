package com.cyborgcats.reusable.Autonomous;

public class V_Leash {
	private int currentSegment = 0;
	private boolean doneGeneratingTargets = false;
	
	//an array of segments represents a full path
	private final Path[] path;
	private final double desiredLength;
	private final double growthRate;
	/**
	 * Leash will try to keep the distance between desired and actual coordinates as close to desiredLength as possible.
	 * growthRate tells it how quickly to increment the time-like parameter that helps generate new X and Y values.
	**/
	public V_Leash(final Path[] path, final double desiredLength, final double growthRate) {
		this.path = path;
		this.desiredLength = desiredLength;
		this.growthRate = growthRate;
	}
	
	public void init() {
		currentSegment = 0;
		doneGeneratingTargets = false;
	}
	
	private void increment(final double amount) {
		if (!path[currentSegment].increment(amount)) {//if current segment has been used up
			if (currentSegment + 1 < path.length) currentSegment++;//if we are able to move on, do so
			else doneGeneratingTargets = true;//otherwise, say we're done
		}
	}
	
	private double getActualLength(final double currentX, final double currentY) {
		final double differenceX = path[currentSegment].getX() - currentX;
		final double differenceY = path[currentSegment].getY() - currentY;
		return Math.sqrt(differenceX*differenceX + differenceY*differenceY);
	}
	
	public void maintainLength(final double currentX, final double currentY) {
		while (!doneGeneratingTargets && getActualLength(currentX, currentY) < desiredLength) {
			increment(growthRate);
		}
	}
	
	public double getX() {return path[currentSegment].getX();}
	public double getY() {return path[currentSegment].getY();}
	public double getIndependentVariable() {return path[currentSegment].getIndependentVariable();}
	public boolean doneGeneratingTargets() {return doneGeneratingTargets;}
}
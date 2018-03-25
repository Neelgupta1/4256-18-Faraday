package com.cyborgcats.reusable;//COMPLETE 2016

public class V_Compass {
	private double tareAngle = 0;
	private final double protectedZoneStart;//Angles increase as the numbers on a clock increase. This value should be the first protected angle encountered by a minute hand which starts at 12:00.
	private final double protectedZoneSize;//This value should be the number of degrees the minute hand must travel before reaching the end of the protected section.
	
	public V_Compass(final double protectedZoneStart, final double protectedZoneSize) {
		this.protectedZoneStart = validate(protectedZoneStart);
		this.protectedZoneSize = Math.abs(protectedZoneSize)%360;
	}
	
	
	/**
	 * This function tares the compass at the specified angle, relative to the current 0. It accepts both -'s and +'s.
	**/
	public void setTareAngle(final double tareAngle) {
		this.tareAngle = tareAngle;
	}
	
	
	/**
	 * This function returns the current tare angle, relative to the initialized 0.
	**/
	public double getTareAngle() {
		return tareAngle;
	}
	
	
	/**
	 * This function modifies the input to create a value between 0 and 359.999...
	**/
	public static double validate(final double angle) {
		final double temp = 360 - (Math.abs(angle)%360);
		if (angle < 0) return (temp < 360) ? temp : 0;
		else return (angle%360 < 360) ? angle%360 : 0;
	}
	
	
	/**
	 * This function finds the shortest path from the start angle to the end angle and returns the size of that path in degrees.
	 * Positive means clockwise and negative means counter-clockwise.
	**/
	public static double path(final double start, final double end) {
//		startAngle = validateAngle(startAngle);
//		endAngle = validateAngle(endAngle);
//		double pathVector = endAngle - startAngle;
//		if (Math.abs(pathVector) > 180) {
//			pathVector = Math.abs(pathVector) - 360;
//		}if (endAngle - startAngle < -180) {
//			pathVector = -pathVector;
//		}return pathVector;
		return Math.IEEEremainder(end - start, 360.0);
	}
	
	
	/**
	 * This function returns a valid and legal version of the input.
	**/
	public double legalize(double angle) {
		if (protectedZoneSize != 0) {
			final double protectedZoneEnd = protectedZoneStart + protectedZoneSize;
				  double fromStartingEdge = path(protectedZoneStart, angle);
			final double toEndingEdge = path(angle, protectedZoneEnd);
			
			if (fromStartingEdge < 0) {
				fromStartingEdge += 360;
				fromStartingEdge *= Math.signum(toEndingEdge);
			}
			if ((fromStartingEdge > 0) && (fromStartingEdge < protectedZoneSize)) angle = fromStartingEdge <= Math.abs(toEndingEdge) ? protectedZoneStart : protectedZoneEnd;
		}
		return validate(angle);
	}
	
	
	/**
	 * This function returns the path to the border that is nearest to the specified angle.
	**/
	private double borderPath(final double start) {
		final double toStartingEdge = path(start, protectedZoneStart);
		final double toEndingEdge = path(start, protectedZoneStart + protectedZoneSize);
		return Math.abs(toStartingEdge) <= Math.abs(toEndingEdge) ? toStartingEdge : toEndingEdge;
	}
	
	
	/**
	 * This function finds the shortest legal path from the start angle to the end angle and returns the size of that path in degrees.
	 * Positive means clockwise and negative means counter-clockwise.
	**/
	public double legalPath(final double start, final double end) {
		final double start_legal = legalize(start);
		final double path_escape = validate(start) == start_legal ? 0.0 : borderPath(start);
		double path_main = path(start_legal, legalize(end));
		
		if (protectedZoneSize != 0) {
			double borderPath = borderPath(start_legal);
			
			//OPTION A -- condensed
			double comparator = borderPath == 0 ? 0 : 1;
			if (borderPath == 0) borderPath = path(start_legal, protectedZoneStart + protectedZoneSize/2.0);
			if (path_main/borderPath > comparator) path_main -= Math.copySign(360, path_main);
			
			//OPTION B -- readable
//			if (borderPath != 0) {
//				//equivalent to:  if (Math.abs(borderPath) < Math.abs(path_main) && Math.signum(path_main) == Math.signum(borderPath))
//				if (path_main/borderPath > 1) path_main -= Math.copySign(360, path_main);
//			}else {
//				//equivalent to:  if (Math.signum(path_main) == Math.signum(path(start_legal, protectedZoneStart + protectedZoneSize/2)))
//				if (path_main/path(start_legal, protectedZoneStart + protectedZoneSize/2) > 0) path_main -= Math.copySign(360, path_main);
//			}
		}
		return path_main + path_escape;
	}
	
	
	/**
	 * This function returns the standard deviation of an array of angles in degrees.
	 * It's smart enough to handle 360-0 boundary condition.
	**/
	public static double stdd(final double[] angles) {
		double sin = 0.0,	cos = 0.0;
		for (double angle : angles) {sin += Math.sin(Math.toRadians(angle));cos += Math.cos(Math.toRadians(angle));}
		
		sin /= angles.length;
		cos /= angles.length;
		final double stdd = Math.sqrt(-Math.log(sin*sin + cos*cos));
		
		return Math.toDegrees(stdd);
	}
	
	
	/**
	 * This function finds the angle between the Y axis and any Cartesian coordinate.
	**/
	public static double convertToAngle(final double x, final double y) {return Math.toDegrees(Math.atan2(x, -y));}
}
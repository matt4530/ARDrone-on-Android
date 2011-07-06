package com.profusiongames;

public class MathUtil {
	public static float wrap(float value, float lower, float upper) { 
		  if(upper <= lower) 
		    throw new ArithmeticException("Rotary bounds are of negative or zero size"); 
		 
		  float distance = upper - lower; 
		  float times = (float)Math.floor((value - lower) / distance); 
		 
		  return value - (times * distance); 
		} 
		 
		public static float getShortestAngle(float angle1, float angle2) { 
		  float difference = angle2 - angle1; 
		  return wrap(difference, -180.0f, +180.0f); 
		}
		
		public static float abs(float a) {
			return a > 0 ? a : -a;
		}
		
		public static float trunk(float a) {
			return (float)((int)a);
		}
}

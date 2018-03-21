package com.ustc.wsn.mydataapp.ellipsoidFit;

import java.io.IOException;

/**
 * Run the EllipsoidFit sample.
 * 
 * Apache License Version 2.0, January 2004.
 * 
 * @author Kaleb
 * @version 1.0
 */
public class Main
{
	public static void main(String[] args) throws IOException
	{
		long a = System.currentTimeMillis();
		//new EllipsoidFit();
		long b = System.currentTimeMillis();
		System.out.println(b-a);
	}
}

// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   MD5Util.java

package com.ustc.wsn.mydataapp.utils;

import java.security.MessageDigest;

public class MD5Util
{

	private static final String SALT = "gxexo";

	public MD5Util()
	{
	}

	public static String encode(String password)
	{
		password = (new StringBuilder()).append(password).append("gxexo").toString();
		return processEncode(password);
	}

	public static String processEncode(String password)
	{
		MessageDigest md5 = null;
		try
		{
			md5 = MessageDigest.getInstance("MD5");
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		char charArray[] = password.toCharArray();
		byte byteArray[] = new byte[charArray.length];
		for (int i = 0; i < charArray.length; i++)
			byteArray[i] = (byte)charArray[i];

		byte md5Bytes[] = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++)
		{
			int val = md5Bytes[i] & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}

		return hexValue.toString();
	}
}

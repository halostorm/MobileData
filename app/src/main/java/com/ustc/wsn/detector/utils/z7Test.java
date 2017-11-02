package com.ustc.wsn.detector.utils;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class z7Test {
	public static void z7(String inputpath, String outputpath) throws IOException {
		// inputpath =
		// "E:/Users/halo_storm/workspace/7ZTest/7zTest/Fre_2017_7_3_22_16.txt";//
		// ÐèÒªÑ¹ËõµÄÎÄ¼þÂ·¾¶
		// outputpath = "E:/Users/halo_storm/workspace/7ZTest/7zTest/test.7z";

		File output = new File(outputpath);
		SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(output);
		File file = new File(inputpath);
		SevenZArchiveEntry entry = sevenZOutputFile.createArchiveEntry(file, file.getName());
		sevenZOutputFile.putArchiveEntry(entry);
		FileInputStream in = new FileInputStream(file);
		byte[] b = new byte[1024];
		int count = 0;
		count = in.read(b);
		while (count > 0) {
			// System.out.println("count:"+count);
			sevenZOutputFile.write(b, 0, count);
			count = in.read(b);
		}
		in.close();
		sevenZOutputFile.closeArchiveEntry();
		sevenZOutputFile.close();



	}
}

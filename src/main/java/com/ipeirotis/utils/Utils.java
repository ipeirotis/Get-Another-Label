/*******************************************************************************
 * Copyright 2012 Panos Ipeirotis
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.ipeirotis.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;

public class Utils {

	public static String cleanLine(String line) {

		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c < 128 && (Character.isLetter(c) || Character.isDigit(c))) {
				buffer.append(c);
			} else {
				// buffer.append('');
			}
		}
		return buffer.toString().toLowerCase();
	}

	public static String getFile(String FileName) {

		StringBuffer buffer = new StringBuffer();

		try {
			BufferedReader dataInput = new BufferedReader(new FileReader(new File(FileName)));
			String line;

			while ((line = dataInput.readLine()) != null) {
				// buffer.append(cleanLine(line.toLowerCase()));
				buffer.append(line);
				buffer.append('\n');
			}
			dataInput.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return buffer.toString();
	}

	public static void writeFile(String in, String filename) {

		try {
			File outfile = new File(filename);

			if (!(new File(outfile.getParent()).exists())) {
				(new File(outfile.getParent())).mkdirs();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
			bw.write(in);
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Double round(double d, int decimalPlace) {

		// see the Javadoc about why we use a String in the constructor
		// http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
		BigDecimal bd = new BigDecimal(Double.toString(d));
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

	public static Double entropy(double[] p) {

		double h = 0;
		for (int i = 0; i < p.length; i++) {
			h += (p[i] > 0) ? p[i] * Math.log(p[i]) : 0.0;
		}
		return -h;
	}

}

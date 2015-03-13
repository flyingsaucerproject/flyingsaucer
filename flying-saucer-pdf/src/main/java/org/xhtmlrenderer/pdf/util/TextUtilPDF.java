package org.xhtmlrenderer.pdf.util;

import java.text.Bidi;

public class TextUtilPDF {


	/**
	 * This method transforms a left to right string into a Bidirectional string. 
	 *
	 * @param String text  
	 * @return String
	 */
	public static String bidifyString(String s){
		System.out.println("Entered bidifyString method");
		Bidi bidi = new Bidi(s, Bidi.DIRECTION_LEFT_TO_RIGHT);

		int count = bidi.getRunCount();
		byte[] levels = new byte[count];
		Integer[] runs = new Integer[count];

		for (int i = 0; i < count; i++)
		{
			levels[i] = (byte)bidi.getRunLevel(i);
			runs[i] = i;
		}

		Bidi.reorderVisually(levels, 0, runs, 0, count);

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < count; i++)
		{
			int index = runs[i];
			int start = bidi.getRunStart(index);
			int end = bidi.getRunLimit(index);
			int level = levels[index];

			if ((level & 1) != 0)
			{
				for (; --end >= start;)
				{
					result.append(s.charAt(end));
				}
			}
			else
			{
				result.append(s, start, end);
			}
		}
		return result.toString();
	}

}

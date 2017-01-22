/*
 * Copyright (C) 2017 Lukas Zaruba, lukas.zaruba@gmail.com
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.xhtmlrenderer.fop.nbsp;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes text and replaces spaces by non-break spaces (\u00A0) 
 * on places designated by the language definition file.
 * 
 * Language definition files should be located on classpath in the directory
 * "non-break-spaces/${langKey}.nbsp". If lang key contains '_', in first run
 * we search for whole lang key and if not found for part of the lang key before '_'.
 * Encoding of the nbsp file must be utf8.
 * 
 * Language definition file consist of line per each rule. Line contains regexp pattern with
 * three groups. Second group will be reaplaced by \u00A0. First and second will be copied and are used
 * to match the selection properly. 
 * Ex.: "([\\s]+and)( )([^\\s]+)" will replace "this and something else" with "this and\u00A0something else" 
 * so and will not be left hanging at the end of the line.
 * 
 * Rules are applied to the content in the order as they appear in the file and result of one rule run is used
 * as input for the next run.
 * 
 * Lines starting with '#' and empty lines are skipped and can be used as comments.
 * 
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public class NonBreakPointsEnhancer {
	
	private NonBreakPointsLoader loader;
	
	public NonBreakPointsEnhancer() {
		this(new NonBreakPointsLoaderImpl());
	}
	
	/**
	 * For test only
	 */
	/*package*/ NonBreakPointsEnhancer(NonBreakPointsLoader loader) {
		this.loader = loader;
	}
	
	public String enhance(String input, String lang) {
		if (input == null) return null;
		if (input.isEmpty()) return "";
		if (lang == null || lang.isEmpty()) return input;
		List<String> rules = loader.loadNBSP(lang);
		if (rules == null) return input;
		for (String r : rules) {
			Matcher m = Pattern.compile(r).matcher(input);
			if (m.groupCount() != 3) {
				throw new IllegalArgumentException("Expression must contain exactly 3 groups! " + r);
			}
			if (m.find()) {
				input = m.replaceAll("$1\u00A0$3");
			}
		}
		return input;
	}

}

/*
 * Copyright (C) 2017 MEDIA SOLUTIONS
 * All Rights Reserved.  No use, copying or distribution of this
 * work may be made except in accordance with a valid license
 * agreement from MEDIA SOLUTIONS. This notice must be
 * included on all copies, modifications and derivatives of this
 * work.
 *
 * MEDIA SOLUTIONS MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * MEDIA SOLUTIONS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */
package org.xhtmlrenderer.layout.breaker;

/**
 * @author Lukas Zaruba, lukas.zaruba@media-sol.com, MEDIA SOLUTIONS
 */
public class BreakAnywhereLineBreakStrategy implements BreakPointsProvider {

	private String currentString;
	int position = 0;

	public BreakAnywhereLineBreakStrategy(String currentString) {
		this.currentString = currentString;
	}

	@Override
	public BreakPoint next() {
		if (position + 1 > currentString.length()) return BreakPoint.getDonePoint();
		return new BreakPoint(position++);
	}

}

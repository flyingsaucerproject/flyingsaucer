/*
 * FirstLetterStyle.java
 * Copyright (c) 2004 Torbjörn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.xhtmlrenderer.layout.content;

import org.xhtmlrenderer.css.newmatch.CascadedStyle;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2004-dec-06
 * Time: 03:21:52
 * To change this template use File | Settings | File Templates.
 */
public class FirstLetterStyle {
    private CascadedStyle _style;

    public FirstLetterStyle(CascadedStyle style) {
        _style = style;
    }

    public CascadedStyle getStyle() {
        return _style;
    }
}

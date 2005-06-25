/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Who?
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
 * }}}
 */
package org.xhtmlrenderer.resource;

import org.xml.sax.InputSource;

import java.awt.*;

/**
 * @author Administrator
 */
public class ImageResource extends AbstractResource {
    private Image _img;

    /**
     * Creates a new instance of ImageResource
     */
    /*public ImageResource(InputSource source) {
        super(source);
    }*/

    //HACK: at least for now, till we know what we want to do here
    public ImageResource(Image img) {
        super((InputSource) null);
        _img = img;
    }

    public Image getImage() {
        return _img;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2005/06/25 17:23:34  tobega
 * first refactoring of UAC: ImageResource
 *
 * Revision 1.1  2005/02/03 20:39:35  pdoubleya
 * Added to CVS.
 *
 *
 */
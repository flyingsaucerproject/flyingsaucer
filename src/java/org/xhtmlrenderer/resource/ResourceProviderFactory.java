/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Patrick Wright
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

/**
 * Creates ResourceProviders; static methods only.
 *
 * @author Patrick Wright
 */
public class ResourceProviderFactory {
    private static final ResourceProvider DEFAULT_RESOURCE_PROVIDER;
    
    
    static {
        DEFAULT_RESOURCE_PROVIDER = new DefaultResourceProvider();
    }
    
    /** Creates a new instance of ResourceProviderFactory */
    private ResourceProviderFactory() {
    }
    
    public static final ResourceProvider newDefaultResourceProvider() {
        return DEFAULT_RESOURCE_PROVIDER;        
    }
    
}

 /*
  * $Id$
  *
  * $Log$
  * Revision 1.1  2005/02/03 20:39:34  pdoubleya
  * Added to CVS.
  *
  *
  */
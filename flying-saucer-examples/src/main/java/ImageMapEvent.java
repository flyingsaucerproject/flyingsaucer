/*
 * {{{ header & license
 * Copyright (c) 2008 elbart0 at free.fr (submitted via email)
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
 * }}}
 */
import java.awt.event.MouseAdapter;

/**
 * Used in notification of mouse motions related to an image map
 */
public class ImageMapEvent {
    private final MouseAdapter mouseAdapter;
    private final String value;

    public ImageMapEvent(MouseAdapter mouseAdapter, String value) {
        this.mouseAdapter = mouseAdapter;
        this.value = value;
    }
}

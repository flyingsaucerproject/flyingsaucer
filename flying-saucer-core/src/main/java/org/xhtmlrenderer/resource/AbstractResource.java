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

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * @author Patrick Wright
 */
@ParametersAreNonnullByDefault
public abstract class AbstractResource implements Resource {
    private final InputSource inputSource;
    private final long createTimeStamp;
    private long elapsedLoadTime;

    protected AbstractResource(@Nullable InputSource source) {
        this.inputSource = source;
        this.createTimeStamp = System.currentTimeMillis();
    }

    protected AbstractResource(@Nullable InputStream is) {
        this(is == null ? null : new InputSource(new BufferedInputStream(is)));
    }

    @Nullable
    @CheckReturnValue
    public InputSource getResourceInputSource() {
        return this.inputSource;
    }

    @CheckReturnValue
    public long getResourceLoadTimeStamp() {
        return this.createTimeStamp;
    }

    @CheckReturnValue
    public long getElapsedLoadTime() {
        return elapsedLoadTime;
    }

    void setElapsedLoadTime(long elapsedLoadTime) {
        this.elapsedLoadTime = elapsedLoadTime;
    }
}

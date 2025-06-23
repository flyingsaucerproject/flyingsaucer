/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.pdf;

import com.google.errorprone.annotations.CheckReturnValue;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.jspecify.annotations.NonNull;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.Size;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SvgImage extends ITextFSImage {
    public SvgImage(byte[] image, Size size, String uri) {
        super(image, size, uri);
    }

    @NonNull
    @CheckReturnValue
    @Override
    public FSImage scale(int width, int height) {
        return new SvgImage(image, size.scale(width, height), uri);
    }

    @Override
    public byte[] getImage() {
        Transcoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, 1.0f * getWidth());
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, 1.0f * getHeight());

        try (ByteArrayInputStream in = new ByteArrayInputStream(image)) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                transcoder.transcode(new TranscoderInput(in), new TranscoderOutput(out));
                out.flush();
                return out.toByteArray();
            }
        }
        catch (IOException | TranscoderException e) {
            throw new RuntimeException("Failed to convert SVG to PNG (%s)".formatted(uri), e);
        }
    }
}

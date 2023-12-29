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

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
@ParametersAreNonnullByDefault
public class NonBreakPointsLoaderImpl implements NonBreakPointsLoader {

    @Override
    @Nonnull
    @CheckReturnValue
    public List<String> loadNBSP(@Nullable String lang) {
        if (lang == null || lang.isEmpty()) {
            throw new IllegalArgumentException("Lang must be filled to search for file!");
        }
        List<String> result = loadForKey(lang);
        if (result == null) {
            int index = lang.indexOf('_');
            if (index < 0) return emptyList(); // cannot split key
            result = loadForKey(lang.substring(0, index));
        }
        return result == null ? emptyList() : result;
    }

    @Nullable
    @CheckReturnValue
    private List<String> loadForKey(String lang) {
        String path = "non-break-spaces/" + lang + ".nbsp";
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (is == null) return null;
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(is, UTF_8));
            List<String> result = new ArrayList<>();
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                result.add(line);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Error while loading nbsp file from path " + path, e);
        } finally {
            try {
                is.close();
            } catch (IOException e1) {
                // ignore
            }
        }
    }

}

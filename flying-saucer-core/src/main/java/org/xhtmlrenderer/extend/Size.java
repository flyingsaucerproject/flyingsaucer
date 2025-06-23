package org.xhtmlrenderer.extend;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;

public record Size(int width, int height) {

    @NonNull
    @CheckReturnValue
    public Size scale(int width, int height) {
        if (width > 0 || height > 0) {
            int targetWidth = width;
            int targetHeight = height;

            if (targetWidth == -1) {
                targetWidth = (int) (this.width() * ((double) targetHeight / this.height()));
            }

            if (targetHeight == -1) {
                targetHeight = (int) (this.height() * ((double) targetWidth / this.width()));
            }

            if (this.width() != targetWidth || this.height() != targetHeight) {
                return new Size(targetWidth, targetHeight);
            }
        }
        return this;
    }
}

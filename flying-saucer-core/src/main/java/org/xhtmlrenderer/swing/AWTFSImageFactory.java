package org.xhtmlrenderer.swing;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.swing.AWTFSImage.NullImage;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.xhtmlrenderer.util.ImageUtil.convertToBufferedImage;

public class AWTFSImageFactory {
    private static final FSImage NULL_FS_IMAGE = new NullImage();

    @CheckReturnValue
    public static FSImage createImage(@Nullable Image img) {
        if (img == null) {
            return NULL_FS_IMAGE;
        }
        BufferedImage bufferedImage = convertToBufferedImage(img, BufferedImage.TYPE_INT_ARGB);
        return new AWTFSImage.NewAWTFSImage(bufferedImage);
    }
}

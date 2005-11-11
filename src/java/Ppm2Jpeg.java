import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileFilter;
import java.util.Hashtable;

/**
 * Converts PPM files to JPEG files; can pass in single file name or a directory name, the
 * latter will process all .ppm files in the directory, and output to that directory.
 * This requires JAI to be installed.
 */
public class Ppm2Jpeg {
    public static void main(String[] args) {
        File f = new File(args[0]);
        if ( f.exists()) {
            if ( f.isFile()) {
                ppm2Jpeg(f);
            } else {
                File[] list = f.listFiles(new FileFilter() {
                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".ppm");
                    }
                });
                for (int i = 0; i < list.length; i++) {
                    File file = list[i];
                    ppm2Jpeg(file);
                }
            }
        } else {
            System.err.println("Please enter a file or directory name as arg: " + f);
        }
    }

    private static void ppm2Jpeg(File file) {
        try {
            System.out.print("Converting " + file);
            String fileName = file.getAbsolutePath();
            String outname = "";
            if ( fileName.endsWith("ppm")) {
                outname = fileName.substring(0, fileName.length() - 4) + ".jpg";
            } else {
                outname = fileName + ".jpg";
            }
            RenderedOp img = JAI.create("fileload", fileName);
            ColorModel cm = img.getColorModel();
            WritableRaster imgRaster = img.copyData();
            BufferedImage bi = new BufferedImage(cm, imgRaster, false, new Hashtable());
            ImageIO.write((RenderedImage) bi, "jpg", new File(outname));
            System.out.println("...done, " + outname);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

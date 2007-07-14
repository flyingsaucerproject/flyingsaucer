package utilities;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.*;

import javax.imageio.ImageIO;

import org.xhtmlrenderer.util.FSImageWriter;
import org.xhtmlrenderer.util.ImageUtil;

/**
 */
public class Thumbnails {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Enter directory name");
			return;
		}
		String dirName = args[0];
		File dir = new File(dirName);
		if (!dir.exists()) {
			System.out.println("Directory not found: " + dirName);
			return;
		}
		if (!dir.isDirectory()) {
			System.out.println("Not a directory: " + dirName);
			return;
		}
		File[] pngs = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".png") && file.getName().indexOf("thumb") == - 1;
			}
		});
		FSImageWriter writer = new FSImageWriter("png");
		int width = 141;
		int height = 113;
		for (int i = 0; i < pngs.length; i++) {
			File png = pngs[i];
			try {
				String path = png.getAbsolutePath();
				String tpath = path.substring(0, path.indexOf(".png")) + "-thumb.png"; 
				BufferedImage bi = ImageIO.read(png);
				Image img = bi.getScaledInstance(width, height, Image.SCALE_FAST);
				bi = ImageUtil.convertToBufferedImage(img, BufferedImage.TYPE_4BYTE_ABGR);
				Graphics g = bi.getGraphics();
				writer.write(bi, tpath);
				System.out.println("Wrote: " + tpath);
			} catch (IOException e) {
				System.err.println("Can't read file, skipping: " + png.getName() + ", " + e.getMessage());
				continue;
			}
		}
	}
}

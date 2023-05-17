package xyz.spiralhalo.sherlock.util.img;

import java.awt.*;

import javax.swing.*;

import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.neon.icon.ResizableIcon;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.Main;

public class IconUtil {

	public static Icon autoColor(Icon source) {
		if (Main.currentTheme.foreground != 0 && source instanceof ImageIcon) {
			return new ImageIcon(ImgUtil.create(((ImageIcon) source).getImage()).tint(Main.currentTheme.foreground));
		} else {
			return source;
		}
	}

	public static ImageIcon tint(ImageIcon icon, int rgb) {
		return new ImageIcon(ImgUtil.create(icon.getImage()).tint(rgb));
	}

	public static ImageIcon createAutoColor(String path) {
		try {
			if (Main.currentTheme.foreground == 0) {
				return new ImageIcon(ImgUtil.create(path));
			} else {
				return new ImageIcon(ImgUtil.create(path).tint(Main.currentTheme.foreground));
			}
		} catch (Exception e) {
			Debug.log(e);
			return null;
		}
	}

	public static ResizableIcon createResizeableAutoColor(String path, int initW, int initH) {
		try {
			if (Main.currentTheme.foreground == 0) {
				return ImageWrapperResizableIcon.getIcon(ImgUtil.create(path), new Dimension(initW, initH));
			} else {
				final int color = Main.currentTheme.foreground;
				final Dimension dim = new Dimension(initW, initH);
				return ImageWrapperResizableIcon.getIcon(ImgUtil.create(path).tint(color), dim);
			}
		} catch (Exception e) {
			Debug.log(e);
			return null;
		}
	}
}

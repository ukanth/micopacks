package dev.ukanth.iconmgr;

import android.graphics.Point;

import java.util.Locale;

/**
 * Created by ukanth on 15/8/17.
 */

public class ViewHelper {


    public static Point getWallpaperViewRatio(String viewStyle) {
        switch (viewStyle.toLowerCase(Locale.getDefault())) {
            case "square":
                return new Point(1, 1);
            case "landscape":
                return new Point(16, 9);
            case "portrait":
                return new Point(4, 5);
            default:
                return new Point(1, 1);
        }
    }

    public static Detail.Style getHomeImageViewStyle(String viewStyle) {
        switch (viewStyle.toLowerCase(Locale.getDefault())) {
            case "card_square":
                return new Detail.Style(new Point(1, 1), Detail.Style.Type.CARD_SQUARE);
            case "card_landscape":
                return new Detail.Style(new Point(16, 9), Detail.Style.Type.CARD_LANDSCAPE);
            case "square":
                return new Detail.Style(new Point(1, 1), Detail.Style.Type.SQUARE);
            case "landscape":
                return new Detail.Style(new Point(16, 9), Detail.Style.Type.LANDSCAPE);
            default:
                return new Detail.Style(new Point(16, 9), Detail.Style.Type.CARD_LANDSCAPE);
        }
    }
}

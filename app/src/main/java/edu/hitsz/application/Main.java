package edu.hitsz.application;

/**
 * 保留 legacy 中对屏幕尺寸常量的访问方式，实际数值由 Android SurfaceView 在运行时写入。
 */
public final class Main {

    public static int WINDOW_WIDTH = 1080;
    public static int WINDOW_HEIGHT = 1920;

    private Main() {
    }
}

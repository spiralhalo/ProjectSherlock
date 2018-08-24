package xyz.spiralhalo.sherlock;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;

public class EnumerateWindows {
    private static final int MAX_TITLE_LENGTH = 1024;

    public static String getActiveWindowTitle() {
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        User32DLL.GetWindowTextW(User32DLL.GetForegroundWindow(), buffer, MAX_TITLE_LENGTH);
        return Native.toString(buffer);
    }

    static class User32DLL {
        static { Native.register("user32"); }
        public static native HWND GetForegroundWindow();
        public static native int GetWindowTextW(HWND hWnd, char[] lpString, int nMaxCount);
    }
}
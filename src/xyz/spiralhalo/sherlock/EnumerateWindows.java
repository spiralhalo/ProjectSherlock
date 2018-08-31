package xyz.spiralhalo.sherlock;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.UINT;

public class EnumerateWindows {
    private static final int MAX_TITLE_LENGTH = 1024;
//    private static final long GW_HWNDFIRST = 0L;
//    private static final long GW_HWNDLAST = 1L;
    private static final UINT GW_OWNER = new UINT(4L);

    public static String getActiveWindowTitle() {
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        User32DLL.GetWindowTextW(User32DLL.GetForegroundWindow(), buffer, MAX_TITLE_LENGTH);
        return Native.toString(buffer);
    }

    public static String getRootWindowTitle() {
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        HWND foregroundWindow = User32DLL.GetForegroundWindow();
        User32DLL.GetWindowTextW(User32DLL.GetWindow(foregroundWindow, GW_OWNER), buffer, MAX_TITLE_LENGTH);
        return Native.toString(buffer);
    }

//    public static void debug(){
//        char[] buffer0 = new char[MAX_TITLE_LENGTH * 2];
//        char[] buffer1 = new char[MAX_TITLE_LENGTH * 2];
//        char[] buffer2 = new char[MAX_TITLE_LENGTH * 2];
//        char[] buffer3 = new char[MAX_TITLE_LENGTH * 2];
//        HWND x = User32DLL.GetForegroundWindow();
//        User32DLL.GetWindowTextW(User32DLL.GetWindow(x, new UINT(GW_HWNDFIRST)), buffer0, MAX_TITLE_LENGTH);
//        User32DLL.GetWindowTextW(User32DLL.GetWindow(x, new UINT(GW_HWNDLAST)), buffer1, MAX_TITLE_LENGTH);
//        User32DLL.GetWindowTextW(User32DLL.GetWindow(x, new UINT(GW_OWNER)), buffer2, MAX_TITLE_LENGTH);
//        User32DLL.GetWindowTextW(User32DLL.GetTopWindow(x), buffer3, MAX_TITLE_LENGTH);
//        Debug.log("[TopWindow] "+Native.toString(buffer3));
//        Debug.log("[GW_HWNDFIRST] "+Native.toString(buffer0));
//        Debug.log("[GW_HWNDLAST] "+Native.toString(buffer1));
//        Debug.log("[GW_OWNER] "+Native.toString(buffer2));
//    }

    static class User32DLL {
        static { Native.register("user32"); }
        public static native HWND GetForegroundWindow();
//        public static native HWND GetTopWindow(HWND hWnd);
        public static native HWND GetWindow (HWND hWnd, UINT uCmd);
        public static native int GetWindowTextW(HWND hWnd, char[] lpString, int nMaxCount);
    }
}
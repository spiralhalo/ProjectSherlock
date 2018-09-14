package xyz.spiralhalo.sherlock;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.sun.jna.ptr.IntByReference;

import java.util.ArrayList;

public class EnumerateWindows {
    private static final int MAX_TITLE_LENGTH = 1024;
    private static final DWORD GW_OWNER = new DWORD(4L);
    private static final User32 user32 = User32.INSTANCE;
    private static final Kernel32 kernel32 = Kernel32.INSTANCE;
    private static final Psapi psapi = Psapi.INSTANCE;

    public static String getActiveWindowTitle() {
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        user32.GetWindowText(user32.GetForegroundWindow(), buffer, MAX_TITLE_LENGTH);
        return Native.toString(buffer);
    }

    public static String getRootWindowTitle() {
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        HWND foregroundWindow = user32.GetForegroundWindow();
        user32.GetWindowText(user32.GetWindow(foregroundWindow, GW_OWNER), buffer, MAX_TITLE_LENGTH);
        return Native.toString(buffer);
    }

    public static String[] getOpenWindowTitles() {
        EnumWindowsProc enumproc = new EnumWindowsProc();
        user32.EnumWindows(enumproc, null);
        return enumproc.getTitles();
    }

    private static class EnumWindowsProc implements WNDENUMPROC{
        private final ArrayList<String> titles = new ArrayList<>();
        @Override
        public boolean callback(HWND hwnd, Pointer pointer) {
            if (user32.IsWindowVisible(hwnd)) {
                char[] buffer = new char[MAX_TITLE_LENGTH * 2];
                user32.GetWindowText(hwnd, buffer, MAX_TITLE_LENGTH);
                char[] buffer2 = new char[MAX_TITLE_LENGTH * 2];
                IntByReference pidPtr = new IntByReference();
                user32.GetWindowThreadProcessId(hwnd, pidPtr);
                HANDLE handle = kernel32.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, pidPtr.getValue());
                psapi.GetModuleFileNameExW(handle, null, buffer2, MAX_TITLE_LENGTH);
                String title = Native.toString(buffer);
                String path = Native.toString(buffer2);
                if(path.length() > 0) {
                    String toPut = String.format("%s, %s", title, path.substring(path.lastIndexOf('\\') + 1));
                    if (title.length() > 0 && !titles.contains(toPut) && !excluded(path)) {
                        titles.add(toPut);
                    }
                }
            }
            return true;
        }
        private static boolean excluded(String path){
            return path.toLowerCase().contains("windows\\explorer.exe");
        }

        public String[] getTitles() {
            return titles.toArray(new String[0]);
        }
    }
}
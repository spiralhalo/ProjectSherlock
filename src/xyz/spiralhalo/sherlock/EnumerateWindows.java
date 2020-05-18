package xyz.spiralhalo.sherlock;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.sun.jna.ptr.IntByReference;
import xyz.spiralhalo.sherlock.util.jna.DwmApi;

import java.awt.*;
import java.util.ArrayList;

import static com.sun.jna.platform.win32.WinError.S_OK;
import static xyz.spiralhalo.sherlock.util.jna.DWMWINDOWATTRIBUTE.DWMWA_CLOAKED;

public class EnumerateWindows {

    public static class WindowInfo {
        public final HWND hwndPointer;
        public final String title;
        public final String exeName;

        public WindowInfo(HWND hwndPointer, String title, String exeName) {
            this.hwndPointer = hwndPointer;
            this.title = title;
            this.exeName = exeName;
        }
    }

    private static final int MAX_TITLE_LENGTH = 1024;
    private static final DWORD GW_OWNER = new DWORD(4L);
    private static final User32 user32 = User32.INSTANCE;
    private static final Kernel32 kernel32 = Kernel32.INSTANCE;
    private static final Psapi psapi = Psapi.INSTANCE;
    private static final DwmApi dwmapi = DwmApi.INSTANCE;

    public static WindowInfo getActiveWindowInfo() {
        HWND foregroundWindow = user32.GetForegroundWindow();
        return new WindowInfo(foregroundWindow, getTitle(foregroundWindow), getExeName(foregroundWindow));
    }

    public static WindowInfo getRootWindowInfo(HWND foregroundWindow) {
        HWND rootWindow = user32.GetWindow(foregroundWindow, GW_OWNER);
        return new WindowInfo(rootWindow, getTitle(foregroundWindow), getExeName(foregroundWindow));
    }

    public static Rectangle getWindowRect(HWND window){
        WinDef.RECT rect = new WinDef.RECT();
        user32.GetWindowRect(window, rect);
        return rect.toRectangle();
    }

    public static String[] getOpenWindowTitles(String[] filterOut, boolean includeProcess) {
        EnumWindowsProc enumproc = new EnumWindowsProc(filterOut, includeProcess);
        user32.EnumWindows(enumproc, null);
        return enumproc.getTitles();
    }

    private static int dwmIsCloaked(HWND hWnd) {
        if(dwmapi == null) return 0;
        IntByReference cloaked = new IntByReference();
        HRESULT hRes = dwmapi.DwmGetWindowAttribute( hWnd, DWMWA_CLOAKED, cloaked, DWORD.SIZE );
        if (!hRes.equals(S_OK))
        {
            cloaked.setValue(0);
        }
        return cloaked.getValue();
    }

    private static String getTitle(HWND hwnd){
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        user32.GetWindowText(hwnd, buffer, MAX_TITLE_LENGTH);
        return Native.toString(buffer);
    }

    private static String getExeName(HWND hwnd){
        IntByReference pidPtr = new IntByReference();
        user32.GetWindowThreadProcessId(hwnd, pidPtr);
        char[] buffer2 = new char[MAX_TITLE_LENGTH * 2];
        HANDLE handle = kernel32.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, pidPtr.getValue());
        psapi.GetModuleFileNameExW(handle, null, buffer2, MAX_TITLE_LENGTH);
        String path = Native.toString(buffer2);
        return path.substring(path.lastIndexOf('\\') + 1);
    }

    private static class EnumWindowsProc implements WNDENUMPROC {
        private final ArrayList<String> titles = new ArrayList<>();
        private final String[] filterOut;
        private final boolean includeProcess;

        public EnumWindowsProc(String[] filterOut, boolean includeProcess) {
            this.filterOut = new String[filterOut.length];
            this.includeProcess = includeProcess;
            for (int i = 0; i < filterOut.length; i++) { this.filterOut[i] = filterOut[i].toLowerCase(); }
        }

        @Override
        public boolean callback(HWND hwnd, Pointer pointer) {
            if (user32.IsWindowVisible(hwnd) && dwmIsCloaked(hwnd)==0) {
                char[] buffer = new char[MAX_TITLE_LENGTH * 2];
                user32.GetWindowText(hwnd, buffer, MAX_TITLE_LENGTH);
                char[] buffer2 = new char[MAX_TITLE_LENGTH * 2];
                IntByReference pidPtr = new IntByReference();
                user32.GetWindowThreadProcessId(hwnd, pidPtr);
                if(pidPtr.getValue()!=kernel32.GetCurrentProcessId()) {
                    HANDLE handle = kernel32.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION, false, pidPtr.getValue());
                    psapi.GetModuleFileNameExW(handle, null, buffer2, MAX_TITLE_LENGTH);
                    String title = Native.toString(buffer);
                    String path = Native.toString(buffer2);
                    if (title.length() > 0 && !excluded(path)) {
                        String toPut;
                        if (includeProcess) {
                            toPut = String.format("%s, %s", title, path.substring(path.lastIndexOf('\\') + 1));
                        } else {
                            toPut = title;
                        }
                        if (!titles.contains(toPut)) {
                            titles.add(toPut);
                        }
                    }
                }
            }
            return true;
        }

        private boolean excluded(String path){
            for (String x:filterOut) {
                if(path.toLowerCase().contains(x)){
                    return true;
                }
            }
            return false;
        }

        public String[] getTitles() {
            return titles.toArray(new String[0]);
        }
    }
}
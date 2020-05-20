package xyz.spiralhalo.sherlock.util.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

public interface DwmApi extends Library {

    DwmApi INSTANCE = (DwmApi)Native.loadLibrary("dwmapi", DwmApi.class);

    WinNT.HRESULT DwmGetWindowAttribute(HWND hwnd, int dwAttribute, IntByReference pvAttribute, int cbAttribute);
}
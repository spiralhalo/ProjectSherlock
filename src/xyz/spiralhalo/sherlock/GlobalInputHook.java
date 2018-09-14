package xyz.spiralhalo.sherlock;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.mouse.GlobalMouseHook;

public class GlobalInputHook {
    public static GlobalKeyboardHook GLOBAL_KEYBOARD_HOOK = new GlobalKeyboardHook(false);
    public static GlobalMouseHook GLOBAL_MOUSE_HOOK = new GlobalMouseHook(false);
}

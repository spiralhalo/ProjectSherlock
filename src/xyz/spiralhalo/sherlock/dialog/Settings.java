//
//    Copyright 2020 spiralhalo <re.nanashi95@gmail.com>
//
//    This file is part of Project Sherlock.
//
//    Project Sherlock is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Project Sherlock is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Project Sherlock.  If not, see <https://www.gnu.org/licenses/>.
//

package xyz.spiralhalo.sherlock.dialog;

import xyz.spiralhalo.sherlock.Main;
import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.bookmark.BookmarkConfig;
import xyz.spiralhalo.sherlock.bookmark.BookmarkConfig.BookmarkInt;
import xyz.spiralhalo.sherlock.bookmark.BookmarkMgr;
import xyz.spiralhalo.sherlock.persist.settings.*;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.HMSMode;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.Theme;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.swing.IntSelection;
import xyz.spiralhalo.sherlock.util.swing.IntSelectorModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static xyz.spiralhalo.sherlock.bookmark.BookmarkConfig.*;
import static xyz.spiralhalo.sherlock.bookmark.BookmarkConfig.BookmarkBool.*;
import static xyz.spiralhalo.sherlock.bookmark.BookmarkConfig.BookmarkInt.AUTO_SUBFOLDER;
import static xyz.spiralhalo.sherlock.persist.settings.AppConfig.*;
import static xyz.spiralhalo.sherlock.persist.settings.AppConfig.AppBool.*;
import static xyz.spiralhalo.sherlock.persist.settings.AppConfig.AppInt.*;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.*;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt.*;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserBool.*;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserStr.BREAK_MESSAGE;

public class Settings extends JDialog {
    //region AUTOVARS
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton buttonApply;
    private JCheckBox checkAAskBeforeQuit;
    private JSlider sliderTarget;
    private JCheckBox day0;
    private JCheckBox day1;
    private JCheckBox day2;
    private JCheckBox day3;
    private JCheckBox day4;
    private JCheckBox day5;
    private JCheckBox day6;
    private JComboBox<String> comboHMSMode;
    private JCheckBox checkAMinimize;
    private JTabbedPane tabbedPane1;
    private JLabel lblTarget;
    private JButton btnDefGeneral;
    private JButton btnDefApp;
    private JLabel lblTimeout;
    private JSlider sliderTimeout;
    private JCheckBox checkAStartup;
    private JCheckBox checkARunMinimized;
    private JLabel lblAutoRefresh;
    private JSlider sliderAutoRefresh;
    private JComboBox<String> comboTheme;
    private JComboBox<IntSelection> comboBkmkHotkey;
    private JCheckBox checkBookmarks;
    private JCheckBox checkBkmkCtrl;
    private JCheckBox checkBkmkShift;
    private JButton btnDefBookmarks;
    private JLabel lblHotkey;
    private JCheckBox checkBkmkCloseWindow;
    private JRadioButton radOldRating;
    private JCheckBox checkShowAbove100;
    private JRadioButton alwaysPickTheHighestRadioButton;
    private JRadioButton radNewRating;
    private JCheckBox checkShowMonthLine;
    private JCheckBox checkShowYearLine;
    private JButton btnDefView;
    private JSlider sliderWeeklyTarget;
    private JLabel lblWeeklyTarget;
    private JPanel finHeader;
    private JCheckBox checkUseRankChart;
    private JCheckBox checkLimitMonthChart;
    private JRadioButton radDblClickView;
    private JRadioButton radDblClkBookmarks;
    private JRadioButton radDblClkLaunchB;
    private JCheckBox checkAutoBookmark;
    private JList<String> listPF;
    private JButton btnPFolderAdd;
    private JButton btnPFolderDelete;
    private JLabel lblSubFolder;
    private JSlider sliderSubFolder;
    private JButton btnPFUp;
    private JButton btnPFDn;
    private JCheckBox checkAutoBIgnoreExisting;
    private JTextField textAutoBExclExt;
    private JCheckBox checkBreak;
    private JTextField textBreakMsg;
    private JButton btnDefNotifs;
    private JSlider sliderBreakInterval;
    private JSlider sliderBreakMinimum;
    private JLabel lblBreakInterval;
    private JLabel lblBreakMinimum;
    private JRadioButton radBreakFromWork;
    private JRadioButton radBreakAny;
    private JPanel pnlBookmarksShortcut;
    private JPanel pnlBookmarksAuto;
    private JPanel pnlRemindersBreak;
    //endregion

    private boolean result = false;

    private DefaultListModel<String> pfModel;
    private IntSelectorModel vkSelectorModel;

    private final JCheckBox[] days = new JCheckBox[]{day0,day1,day2,day3,day4,day5,day6};

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private BookmarkMgr bookmarkMgr;

    private void EDITABLE_SECTION_HERE(){

        // edit for NEW COMBO BOXES
        comboHMSMode.setModel(new DefaultComboBoxModel<>(new String[]{HMSMode.STRICT.text, HMSMode.COLON.text}));
        String[] themes = new String[Theme.values().length];
        for (Theme x:Theme.values()) { themes[x.x] = x.label; }
        comboTheme.setModel(new DefaultComboBoxModel<>(themes));
        vkSelectorModel = new IntSelectorModel(BookmarkMgr.ALLOWED_VK_NAME, BookmarkMgr.ALLOWED_VK);
        comboBkmkHotkey.setModel(vkSelectorModel);

        // edit for NEW SEGMENTS
        registerDefaultButton(btnDefGeneral, "general");
        registerDefaultButton(btnDefView, "view");
        registerDefaultButton(btnDefBookmarks, "bookmarks");
        registerDefaultButton(btnDefNotifs, "notifs");
        registerDefaultButton(btnDefApp, "app");

        // edit for NEW SLIDERS
        resetSlider(sliderTarget, 4, 5*4, 12*4, 15*60);
        resetSlider(sliderTimeout, 1, 5, 30, 60);
        resetSlider(sliderAutoRefresh, 1, 10, 30, 60);
        resetSlider(sliderWeeklyTarget, 1, 5, 7, 1);
        resetSlider(sliderSubFolder, 0, 0, 4, 1);
        resetSlider(sliderBreakInterval, 1, 2*4, 9*4, 15*60);
        resetSlider(sliderBreakMinimum, 1, 5, 60, 60);
        bindTimeSlider(sliderTarget, lblTarget);
        bindTimeSlider(sliderTimeout, lblTimeout);
        bindTimeSlider(sliderAutoRefresh, lblAutoRefresh);
        bindTimeSlider(sliderBreakInterval, lblBreakInterval);
        bindTimeSlider(sliderBreakMinimum, lblBreakMinimum);
        bindCustomSlider(sliderWeeklyTarget, lblWeeklyTarget, "day", "days");
        bindCustomSliderSpecialCase(sliderSubFolder, lblSubFolder, "level", "levels", 0, "Don't scan");

        // edit for NEW CHECK BOXES / RADIO BUTTONS WITH CHILDREN
        addDependency(checkAStartup, checkARunMinimized);
        addDependency(radNewRating, checkUseRankChart);
        addDependency(checkAutoBookmark, pnlBookmarksAuto);
        addDependency(checkBookmarks, pnlBookmarksShortcut);
        addDependency(checkBreak, pnlRemindersBreak);

        // edit for NEW OPTIONS

        // <start> NEW OPTIONS

        String general = "general";
        reg(radDblClickView, general, ()->DOUBLE_CLICK_ACTION.get()==0, b->{if(b)DOUBLE_CLICK_ACTION.set(0);}, DOUBLE_CLICK_ACTION.def==0);
        reg(radDblClkBookmarks, general, ()->DOUBLE_CLICK_ACTION.get()==1, b->{if(b)DOUBLE_CLICK_ACTION.set(1);}, DOUBLE_CLICK_ACTION.def==1);
        reg(radDblClkLaunchB, general, ()->DOUBLE_CLICK_ACTION.get()==2, b->{if(b)DOUBLE_CLICK_ACTION.set(2);}, DOUBLE_CLICK_ACTION.def==2);
        reg(sliderTimeout, general, AFK_TIMEOUT_SECOND::get, AFK_TIMEOUT_SECOND::set, AFK_TIMEOUT_SECOND.def);
        reg(sliderTarget, general, DAILY_TARGET_SECOND::get,
                DAILY_TARGET_SECOND::set, DAILY_TARGET_SECOND.def);
        for (int i = 0; i < days.length; i++) {
            final int z = i;
            reg(days[z], general, ()->userGWDay(z), b->userSWDay(z,b), userDWDay(i));
        }
        reg(sliderWeeklyTarget, general, ()->WEEKLY_TARGET_DAYS.get(1, 7, true),
                WEEKLY_TARGET_DAYS::set, WEEKLY_TARGET_DAYS.def);

        String view = "view";
        reg(comboHMSMode, view, ()->appHMS()==HMSMode.COLON?1:0, i->appHMS(i==1?HMSMode.COLON:HMSMode.STRICT), defaultHMSMode()==HMSMode.COLON?1:0);
        reg(radNewRating, view, ()->!OLD_RATING.get(), b->OLD_RATING.set(!b), !OLD_RATING.def);
        reg(radOldRating, view, OLD_RATING::get, OLD_RATING::set, OLD_RATING.def);
        reg(checkShowAbove100, view, EXCEED_100_PERCENT::get, EXCEED_100_PERCENT::set, EXCEED_100_PERCENT.def);
        reg(checkShowMonthLine, view, ()->!DISABLE_MONTH_LINE.get(), b->DISABLE_MONTH_LINE.set(!b), !DISABLE_MONTH_LINE.def);
        reg(checkShowYearLine, view, ENABLE_YEAR_LINE::get, ENABLE_YEAR_LINE::set, ENABLE_YEAR_LINE.def);
        reg(checkUseRankChart, view, USE_RANK_MONTH_CHART::get, USE_RANK_MONTH_CHART::set, USE_RANK_MONTH_CHART.def);
        reg(checkLimitMonthChart, view, LIMIT_MONTH_CHART_UPPER::get, LIMIT_MONTH_CHART_UPPER::set, LIMIT_MONTH_CHART_UPPER.def);

        String app = "app";
        reg(checkAAskBeforeQuit, app, ()->appGBool(ASK_BEFORE_QUIT), i->appSBool(ASK_BEFORE_QUIT, i), appDBool(ASK_BEFORE_QUIT));
        reg(checkAMinimize, app, ()->appGBool(MINIMIZE_TO_TRAY), i->appSBool(MINIMIZE_TO_TRAY, i), appDBool(MINIMIZE_TO_TRAY));
        reg(checkAStartup, app, ()->appGBool(RUN_ON_STARTUP), i->appSBool(RUN_ON_STARTUP, i), appDBool(RUN_ON_STARTUP));
        reg(checkARunMinimized, app, ()->appGBool(RUN_MINIMIZED), i->appSBool(RUN_MINIMIZED, i), appDBool(RUN_MINIMIZED));
        reg(sliderAutoRefresh, app, ()->appGInt(REFRESH_TIMEOUT), i->appSInt(REFRESH_TIMEOUT, i), appDInt(REFRESH_TIMEOUT));
        reg(comboTheme, app, ()->getTheme().x, AppConfig::setTheme, defaultTheme().x);

        String bkmk = "bookmarks";
        reg(checkAutoBookmark, bkmk, ()->bkmkGBool(AUTO_BOOKMARK), b->bkmkSBool(AUTO_BOOKMARK, b), bkmkDBool(AUTO_BOOKMARK));
        reg(checkAutoBIgnoreExisting, bkmk, ()->!bkmkGBool(AUTO_INCLUDE_EXISTING), b->bkmkSBool(AUTO_INCLUDE_EXISTING, !b), !bkmkDBool(AUTO_INCLUDE_EXISTING));
        reg(textAutoBExclExt, bkmk, ()->String.join(",",bkmkGPFExclExt()), s->bkmkSPFExclExt(s.replace(" ", "").split(",")), String.join(",",bkmkDPFExclExt()));
        reg(sliderSubFolder, bkmk, ()->bkmkGInt(AUTO_SUBFOLDER), i->bkmkSInt(AUTO_SUBFOLDER, i), bkmkDInt(AUTO_SUBFOLDER));
        reg(checkBookmarks, bkmk, ()->bkmkGBool(ENABLED), b->bkmkSBool(ENABLED, b), bkmkDBool(ENABLED));
        reg(checkBkmkCtrl, bkmk, ()->bkmkGBool(CTRL), b->bkmkSBool(CTRL, b), bkmkDBool(CTRL));
        reg(checkBkmkShift, bkmk, ()->bkmkGBool(SHIFT), b->bkmkSBool(SHIFT, b), bkmkDBool(SHIFT));
        reg(checkBkmkCloseWindow, bkmk, ()->bkmkGBool(CLOSE_WINDOW), b->bkmkSBool(CLOSE_WINDOW, b),
                bkmkDBool(CLOSE_WINDOW));
        reg(comboBkmkHotkey, bkmk, ()->vkSelectorModel.getIndexFor(bkmkGInt(BookmarkInt.HOTKEY)),
                i->bkmkSInt(BookmarkInt.HOTKEY, vkSelectorModel.getElementAt(i).getValue()),
                vkSelectorModel.getIndexFor(bkmkDInt(BookmarkInt.HOTKEY)));

        String notifs = "notifs";
        reg(checkBreak, notifs, BREAK_REMINDER::get, BREAK_REMINDER::set, BREAK_REMINDER.def);
        reg(textBreakMsg, notifs, BREAK_MESSAGE::get, BREAK_MESSAGE::set, BREAK_MESSAGE.def);
        reg(sliderBreakInterval, notifs, BREAK_MAX_WORKDUR::get, BREAK_MAX_WORKDUR::set, BREAK_MAX_WORKDUR.def);
        reg(sliderBreakMinimum, notifs, BREAK_MIN_BREAKDUR::get, BREAK_MIN_BREAKDUR::set, BREAK_MIN_BREAKDUR.def);
        reg(radBreakFromWork, notifs, ()->!BREAK_ANY_USAGE.get(), b->BREAK_ANY_USAGE.set(!b), !BREAK_ANY_USAGE.def);
        reg(radBreakAny, notifs, BREAK_ANY_USAGE::get, BREAK_ANY_USAGE::set, BREAK_ANY_USAGE.def);

        // <end> NEW OPTIONS
    }

    public Settings(JFrame owner, BookmarkMgr bookmarkMgr) {
        super(owner, "Settings");
        this.bookmarkMgr = bookmarkMgr;
        setContentPane(contentPane);
        setMinimumSize(contentPane.getMinimumSize());
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        pack();
        setLocationRelativeTo(owner);
        buttonApply.setEnabled(false);

        buttonOK.addActionListener(e->onOK());
        buttonApply.addActionListener(e->onApply());
        buttonCancel.addActionListener(e->onCancel());

        // project folders list
        Main.applyButtonTheme(btnPFUp, btnPFDn, btnPFolderAdd, btnPFolderDelete);
        pfModel = new DefaultListModel<>();
        for (String s:BookmarkConfig.bkmkGPFList()) {
            pfModel.addElement(s);
        }
        listPF.setModel(pfModel);
        listPF.addListSelectionListener(listSelectionEvent -> {
            btnPFolderDelete.setEnabled(listPF.getSelectedIndex()!=-1);
            btnPFUp.setEnabled(listPF.getSelectedIndex()!=-1);
            btnPFDn.setEnabled(listPF.getSelectedIndex()!=-1);
        });
        btnPFolderAdd.addActionListener(e->addPFolder());
        btnPFolderDelete.addActionListener(e -> delPFolder());
        btnPFUp.addActionListener(e->movePF(-1));
        btnPFDn.addActionListener(e->movePF(+1));

        EDITABLE_SECTION_HERE();

        contentPane.registerKeyboardAction(e->onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
    }

    private void addPFolder() {
        JFileChooser destChooser = new JFileChooser();
        destChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        destChooser.setMultiSelectionEnabled(false);
        destChooser.showDialog(this, "Add folder");
        File dest = destChooser.getSelectedFile();
        if(dest != null){
            if(!pfModel.contains(dest.getPath())) {
                pfModel.addElement(dest.getPath());
            }
            enabler.setPermanentEnableApply(true);
        }
    }

    private void delPFolder() {
        int selectedIndex = listPF.getSelectedIndex();
        pfModel.remove(listPF.getSelectedIndex());
        if(selectedIndex<pfModel.size()) {
            listPF.setSelectedIndex(selectedIndex);
        }
        enabler.setPermanentEnableApply(true);
    }

    private void movePF(int move) {
        if(listPF.getSelectedIndex() == -1)return;
        int newI = listPF.getSelectedIndex() + move;
        if(newI < 0 || newI >= pfModel.size()) return;
        //swap
        String x = listPF.getSelectedValue();
        pfModel.set(listPF.getSelectedIndex(), pfModel.get(newI));
        pfModel.set(newI, x);
        listPF.setSelectedIndex(newI);
    }

    private void onApply() {
        result = true;

        //pflist
        String[] pfList = new String[pfModel.size()];
        for (int i = 0; i < pfList.length; i++) {
            pfList[i] = pfModel.get(i);
        }
        BookmarkConfig.bkmkSPFList(pfList);

        applyAll();
        buttonApply.setEnabled(false);
        bookmarkMgr.reinitHotkeyHook();
        Application.createOrDeleteStartupRegistry();
        if(Main.currentTheme != getTheme()){
            if(JOptionPane.showConfirmDialog(this, "Theme has been changed. Restart the app?",
            "Confirm restart", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                Application.restartApp(false);
            }
        }
    }

    public boolean getResult(){
        return result;
    }

    private void onOK() {
        if(buttonApply.isEnabled()) {
            onApply();
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    // "LOW LEVEL" CODE BELOW

    //region DEPENDENCY

    public static void addDependency(JToggleButton y, JComponent... x){
        ItemListener il = e->{
            for(JComponent x1:x) {
                setEnableRecursive(x1, y.isSelected());
            }
        };
        y.addItemListener(il);
        il.itemStateChanged(null);
    }

    private static void setEnableRecursive(JComponent x1, boolean y){
        for(Component x2:(x1).getComponents()){
            if(x2 instanceof JComponent) {
                setEnableRecursive((JComponent) x2, y);
            }
        }
        x1.setEnabled(y);
    }

    //endregion DEPENDENCY

    //region BINDSLIDER

    public static void resetSlider(JSlider slider, int min, int value, int max, int multiplier){
        slider.setMinimum(min*multiplier);
        slider.setMaximum(max*multiplier);
        slider.setValue(value*multiplier);
        slider.setMinorTickSpacing(multiplier);
        switch (multiplier){
            case 1:
                slider.setMajorTickSpacing(10);
                break;
            case 60:
                slider.setMajorTickSpacing(5*60);
                break;
            case 60*15:
            case 60*30:
                slider.setMajorTickSpacing(60*60);
                break;
            case 60*60:
                slider.setMajorTickSpacing(2*60*60);
                break;
            default:
                slider.setMajorTickSpacing(multiplier*2);
                break;
        }
        slider.setSnapToTicks(true);
        slider.setPaintTicks(true);
    }

    public static void bindTimeSlider(JSlider slider, JLabel label){
        slider.addChangeListener(e->label.setText(String.format("%s",
                FormatUtil.hmsLong(slider.getMinorTickSpacing()*Math.round(slider.getValue()*1f/slider.getMinorTickSpacing())))));
        label.setText(String.format("%s", FormatUtil.hmsLong(slider.getMinorTickSpacing()*Math.round(slider.getValue()*1f/slider.getMinorTickSpacing()))));
    }

    public static void bindCustomSlider(JSlider slider, JLabel label, String unit, String pluralUnit){
        slider.addChangeListener(e->label.setText(String.format("%d %s", slider.getValue(), (slider.getValue()==1?unit:pluralUnit))));
        label.setText(String.format("%d %s", slider.getValue(), (slider.getValue()==1?unit:pluralUnit)));
    }

    public static void bindCustomSliderSpecialCase(JSlider slider, JLabel label, String unit, String pluralUnit, int special, String specialS){
        slider.addChangeListener(e-> {
            if (slider.getValue() == special) {
                label.setText(specialS);
            } else {
                label.setText(String.format("%d %s", slider.getValue(), (slider.getValue() == 1 ? unit : pluralUnit)));
            }
        });
        if (slider.getValue() == special) {
            label.setText(specialS);
        } else {
            label.setText(String.format("%d %s", slider.getValue(), (slider.getValue() == 1 ? unit : pluralUnit)));
        }
    }

    //endregion BINDSLIDER

    //region REGISTER

    private static class Registration<T> {
        private final Supplier<T> guiGetter;
        private final Consumer<T> configSetter;
        private final T defaultValue;
        private final Consumer<T> guiSetter;

        private Registration(Supplier<T> guiGetter, Consumer<T> configSetter, T defaultValue, Consumer<T> guiSetter) {
            this.guiGetter = guiGetter;
            this.configSetter = configSetter;
            this.defaultValue = defaultValue;
            this.guiSetter = guiSetter;
        }

        private void resetDef(){
            guiSetter.accept(defaultValue);
        }

        private void apply() {
            configSetter.accept(guiGetter.get());
        }
    }

    private final HashMap<String, HashSet<Registration>> registrations = new HashMap<>();

    private <T> void register(String defaultNode, Supplier<T> guiGetter, Consumer<T> configSetter,
                              T defVal, Consumer<T> guiSetter){
        registrations.putIfAbsent(defaultNode, new HashSet<>());
        registrations.get(defaultNode).add(new Registration<>(guiGetter, configSetter, defVal, guiSetter));
    }

    private void registerDefaultButton(JButton x, String node){
        x.addActionListener(e-> resetDefault(node));
    }

    private void resetDefault(String node){
        if(registrations.containsKey(node)) {
            for (Registration y : registrations.get(node)) {
                y.resetDef();
            }
        }
    }

    private void applyAll(){
        for (String y: registrations.keySet()) {
            for(Registration x: registrations.get(y)){
                x.apply();
            }
        }
    }

    //endregion REGISTER

    //region ENABLER

    private final ApplyButtonEnabler enabler = new ApplyButtonEnabler(buttonApply);
    private static class ApplyButtonEnabler{
        //        private boolean adjusting = false;
        private final HashMap<Supplier, Supplier> sups = new HashMap<>();
        private final JButton buttonApply;
        private boolean permanentEnableApply;

        ApplyButtonEnabler(JButton buttonApply) {
            this.buttonApply = buttonApply;
        }

//        void changed() { if(!adjusting){ futureAdjust(); } }

        void add(JTextField x, Supplier<String> confGetter){
            sups.put(x::getText, confGetter);
            x.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { adjust(); }
                public void removeUpdate(DocumentEvent e) { adjust(); }
                public void changedUpdate(DocumentEvent e) { adjust(); } });
        }

        void add(JSlider x, Supplier<Integer> confGetter){
            sups.put(x::getValue, confGetter);
            x.addChangeListener(e -> adjust());
        }

        void add(JComboBox x, Supplier<Integer> confGetter){
            sups.put(x::getSelectedIndex, confGetter);
            x.addItemListener(e -> adjust());
        }

        void add(JToggleButton x, Supplier<Boolean> confGetter){
            sups.put(x::isSelected, confGetter);
            x.addItemListener(e -> adjust());
        }

        void setPermanentEnableApply(boolean permanentEnableApply) {
            this.permanentEnableApply = permanentEnableApply;
            buttonApply.setEnabled(permanentEnableApply);
        }
        //        private void futureAdjust(){
////            adjusting = true;
//            adjusting = adjust();
//        }

        private void adjust(){
            if(permanentEnableApply) return;
            for (Map.Entry<Supplier, Supplier> x:sups.entrySet()) {
                if(!x.getKey().get().equals(x.getValue().get())){
                    buttonApply.setEnabled(true);
                    return; //false;
                }
            }
            buttonApply.setEnabled(false);
//            return false;
        }
    }

    //endregion ENABLER

    //region REG (previously bind)

    private void reg(JComboBox x, String defNode, Supplier<Integer> confG, Consumer<Integer> confS, Integer defVal){
        enabler.add(x, confG);
        x.setSelectedIndex(confG.get());
        register(defNode, x::getSelectedIndex, confS, defVal, x::setSelectedIndex);
    }

    private void reg(JToggleButton x, String defNode, Supplier<Boolean> confG, Consumer<Boolean> confS, Boolean defVal){
        enabler.add(x, confG);
        x.setSelected(confG.get());
        register(defNode, x::isSelected, confS, defVal, x::setSelected);
    }

    private void reg(JSlider x, String defNode, Supplier<Integer> confG, Consumer<Integer> confS, Integer defVal){
        enabler.add(x, confG);
        x.setValue(confG.get());
        register(defNode, x::getValue, confS, defVal, x::setValue);
    }

    private void reg(JTextField x, String defNode, Supplier<String> confG, Consumer<String> confS, String defVal){
        enabler.add(x, confG);
        x.setText(confG.get());
        register(defNode, x::getText, confS, defVal, x::setText);
    }

    //endregion REG
}

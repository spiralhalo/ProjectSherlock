package xyz.spiralhalo.sherlock.dialog;

import xyz.spiralhalo.sherlock.Debug;
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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode.*;

public class Settings extends JDialog {
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
    private boolean result = false;

    private DefaultListModel<String> pfModel;
    private IntSelectorModel vkSelectorModel;

    private final JCheckBox[] days = new JCheckBox[]{day0,day1,day2,day3,day4,day5,day6};
    private final ApplyButtonEnabler enabler = new ApplyButtonEnabler(buttonApply);

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

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

//    private static class Dependency implements ItemListener {
//        public static void setChildren(JToggleButton y, Component... x){
//            y.addItemListener(new Dependency(x, y));
//        }
//
//        @Override
//        public void itemStateChanged(ItemEvent e) {
//            for (Component x1:x) { x1.setEnabled(y.isSelected()); }
//        }
//
//        private Component[] x;
//        private JToggleButton y;
//
//        private Dependency(Component[] x, JToggleButton y) {
//            this.x = x;
//            this.y = y;
//        }
//    }

    private static class ConfigBinding<T> {
        private final Supplier<T> guiGetter;
        private final Consumer<T> configSetter;
        private final T defaultValue;
        private final Consumer<T> guiSetter;

        private ConfigBinding(Supplier<T> guiGetter, Consumer<T> configSetter, T defaultValue, Consumer<T> guiSetter) {
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

    private final HashMap<String, HashSet<ConfigBinding>> bindings = new HashMap<>();

    private <T> void register(String defaultNode, Supplier<T> guiGetter, Consumer<T> configSetter,
                              T defVal, Consumer<T> guiSetter){
        bindings.putIfAbsent(defaultNode, new HashSet<>());
        bindings.get(defaultNode).add(new ConfigBinding<>(guiGetter, configSetter, defVal, guiSetter));
    }

    private void registerDefaultButton(JButton x, String node){
        x.addActionListener(e-> resetDefault(node));
    }

    private void resetDefault(String node){
        if(bindings.containsKey(node)) {
            for (ConfigBinding y : bindings.get(node)) {
                y.resetDef();
            }
        }
    }

    private void applyAll(){
        for (String y: bindings.keySet()) {
            for(ConfigBinding x: bindings.get(y)){
                x.apply();
            }
        }
    }

    private BookmarkMgr bookmarkMgr;

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

        // <start> EDITABLE

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
        registerDefaultButton(btnDefApp, "app");
        registerDefaultButton(btnDefBookmarks, "bookmarks");

        // edit for NEW SLIDERS
        resetSlider(sliderTarget, 4, 5*4, 12*4, 15*60);
        resetSlider(sliderTimeout, 1, 5, 30, 60);
        resetSlider(sliderAutoRefresh, 1, 10, 30, 60);
        resetSlider(sliderWeeklyTarget, 1, 5, 7, 1);
        resetSlider(sliderSubFolder, 0, 0, 4, 1);
        bindTimeSlider(sliderTarget, lblTarget);
        bindTimeSlider(sliderTimeout, lblTimeout);
        bindTimeSlider(sliderAutoRefresh, lblAutoRefresh);
        bindCustomSlider(sliderWeeklyTarget, lblWeeklyTarget, "day", "days");
        bindCustomSliderSpecialCase(sliderSubFolder, lblSubFolder, "level", "levels", 0, "Don't scan");

        // edit for NEW CHECK BOXES / RADIO BUTTONS WITH CHILDREN
        addDependency(checkAStartup, checkARunMinimized);
        addDependency(checkBookmarks, lblHotkey, checkBkmkCtrl, checkBkmkShift, comboBkmkHotkey);
        addDependency(radNewRating, checkUseRankChart);
        addDependency(checkAutoBookmark, checkAutoBIgnoreExisting);
        addDependency(checkAutoBookmark, sliderSubFolder);
//        Dependency.setChildren(radOldRating, checkShowAbove100);

        // edit for NEW OPTIONS

        // <start> NEW OPTIONS

        String general = "general";
        bind(radDblClickView, general, ()->userGInt(GENERAL, DOUBLE_CLICK_ACTION)==0, b->{if(b)userSInt(GENERAL, DOUBLE_CLICK_ACTION, 0);}, userDInt(GENERAL, DOUBLE_CLICK_ACTION)==0);
        bind(radDblClkBookmarks, general, ()->userGInt(GENERAL, DOUBLE_CLICK_ACTION)==1, b->{if(b)userSInt(GENERAL, DOUBLE_CLICK_ACTION, 1);}, userDInt(GENERAL, DOUBLE_CLICK_ACTION)==1);
        bind(radDblClkLaunchB, general, ()->userGInt(GENERAL, DOUBLE_CLICK_ACTION)==2, b->{if(b)userSInt(GENERAL, DOUBLE_CLICK_ACTION, 2);}, userDInt(GENERAL, DOUBLE_CLICK_ACTION)==2);
        bind(sliderTimeout, general, ()->userGInt(GENERAL, AFK_TIMEOUT_SECOND), i->userSInt(GENERAL, AFK_TIMEOUT_SECOND, i), userDInt(GENERAL, AFK_TIMEOUT_SECOND));
        bind(sliderTarget, general, ()->userGInt(GENERAL, DAILY_TARGET_SECOND),
                i->userSInt(GENERAL, DAILY_TARGET_SECOND, i), userDInt(GENERAL, DAILY_TARGET_SECOND));
        for (int i = 0; i < days.length; i++) {
            final int z = i;
            bind(days[z], general, ()->userGWDay(z), b->userSWDay(z,b), userDWDay(i));
        }
        bind(sliderWeeklyTarget, general, ()->userGInt(GENERAL, WEEKLY_TARGET_DAYS, 1, 7, true),
                i->userSInt(GENERAL, WEEKLY_TARGET_DAYS, i), userDInt(GENERAL, WEEKLY_TARGET_DAYS));

        String view = "view";
        bind(comboHMSMode, view, ()->appHMS()==HMSMode.COLON?1:0, i->appHMS(i==1?HMSMode.COLON:HMSMode.STRICT), defaultHMSMode()==HMSMode.COLON?1:0);
        bind(radNewRating, view, ()->!userGBool(VIEW, OLD_RATING), b->userSBool(VIEW, OLD_RATING, !b), !userDBool(VIEW, OLD_RATING));
        bind(radOldRating, view, ()->userGBool(VIEW, OLD_RATING), b->userSBool(VIEW, OLD_RATING, b), userDBool(VIEW, OLD_RATING));
        bind(checkShowAbove100, view, ()->userGBool(VIEW, EXCEED_100_PERCENT), b->userSBool(VIEW, EXCEED_100_PERCENT, b), userDBool(VIEW, EXCEED_100_PERCENT));
        bind(checkShowMonthLine, view, ()->!userGBool(VIEW, DISABLE_MONTH_LINE), b->userSBool(VIEW, DISABLE_MONTH_LINE, !b), !userDBool(VIEW, DISABLE_MONTH_LINE));
        bind(checkShowYearLine, view, ()->userGBool(VIEW, ENABLE_YEAR_LINE), b->userSBool(VIEW, ENABLE_YEAR_LINE, b), userDBool(VIEW, ENABLE_YEAR_LINE));
        bind(checkUseRankChart, view, ()->userGBool(VIEW, USE_RANK_MONTH_CHART), b->userSBool(VIEW, USE_RANK_MONTH_CHART, b), userDBool(VIEW, USE_RANK_MONTH_CHART));
        bind(checkLimitMonthChart, view, ()->userGBool(VIEW, LIMIT_MONTH_CHART_UPPER), b->userSBool(VIEW, LIMIT_MONTH_CHART_UPPER, b), userDBool(VIEW, LIMIT_MONTH_CHART_UPPER));

        String app = "app";
        bind(checkAAskBeforeQuit, app, ()->appGBool(ASK_BEFORE_QUIT), i->appSBool(ASK_BEFORE_QUIT, i), appDBool(ASK_BEFORE_QUIT));
        bind(checkAMinimize, app, ()->appGBool(MINIMIZE_TO_TRAY), i->appSBool(MINIMIZE_TO_TRAY, i), appDBool(MINIMIZE_TO_TRAY));
        bind(checkAStartup, app, ()->appGBool(RUN_ON_STARTUP), i->appSBool(RUN_ON_STARTUP, i), appDBool(RUN_ON_STARTUP));
        bind(checkARunMinimized, app, ()->appGBool(RUN_MINIMIZED), i->appSBool(RUN_MINIMIZED, i), appDBool(RUN_MINIMIZED));
        bind(sliderAutoRefresh, app, ()->appGInt(REFRESH_TIMEOUT), i->appSInt(REFRESH_TIMEOUT, i), appDInt(REFRESH_TIMEOUT));
        bind(comboTheme, app, ()->getTheme().x, AppConfig::setTheme, defaultTheme().x);

        String bkmk = "bookmarks";
        bind(checkAutoBookmark, bkmk, ()->bkmkGBool(AUTO_BOOKMARK), b->bkmkSBool(AUTO_BOOKMARK, b), bkmkDBool(AUTO_BOOKMARK));
        bind(checkAutoBIgnoreExisting, bkmk, ()->!bkmkGBool(AUTO_INCLUDE_EXISTING), b->bkmkSBool(AUTO_INCLUDE_EXISTING, !b), !bkmkDBool(AUTO_INCLUDE_EXISTING));
        bind(textAutoBExclExt, bkmk, ()->String.join(",",bkmkGPFExclExt()), s->bkmkSPFExclExt(s.replace(" ", "").split(",")), String.join(",",bkmkDPFExclExt()));
        bind(sliderSubFolder, bkmk, ()->bkmkGInt(AUTO_SUBFOLDER), i->bkmkSInt(AUTO_SUBFOLDER, i), bkmkDInt(AUTO_SUBFOLDER));
        bind(checkBookmarks, bkmk, ()->bkmkGBool(ENABLED), b->bkmkSBool(ENABLED, b), bkmkDBool(ENABLED));
        bind(checkBkmkCtrl, bkmk, ()->bkmkGBool(CTRL), b->bkmkSBool(CTRL, b), bkmkDBool(CTRL));
        bind(checkBkmkShift, bkmk, ()->bkmkGBool(SHIFT), b->bkmkSBool(SHIFT, b), bkmkDBool(SHIFT));
        bind(checkBkmkCloseWindow, bkmk, ()->bkmkGBool(CLOSE_WINDOW), b->bkmkSBool(CLOSE_WINDOW, b),
                bkmkDBool(CLOSE_WINDOW));
        bind(comboBkmkHotkey, bkmk, ()->vkSelectorModel.getIndexFor(bkmkGInt(BookmarkInt.HOTKEY)),
                i->bkmkSInt(BookmarkInt.HOTKEY, vkSelectorModel.getElementAt(i).getValue()),
                vkSelectorModel.getIndexFor(bkmkDInt(BookmarkInt.HOTKEY)));

        // <end> NEW OPTIONS

        // <end> EDITABLE

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

    public static void addDependency(JToggleButton y, Component... x){
        y.addItemListener(e->{
            for(Component x1:x) { x1.setEnabled(y.isSelected()); }
        });
        for(Component x1:x) { x1.setEnabled(y.isSelected()); }
    }

    private void bind(JComboBox x, String defNode, Supplier<Integer> confG, Consumer<Integer> confS, Integer defVal){
        enabler.add(x, confG);
        x.setSelectedIndex(confG.get());
        register(defNode, x::getSelectedIndex, confS, defVal, x::setSelectedIndex);
    }

    private void bind(JToggleButton x, String defNode, Supplier<Boolean> confG, Consumer<Boolean> confS, Boolean defVal){
        enabler.add(x, confG);
        x.setSelected(confG.get());
        register(defNode, x::isSelected, confS, defVal, x::setSelected);
    }

    private void bind(JSlider x, String defNode, Supplier<Integer> confG, Consumer<Integer> confS, Integer defVal){
        enabler.add(x, confG);
        x.setValue(confG.get());
        register(defNode, x::getValue, confS, defVal, x::setValue);
    }

    private void bind(JTextField x, String defNode, Supplier<String> confG, Consumer<String> confS, String defVal){
        enabler.add(x, confG);
        x.setText(confG.get());
        register(defNode, x::getText, confS, defVal, x::setText);
    }

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
}

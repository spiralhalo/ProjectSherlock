package xyz.spiralhalo.sherlock.dialog;

import xyz.spiralhalo.sherlock.SysIntegration;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.AppBool;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.AppInt;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.HMSMode;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.Theme;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.*;
import java.util.*;
import java.util.function.Supplier;

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
    private JComboBox comboHMSMode;
    private JCheckBox checkAMinimize;
    private JTabbedPane tabbedPane1;
    private JLabel lblTarget;
    private JButton btnDefTracking;
    private JButton btnDefApp;
    private JLabel lblTimeout;
    private JSlider sliderTimeout;
    private JCheckBox checkAStartup;
    private JCheckBox checkARunMinimized;
    private JLabel lblAutoRefresh;
    private JSlider sliderAutoRefresh;
    private JComboBox comboTheme;
    private boolean result = false;

    private final JCheckBox[] days = new JCheckBox[]{day0,day1,day2,day3,day4,day5,day6};
    private final ApplyButtonEnabler enabler = new ApplyButtonEnabler(buttonApply);

    private static class ApplyButtonEnabler{
        private boolean adjusting = false;
        private final HashMap<JSlider, Supplier<Integer>> sliders = new HashMap<>();
        private final HashMap<JComboBox, Supplier<Integer>> comboBoxes = new HashMap<>();
        private final HashMap<JCheckBox, Supplier<Boolean>> checkBoxes = new HashMap<>();
        private final JButton buttonApply;

        ApplyButtonEnabler(JButton buttonApply) {
            this.buttonApply = buttonApply;
        }

        void changed(EventObject e) { if(!adjusting){ futureAdjust(); } }

        void addSlider(JSlider slider, Supplier<Integer> valueSupplier){
            sliders.put(slider, valueSupplier);
            slider.addChangeListener(this::changed);
        }

        void addCheckBox(JCheckBox checkBox, Supplier<Boolean> valueSupplier){
            checkBoxes.put(checkBox, valueSupplier);
            checkBox.addItemListener(this::changed);
        }

        void addComboBox(JComboBox checkBox, Supplier<Integer> valueSupplier){
            comboBoxes.put(checkBox, valueSupplier);
            checkBox.addItemListener(this::changed);
        }

        private void futureAdjust(){
            adjusting = true;
            new java.util.Timer().schedule(new TimerTask() {
                @Override public void run() { adjust(); }
            }, 100);
        }

        private void adjust(){
            for (Map.Entry<JSlider, Supplier<Integer>> x:sliders.entrySet()) {
                if(x.getKey().getValue() != x.getValue().get()){
                    buttonApply.setEnabled(true);
                    adjusting = false;
                    return;
                }
            }
            for (Map.Entry<JComboBox, Supplier<Integer>> x:comboBoxes.entrySet()) {
                if(x.getKey().getSelectedIndex() != x.getValue().get()){
                    buttonApply.setEnabled(true);
                    adjusting = false;
                    return;
                }
            }
            for (Map.Entry<JCheckBox, Supplier<Boolean>> x:checkBoxes.entrySet()) {
                if(x.getKey().isSelected() != x.getValue().get()){
                    buttonApply.setEnabled(true);
                    adjusting = false;
                    return;
                }
            }
            buttonApply.setEnabled(false);
            adjusting = false;
        }
    }

    private static class Dependency implements ItemListener {
        public static void setChildren(JCheckBox y, Component... x){
            y.addItemListener(new Dependency(x, y));
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            for (Component x1:x) {
                x1.setEnabled(y.isSelected());
            }
        }

        private Component[] x;
        private JCheckBox y;

        private Dependency(Component[] x, JCheckBox y) {
            this.x = x;
            this.y = y;
        }
    }

    public Settings(JFrame owner) {
        super(owner, "Settings");
        setContentPane(contentPane);
        setMinimumSize(contentPane.getMinimumSize());
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        pack();
        setLocationRelativeTo(owner);
        buttonApply.setEnabled(false);

        comboHMSMode.setModel(new DefaultComboBoxModel(new String[]{"12:45:30", "12h 45m 30s"}));
        String[] themes = new String[Theme.values().length];
        for (Theme x:Theme.values()) {
            themes[x.x] = String.format("%s%s",x.label, (x.dark?" (dark)":""));
        }
        comboTheme.setModel(new DefaultComboBoxModel(themes));

        buttonOK.addActionListener(e -> onOK());
        buttonApply.addActionListener(e -> onApply());
        buttonCancel.addActionListener(e -> onCancel());
        btnDefTracking.addActionListener(e -> defaultTracking());
        btnDefApp.addActionListener(e -> defaultApp());
        resetSlider(sliderTarget, 4, 5*4, 12*4, 15*60);
        resetSlider(sliderTimeout, 1, 5, 30, 60);
        resetSlider(sliderAutoRefresh, 1, 10, 30, 60);
        bindTimeSlider(sliderTarget, lblTarget);
        bindTimeSlider(sliderTimeout, lblTimeout);
        bindTimeSlider(sliderAutoRefresh, lblAutoRefresh);

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        Dependency.setChildren(checkAStartup, checkARunMinimized);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        init();
    }

    private void init() {
        bind(checkAAskBeforeQuit, ()->AppConfig.getBool(AppBool.ASK_BEFORE_QUIT));
        bind(checkAMinimize, ()->AppConfig.getBool(AppBool.MINIMIZE_TO_TRAY));
        bind(checkAStartup, ()->AppConfig.getBool(AppBool.RUN_ON_STARTUP));
        bind(checkARunMinimized, ()->AppConfig.getBool(AppBool.RUN_MINIMIZED));
        bind(comboHMSMode, ()->AppConfig.getHMSMode()==HMSMode.COLON?0:1);
        bind(comboTheme, ()->AppConfig.getTheme().x);
        bind(sliderAutoRefresh, ()->AppConfig.getInt(AppInt.REFRESH_TIMEOUT));
        bind(sliderTimeout, ()->UserConfig.getInt(UserNode.TRACKING, UserInt.AFK_TIMEOUT_SECOND));
        bind(sliderTarget, ()->UserConfig.getInt(UserNode.TRACKING, UserInt.DAILY_TARGET_SECOND));
        for (int i = 0; i < days.length; i++) {
            final int z = i;
            bind(days[z], ()->UserConfig.isWorkDay(z));
        }
    }

    private void defaultApp() {
        checkAAskBeforeQuit.setSelected(AppConfig.defaultBoolean(AppBool.ASK_BEFORE_QUIT));
        checkAMinimize.setSelected(AppConfig.defaultBoolean(AppBool.MINIMIZE_TO_TRAY));
        checkAStartup.setSelected(AppConfig.defaultBoolean(AppBool.RUN_ON_STARTUP));
        checkARunMinimized.setSelected(AppConfig.defaultBoolean(AppBool.RUN_MINIMIZED));
        sliderAutoRefresh.setValue(AppConfig.defaultInt(AppInt.REFRESH_TIMEOUT));
        comboHMSMode.setSelectedIndex(AppConfig.defaultHMSMode()==HMSMode.COLON?0:1);
        comboTheme.setSelectedIndex(AppConfig.getTheme().x);
    }

    private void defaultTracking() {
        sliderTimeout.setValue(UserConfig.defaultInt(UserNode.TRACKING, UserInt.AFK_TIMEOUT_SECOND));
        sliderTarget.setValue(UserConfig.defaultInt(UserNode.TRACKING, UserInt.DAILY_TARGET_SECOND));
        for (int i = 0; i < days.length; i++) { days[i].setSelected(UserConfig.defaultWorkDay(i)); }
    }

    private void onApply() {
        result = true;
        AppConfig.setBoolean(AppBool.ASK_BEFORE_QUIT, checkAAskBeforeQuit.isSelected());
        AppConfig.setBoolean(AppBool.MINIMIZE_TO_TRAY, checkAMinimize.isSelected());
        AppConfig.setBoolean(AppBool.RUN_ON_STARTUP, checkAStartup.isSelected());
        AppConfig.setBoolean(AppBool.RUN_MINIMIZED, checkARunMinimized.isSelected());
        AppConfig.setHMSMode(comboHMSMode.getSelectedIndex()==0?HMSMode.COLON:HMSMode.STRICT);
        AppConfig.setTheme(comboTheme.getSelectedIndex());
        AppConfig.setInt(AppInt.REFRESH_TIMEOUT, sliderAutoRefresh.getValue());
        UserConfig.setInt(UserNode.TRACKING, UserInt.AFK_TIMEOUT_SECOND, sliderTimeout.getValue());
        UserConfig.setInt(UserNode.TRACKING, UserInt.DAILY_TARGET_SECOND, sliderTarget.getValue());
        for (int i = 0; i < days.length; i++) { UserConfig.setWorkDay(i, days[i].isSelected()); }
        buttonApply.setEnabled(false);
        SysIntegration.createOrDeleteStartupRegistry();
    }

    private void bind(JComboBox x, Supplier<Integer> y){
        enabler.addComboBox(x, y);
        x.setSelectedIndex(y.get());
    }

    private void bind(JCheckBox x, Supplier<Boolean> y){
        enabler.addCheckBox(x, y);
        x.setSelected(y.get());
    }

    private void bind(JSlider x, Supplier<Integer> y){
        enabler.addSlider(x, y);
        x.setValue(y.get());
    }

    private void resetSlider(JSlider slider, int min, int value, int max, int multiplier){
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

    private void bindTimeSlider(JSlider slider, JLabel label){
        slider.addChangeListener(e->label.setText(String.format("%s",
                FormatUtil.hmsLong(slider.getMinorTickSpacing()*(slider.getValue()/slider.getMinorTickSpacing())))));
        label.setText(String.format("%s", FormatUtil.hmsLong(slider.getMinorTickSpacing()*(slider.getValue()/slider.getMinorTickSpacing()))));
    }

    public boolean getResult(){
        return result;
    }

    private void onOK() {
        onApply();
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}

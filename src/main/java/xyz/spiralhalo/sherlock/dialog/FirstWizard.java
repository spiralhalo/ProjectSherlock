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

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Main;
import xyz.spiralhalo.sherlock.bookmark.BookmarkConfig;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

import static xyz.spiralhalo.sherlock.dialog.Settings.*;
import static xyz.spiralhalo.sherlock.persist.settings.AppConfig.getTheme;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserBool.OLD_RATING;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserBool.USE_RANK_MONTH_CHART;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt.DAILY_TARGET_SECOND;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt.DOUBLE_CLICK_ACTION;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode.GENERAL;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode.VIEW;

public class FirstWizard extends JDialog {
    private JPanel contentPane;
    private JButton btnNext;
    private JButton buttonCancel;
    private JPanel panel1;
    private JPanel panel2;
    private JSlider sliderTarget;
    private JLabel lblTarget;
    private JPanel panel3;
    private JRadioButton radRatingNew;
    private JCheckBox checkUseRankChart;
    private JPanel panel5;
    private JButton btnPFUp;
    private JButton btnPFDn;
    private JButton btnPFolderAdd;
    private JButton btnPFolderDelete;
    private JList<String> listPF;
    private JPanel lastPanel;
    private JSlider sliderSubFolder;
    private JLabel lblSubFolder;
    private JPanel panel4;
    private JPanel panel6;
    private JCheckBox checkAStartup;
    private JCheckBox checkARunMinimized;
    private JRadioButton radDblClickView;
    private JRadioButton radDblClkBookmarks;
    private JRadioButton radDblClkLaunchB;
    private JPanel cardPane;
    private JButton btnPrev;
    private JButton btnFinish;
    private JRadioButton radRatingOld;
    private JRadioButton radCurrentTheme;
    private JRadioButton radLightTheme;
    private JRadioButton radDarkTheme;
    private JRadioButton radSysTheme;
    private JCheckBox checkAutoBookmark;

    private DefaultListModel<String> pfModel;

    public FirstWizard(JFrame owner) {
        super(owner, "First run ...witch!"); // Wizard is a term referring to a person, usually a man, with extraordinary magical powers. In the computing world, "wizard" refers to a person with extraordinary level of understanding of the computer technology, often used in a self-proclaimed way. The term wizard then later on creeps into software marketing, where the term is used to describe subset of a software that aids a user in accomplishing a certain task in a step-by-step fashion. However, it is questionable whether this usage survives to the modern time, as such software subsets often no longer accompanied with the word "wizard" (For example, step-by-step user registration is never called a Registration Wizard). In fact, my first exposure of such usage was on the "InstallShield Wizard", and back then those captions confuses me greatly as to what could possibly be the connection between fictious men of magnificent magical prowess with an installation procedure of a computer software. Thus I would argue that to the modern, casual end-users, the term "Wizard" is just confusing and there is no harm in putting an even more confusing term on the caption of this dialog window.
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btnNext);

        btnPrev.addActionListener(e -> onPrev());
        btnNext.addActionListener(e -> onNext());
        btnFinish.addActionListener(e -> onFinish());
        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

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

        // sliders
        resetSlider(sliderTarget, 4, 5*4, 12*4, 15*60);
        resetSlider(sliderSubFolder, 0, 0, 4, 1);
        bindTimeSlider(sliderTarget, lblTarget);
        bindCustomSliderSpecialCase(sliderSubFolder, lblSubFolder, "level", "levels", 0, "Don't scan");

        // checkboxes with dependencies
        addDependency(checkAStartup, checkARunMinimized);
        addDependency(radRatingNew, checkUseRankChart);
//        addDependency(checkAutoBookmark, checkAutoBIgnoreExisting);
        addDependency(checkAutoBookmark, sliderSubFolder);

        // step 1: target
        sliderTarget.setValue(DAILY_TARGET_SECOND.get());
        // step 2: rating
        radRatingNew.setSelected(!OLD_RATING.get());
        radRatingOld.setSelected(OLD_RATING.get());
        checkUseRankChart.setSelected(USE_RANK_MONTH_CHART.get());
        // step 3: theme
        radCurrentTheme.setSelected(true);
        // step 4: auto-bookmarks
        checkAutoBookmark.setSelected(BookmarkConfig.bkmkGBool(BookmarkConfig.BookmarkBool.AUTO_BOOKMARK));
        sliderSubFolder.setValue(BookmarkConfig.bkmkGInt(BookmarkConfig.BookmarkInt.AUTO_SUBFOLDER));
//        checkAutoBIgnoreExisting.setSelected(!BookmarkConfig.bkmkGBool(BookmarkConfig.BookmarkBool.AUTO_INCLUDE_EXISTING));
        radDblClickView.setSelected(DOUBLE_CLICK_ACTION.get()==0);
        radDblClkBookmarks.setSelected(DOUBLE_CLICK_ACTION.get()==1);
        radDblClkLaunchB.setSelected(DOUBLE_CLICK_ACTION.get()==2);
        // step 5: startup
        checkAStartup.setSelected(AppConfig.appGBool(AppConfig.AppBool.RUN_ON_STARTUP));
        checkARunMinimized.setSelected(AppConfig.appGBool(AppConfig.AppBool.RUN_MINIMIZED));

        pack();
        setLocationRelativeTo(owner);
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
        }
    }

    private void delPFolder() {
        int selectedIndex = listPF.getSelectedIndex();
        pfModel.remove(listPF.getSelectedIndex());
        if(selectedIndex<pfModel.size()) {
            listPF.setSelectedIndex(selectedIndex);
        }
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

    private void onPrev() {
        ((CardLayout)cardPane.getLayout()).previous(cardPane);
        btnFinish.setVisible(lastPanel.isVisible());
        btnPrev.setVisible(!panel1.isVisible());
        btnNext.setVisible(!lastPanel.isVisible());
    }

    private void onNext() {
        ((CardLayout)cardPane.getLayout()).next(cardPane);
        btnFinish.setVisible(lastPanel.isVisible());
        btnPrev.setVisible(!panel1.isVisible());
        btnNext.setVisible(!lastPanel.isVisible());
    }

    private void onFinish() {

        //pflist
        String[] pfList = new String[pfModel.size()];
        for (int i = 0; i < pfList.length; i++) {
            pfList[i] = pfModel.get(i);
        }
        BookmarkConfig.bkmkSPFList(pfList);

        // step 1: target
        DAILY_TARGET_SECOND.set(sliderTarget.getValue());
        // step 2: rating
        OLD_RATING.set(radRatingOld.isSelected());
        USE_RANK_MONTH_CHART.set(checkUseRankChart.isSelected());
        // step 3: theme
        AppConfig.Theme selectedTheme = radLightTheme.isSelected()? AppConfig.Theme.BUSINESS:
                (radDarkTheme.isSelected()? AppConfig.Theme.GRAPHITE:
                        (radSysTheme.isSelected()?AppConfig.Theme.SYSTEM:null));
        if(selectedTheme!=null){
            AppConfig.setTheme(selectedTheme.x);
        }
        // step 4: auto-bookmarks
        BookmarkConfig.bkmkSBool(BookmarkConfig.BookmarkBool.AUTO_BOOKMARK, checkAutoBookmark.isSelected());
//        BookmarkConfig.bkmkSBool(BookmarkConfig.BookmarkBool.AUTO_INCLUDE_EXISTING, !checkAutoBIgnoreExisting.isSelected());
        BookmarkConfig.bkmkSInt(BookmarkConfig.BookmarkInt.AUTO_SUBFOLDER, sliderSubFolder.getValue());
        if(radDblClkBookmarks.isSelected()){
            DOUBLE_CLICK_ACTION.set(1);
        } else if(radDblClkLaunchB.isSelected()){
            DOUBLE_CLICK_ACTION.set(2);
        } else {
            DOUBLE_CLICK_ACTION.set(0);
        }
        // step 5: startup
        AppConfig.appSBool(AppConfig.AppBool.RUN_ON_STARTUP, checkAStartup.isSelected());
        AppConfig.appSBool(AppConfig.AppBool.RUN_MINIMIZED, checkARunMinimized.isSelected());

        Application.createOrDeleteStartupRegistry();

        AppConfig.setWizardLastVersion();

        if(Main.currentTheme != getTheme()){
            if(JOptionPane.showConfirmDialog(this, "Theme has been changed. Restart the app?",
                    "Confirm restart", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                Application.restartApp(false);
            }
        }

        dispose();
    }

    private void onCancel() {
        AppConfig.setWizardLastVersion();
        dispose();
    }
}

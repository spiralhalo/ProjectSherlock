package xyz.spiralhalo.sherlock.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FirstWizard extends JDialog {
    private JPanel contentPane;
    private JButton btnNext;
    private JButton buttonCancel;
    private JPanel panel1;
    private JPanel panel2;
    private JSlider sliderTarget;
    private JLabel lblTarget;
    private JPanel panel3;
    private JRadioButton newRankingSystemRadioButton;
    private JCheckBox checkUseRankChart;
    private JPanel panel5;
    private JButton btnPFUp;
    private JButton btnPFDn;
    private JButton btnPFolderAdd;
    private JButton btnPFolderDelete;
    private JList listPF;
    private JPanel lastPanel;
    private JSlider sliderSubFolder;
    private JLabel lblSubFolder;
    private JCheckBox checkAutoBIgnoreExisting;
    private JPanel panel4;
    private JPanel panel7;
    private JCheckBox checkAStartup;
    private JCheckBox checkARunMinimized;
    private JRadioButton radDblClickView;
    private JRadioButton radDblClkBookmarks;
    private JRadioButton radDblClkLaunchB;
    private JPanel cardPane;
    private JButton btnPrev;
    private JButton btnFinish;
    private JPanel panel6;
    private JCheckBox enableOCRForTheCheckBox;
    private JTable table1;

    public FirstWizard(JFrame owner) {
        super(owner);
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

        pack();
        setLocationRelativeTo(owner);
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
        // add your code here if necessary
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}

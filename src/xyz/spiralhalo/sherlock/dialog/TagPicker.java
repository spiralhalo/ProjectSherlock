package xyz.spiralhalo.sherlock.dialog;

import xyz.spiralhalo.sherlock.EnumerateWindows;
import xyz.spiralhalo.sherlock.Main;
import xyz.spiralhalo.sherlock.util.swing.ArrayListModel;
import xyz.spiralhalo.sherlock.util.swing.TrimmedString;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;

public class TagPicker extends JDialog {
    private static final String[] EXCLUDED = new String[]{"windows\\explorer.exe"};

    public static String[] select(JDialog origin){
        TagPicker x = new TagPicker(origin);
        x.setVisible(true);
        return x.result;
    }

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox comboKeywords;
    private JList listKeywords;
    private JButton btnRefresh;
    private JButton btnClear;
    private JCheckBox checkFilter;
    private String[] result;

    private TagPicker(JDialog owner) {
        super(owner, "Keyword picker");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        btnRefresh.addActionListener(e->refresh());
        btnClear.addActionListener(e->listKeywords.clearSelection());
        comboKeywords.addItemListener(e->select());
        checkFilter.addItemListener(e->select());
        Main.applyButtonTheme(btnRefresh);

        listKeywords.setSelectionModel(new DefaultListSelectionModel() {
            private int i0 = -1;
            private int i1 = -1;

            public void setSelectionInterval(int index0, int index1) {
                if(i0 == index0 && i1 == index1){
                    if(getValueIsAdjusting()){
                        setValueIsAdjusting(false);
                        setSelection(index0, index1);
                    }
                }else{
                    i0 = index0;
                    i1 = index1;
                    setValueIsAdjusting(false);
                    setSelection(index0, index1);
                }
            }
            private void setSelection(int index0, int index1){
                if(super.isSelectedIndex(index0)) {
                    super.removeSelectionInterval(index0, index1);
                }else {
                    super.addSelectionInterval(index0, index1);
                }
            }
        });
        listKeywords.setCellRenderer(new DefaultListCellRenderer());

        refresh();
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }

    private void refresh() {
        comboKeywords.setModel(new DefaultComboBoxModel<>(TrimmedString.createArray(EnumerateWindows.getOpenWindowTitles(EXCLUDED, false), 60)));
        select();
    }

    private void select() {
        String[] x = ((TrimmedString)comboKeywords.getSelectedItem()).getContent().replace(',', ' ')
                .split("[ $-/:-?{-~!\"^\\\\`\\[\\]]");
        ArrayList<String> lowercase = new ArrayList<>();
        ArrayList<String> selected = new ArrayList<>();
        for (String y:x) {
            if(y.length()>0 && !lowercase.contains(y.toLowerCase()) && (y.length()>=3 || !checkFilter.isSelected())){
                selected.add(y);
                lowercase.add(y.toLowerCase());
            }
        }
        listKeywords.setModel(new ArrayListModel<>(selected.toArray(new String[0])));
    }

    private void onOK() {
        int[] x = listKeywords.getSelectedIndices();
        result = new String[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = String.valueOf(listKeywords.getModel().getElementAt(x[i]));
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}

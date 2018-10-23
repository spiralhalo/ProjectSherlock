package xyz.spiralhalo.sherlock.focus;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.swing.DurationSelection;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FocusConfig extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable tblProjects;
    private JCheckBox checkEnable;
    private JLabel lblStatus;
    private JComboBox<DurationSelection> cbTime;
    private final FocusMgr mgr;

    public FocusConfig(JFrame parent, FocusMgr mgr) {
        super(parent);
        this.mgr = mgr;
        setTitle("Focus mode configuration");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        tblProjects.setDefaultRenderer(Object.class, new ActiveSummaryCell());
        tblProjects.setModel(new ActiveSummaryModel(mgr.getProjectList().getActiveProjects()));
        tblProjects.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if(FocusState.getInstance().isEnabled()){
            checkEnable.setSelected(true);
            Project p = FocusState.getInstance().getProject(mgr.getProjectList());
            if(p!=null){
                lblStatus.setText(String.format("Focus mode is currently enabled for %s", p.toString()));
            } else {
                lblStatus.setText("Focus mode status is unknown. Please disable or reconfigure.");
            }
        }

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(parent);
    }

    private void onOK() {
        if(checkEnable.isSelected() && tblProjects.getSelectedRow() != -1){
            Project p = ((ActiveSummaryModel)tblProjects.getModel()).getProject(tblProjects.getSelectedRow());
            if(p != null) {
                mgr.turnOn(p, 1000*((DurationSelection)cbTime.getSelectedItem()).getValue());
            } else {
                mgr.turnOff();
            }
        } else {
            mgr.turnOff();
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        DurationSelection[] x = new DurationSelection[13];
        x[0] = new DurationSelection();
        for (int i = 1; i < 13; i++) {
            x[i] = new DurationSelection(3600*i, DurationSelection.HMSMode.Long);
        }
        cbTime = new JComboBox<>(x);
    }
}

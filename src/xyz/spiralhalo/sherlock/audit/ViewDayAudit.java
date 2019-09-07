package xyz.spiralhalo.sherlock.audit;

import xyz.spiralhalo.sherlock.audit.persist.DayAudit;
import xyz.spiralhalo.sherlock.focus.ActiveSummaryCell;
import xyz.spiralhalo.sherlock.focus.ActiveSummaryModel;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.ImgUtil;
import xyz.spiralhalo.sherlock.util.ListUtil;

import javax.swing.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

public class ViewDayAudit extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable tblProjects;
    private JButton btnAddMilestone;
    private JButton btnAddFinances;
    private JLabel lblDate;
    private JButton btnEditMilestone;
    private JButton btnDeleteMilestone;
    private JTable table1;
    private JTable table2;
    private JTextPane textPane1;
    private JButton btnAddNote;
    private JButton btnEditNote;
    private JButton btnDeleteNote;
    private JButton btnEditFinances;
    private JButton btnDeleteFinances;

    private final DayAudit dayAudit;
    private final ProjectList projectList;
    private boolean result = false;

    public ViewDayAudit(JFrame parent, LocalDate date, ProjectList projectList) {
        super(parent);
        setContentPane(contentPane);
        String dateText = FormatUtil.DTF_DATE_SELECTOR.format(date);
        setTitle(String.format("Day Audit for: %s", dateText));
        lblDate.setText(dateText);

        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        final JButton[] iconButtons = new JButton[]{btnAddFinances, btnAddMilestone, btnAddNote, btnDeleteMilestone,
                btnDeleteNote, btnEditMilestone, btnEditNote, btnEditFinances, btnDeleteFinances};
        for (JButton btn : iconButtons) {
            btn.setIcon(ImgUtil.autoColorIcon(btn.getIcon()));
        }

        ZoneId z = ZoneId.systemDefault();
        ArrayList<Project> relevantProjects = new ArrayList<>();

        for(Project p : ListUtil.extensiveIterator(projectList.getActiveProjects(), projectList.getFinishedProjects()))
        {
            if(!p.getStartDate().withZoneSameInstant(z).toLocalDate().isAfter(date)
                    && (!p.isFinished() || !p.getFinishedDate().withZoneSameInstant(z).toLocalDate().isBefore(date))){
                relevantProjects.add(p);
            }
        }

        tblProjects.setDefaultRenderer(Object.class, new ActiveSummaryCell());
        tblProjects.setModel(new ActiveSummaryModel(relevantProjects));
        tblProjects.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblProjects.getTableHeader().setReorderingAllowed(false);

        dayAudit = AuditListMgr.getDayAudit(z, date);
        this.projectList = projectList;

        pack();
        setMinimumSize(contentPane.getSize());
        setLocationRelativeTo(parent);
    }

    private void refresh() {
        for (long p :dayAudit.getProjects()) {

        }
    }

    private void onOK() {
        result = true;
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public boolean isResult() {
        return result;
    }
}

package xyz.spiralhalo.sherlock.dialog;

import xyz.spiralhalo.sherlock.report.DayModel;
import xyz.spiralhalo.sherlock.report.DurationCell;
import xyz.spiralhalo.sherlock.report.MonthModel;

import javax.swing.*;

public class ViewProject extends JDialog {
    private JPanel contentPane;
    private JTabbedPane tabbedPane1;
    private JScrollPane daily;
    private JTable tableDaily;
    private JPanel errorDay;
    private JScrollPane monthly;
    private JTable tableMonthly;
    private JPanel errorMonth;
    private JTextPane fieldTag;
    private JTextPane fieldName;

    public ViewProject(JFrame owner, String project, String projectTags, DayModel dayModel, MonthModel monthModel) {
        super(owner, "Project viewer");
        setContentPane(contentPane);
        setMinimumSize(contentPane.getMinimumSize());
        setModal(true);
        fieldName.setText(project);
        fieldTag.setText(projectTags);
        tableDaily.setDefaultRenderer(Integer.class, new DurationCell());
        tableMonthly.setDefaultRenderer(Integer.class, new DurationCell());
        tableDaily.setModel(dayModel);
        tableMonthly.setModel(monthModel);
        if(dayModel.getRowCount() == 0){
            errorDay.setVisible(true);
            daily.setVisible(false);
        }
        if(monthModel.getRowCount() == 0){
            errorMonth.setVisible(true);
            monthly.setVisible(false);
        }
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }
}

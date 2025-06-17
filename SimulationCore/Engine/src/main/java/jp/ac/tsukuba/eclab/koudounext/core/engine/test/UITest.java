package jp.ac.tsukuba.eclab.koudounext.core.engine.test;

import jp.ac.tsukuba.eclab.koudounext.core.engine.controller.SimulationController;
import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation.SimulationAlreadyPausedException;
import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation.SimulationAlreadyResumedException;
import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation.SimulationAlreadyStartedException;
import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation.SimulationNotPausedException;
import jp.ac.tsukuba.eclab.koudounext.core.engine.utils.SimulationConfig;

import javax.swing.*;

public class UITest extends JDialog {
    private JPanel mPanel;
    private JTextField mTextMaxSteps;
    private JButton mButtonInit;
    private JButton mButtonStart;
    private JTextField mTextStepInterval;
    private JButton mButtonPrev;
    private JButton mButtonResume;
    private JButton mButtonNext;
    private JButton mButtonPause;
    private SimulationController mController;
    private SimulationConfig mConfig;

    public UITest() {
        setContentPane(mPanel);
        setModal(true);
        mController = SimulationController.getInstance();

        mButtonInit.addActionListener(e -> {
            try {
                int maxSteps = Integer.parseInt(mTextMaxSteps.getText());
                int stepInterval = Integer.parseInt(mTextStepInterval.getText());
                mConfig=new SimulationConfig();
                mConfig.setMaxStep(maxSteps);
                mConfig.setStepIntervalMillisecond(stepInterval);
                mController.init(mConfig);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a legal value!", "Illegal inputs",
                        JOptionPane.ERROR_MESSAGE);
            }
            AgentsUI.getInstance();
        });

        mButtonStart.addActionListener(e -> {
            try {
                mController.startSimulation();
            } catch (SimulationAlreadyStartedException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Illegal action",
                        JOptionPane.ERROR_MESSAGE);
            }

        });

        mButtonResume.addActionListener(e -> {
            try {
                mController.resumeSimulation();
            } catch (SimulationAlreadyResumedException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Illegal action",
                        JOptionPane.ERROR_MESSAGE);
            }

        });

        mButtonNext.addActionListener(e -> {
            try {
                mController.doSimulationNextStep();
            } catch (SimulationNotPausedException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Illegal action",
                        JOptionPane.ERROR_MESSAGE);
            }

        });

        mButtonPause.addActionListener(e -> {
            try {
                mController.pauseSimulation();
            }catch (SimulationAlreadyPausedException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Illegal action",
                        JOptionPane.ERROR_MESSAGE);
            }

        });

        mButtonPrev.addActionListener(e -> {
            try {
                mController.doSimulationPrevStep();
            }catch (SimulationNotPausedException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Illegal action",
                        JOptionPane.ERROR_MESSAGE);
            }

        });

    }

    public static void main(String[] args) {
        UITest dialog = new UITest();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}

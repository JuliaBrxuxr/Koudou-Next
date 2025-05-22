package jp.ac.tsukuba.eclab.koudounext.core.engine.test;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.ModuleManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AgentsUI extends JFrame {
    private final AgentCanvas canvas;
    private static volatile AgentsUI instance = null;
    public AgentsUI() {
        super("Agent Viewer");

        canvas = new AgentCanvas();
        canvas.setPreferredSize(new Dimension(500, 500));

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().add(canvas);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void setAgents(List<Point> agents) {
        canvas.setAgents(agents);
        canvas.repaint();
    }

    private static class AgentCanvas extends JPanel {
        private List<Point> agents = List.of();

        public void setAgents(List<Point> agents) {
            this.agents = agents;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.RED);
            for (Point p : agents) {
                g.fillOval(p.x, p.y, 4, 4);
            }
        }
    }
    public static AgentsUI getInstance() {
        if (instance == null) {
            synchronized (ModuleManager.class) {
                if (instance == null) {
                    instance = new AgentsUI();
                }
            }
        }
        return instance;
    }
}

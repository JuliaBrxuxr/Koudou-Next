package jp.ac.tsukuba.eclab.koudounext.core.engine.test;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent.AgentObject;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraph;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AgentsUI extends JFrame {
    private final MapAndAgentCanvas mapAndAgentCanvas;
    private static volatile AgentsUI instance = null;
    private List<AgentObject> currentListOfAllAgents = new ArrayList<>();

    private AgentsUI(RoadGraph graph) {
        super("Agent Based Model Viewer");
        if (graph == null) {
            throw new IllegalArgumentException("RoadGraph cannot be null for AgentsUI.");
        }
        mapAndAgentCanvas = new MapAndAgentCanvas(graph);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton zoomInButton = new JButton("Zoom In (+)");
        zoomInButton.addActionListener(e -> mapAndAgentCanvas.zoomIn());
        JButton zoomOutButton = new JButton("Zoom Out (-)");
        zoomOutButton.addActionListener(e -> mapAndAgentCanvas.zoomOut());
        JButton resetButton = new JButton("Reset View / Deselect");
        resetButton.addActionListener(e -> {
            mapAndAgentCanvas.resetViewAndSelection();
            // mapAndAgentCanvas.updateAgentObjects(this.currentListOfAllAgents);
        });

        controlPanel.add(zoomInButton);
        controlPanel.add(zoomOutButton);
        controlPanel.add(resetButton);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.add(controlPanel, BorderLayout.NORTH);
        this.add(mapAndAgentCanvas, BorderLayout.CENTER);
        this.setMinimumSize(new Dimension(800, 600));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(true);
    }

    public void updateAllAgentData(List<AgentObject> agentObjects) {
        this.currentListOfAllAgents = (agentObjects != null) ? new ArrayList<>(agentObjects) : new ArrayList<>();
        SwingUtilities.invokeLater(() -> {
            mapAndAgentCanvas.updateAgentObjects(this.currentListOfAllAgents);
        });
    }

    public void displayGlobalPath(List<Integer> pathNodeIndices) {
        SwingUtilities.invokeLater(() -> {
            mapAndAgentCanvas.setGlobalPath(pathNodeIndices);
        });
    }

    public static AgentsUI getInstance(RoadGraph graph) {
        if (instance == null) {
            synchronized (AgentsUI.class) {
                if (instance == null) {
                    instance = new AgentsUI(graph);
                    SwingUtilities.invokeLater(() -> instance.setVisible(true));
                }
            }
        }
        return instance;
    }

    public static AgentsUI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AgentsUI instance has not been initialized. Call getInstance(RoadGraph graph) first.");
        }
        return instance;
    }
}
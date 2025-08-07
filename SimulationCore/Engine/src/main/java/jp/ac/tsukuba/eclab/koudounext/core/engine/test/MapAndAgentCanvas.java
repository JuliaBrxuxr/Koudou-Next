package jp.ac.tsukuba.eclab.koudounext.core.engine.test;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent.AgentObject;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraph;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Coordinate;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Edge;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.HaversineUtil;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapAndAgentCanvas extends JPanel {
    private final RoadGraph graph;

    private List<Point2D.Double> buildingCentroids;
    private List<Integer> currentPathToDisplay;
    private List<AgentObject> allAgentsList;
    private AgentObject selectedAgent = null;
    private double minLat, maxLat, minLon, maxLon;
    private double latRange, lonRange;
    private double initialViewCenterXGeo, initialViewCenterYGeo;
    private double scaleFactor = 1.0;
    private double viewTranslateX = 0;
    private double viewTranslateY = 0;
    private Point lastDragPoint;
    private final Color AGENT_DEFAULT_FILL_COLOR = Color.YELLOW;
    private final Color AGENT_SELECTED_FILL_COLOR = new Color(255, 140, 0);
    private final Color AGENT_BORDER_COLOR = Color.BLACK;
    private final int AGENT_DIAMETER = 6;
    private final int AGENT_ALPHA_OPAQUE = 255;
    private final int AGENT_ALPHA_TRANSLUCENT = (int) (255 * 0.2);
    private static final int AGENT_CLICK_RADIUS = 6;
    private static final int AGENT_CLICK_RADIUS_SQ = AGENT_CLICK_RADIUS * AGENT_CLICK_RADIUS;
    private static final int SCALE_BAR_WIDTH_PX = 100;
    private static final int SCALE_BAR_HEIGHT_PX = 10;
    private static final int SCALE_BAR_MARGIN_PX = 20;

    public MapAndAgentCanvas(RoadGraph graph) {
        this.graph = graph;
        this.currentPathToDisplay = new ArrayList<>();
        this.buildingCentroids = graph.getBuildings();
        this.allAgentsList = new ArrayList<>();
        computeMapBoundsAndInitialViewCenter();
        addMouseListenersAndHandler();
        this.setPreferredSize(new Dimension(1000, 800));
        this.setBackground(new Color(50, 50, 50));
    }

    private void computeMapBoundsAndInitialViewCenter() {
        minLat = Double.MAX_VALUE;
        maxLat = -Double.MAX_VALUE;
        minLon = Double.MAX_VALUE;
        maxLon = -Double.MAX_VALUE;

        if (graph == null || graph.getAllNodes() == null || graph.getAllNodes().isEmpty()) {
            minLat = 35.9;
            maxLat = 36.2;
            minLon = 140.0;
            maxLon = 140.2;
        } else {
            for (Node node : graph.getAllNodes()) {
                if (node == null || node.getCoordinate() == null) continue;
                minLat = Math.min(minLat, node.getCoordinate().getLatitude());
                maxLat = Math.max(maxLat, node.getCoordinate().getLatitude());
                minLon = Math.min(minLon, node.getCoordinate().getLongitude());
                maxLon = Math.max(maxLon, node.getCoordinate().getLongitude());
            }
        }
        if (minLat == Double.MAX_VALUE) {
            minLat = 35.9;
            maxLat = 36.2;
            minLon = 140.0;
            maxLon = 140.2;
        }
        if (Math.abs(maxLat - minLat) < 1e-6) {
            minLat -= 0.01;
            maxLat += 0.01;
        }
        if (Math.abs(maxLon - minLon) < 1e-6) {
            minLon -= 0.01;
            maxLon += 0.01;
        }

        latRange = maxLat - minLat;
        lonRange = maxLon - minLon;
        initialViewCenterXGeo = (minLon + maxLon) / 2;
        initialViewCenterYGeo = (minLat + maxLat) / 2;
    }

    private void addMouseListenersAndHandler() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastDragPoint = e.getPoint();
                boolean agentWasClicked = handleAgentClick(e.getPoint());
                if (!agentWasClicked && selectedAgent != null) {
                    // System.out.println("Clicked on empty space, deselecting agent.");
                    resetAgentSelectionAndPathDisplay();
                    // repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastDragPoint = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastDragPoint == null) return;
                Point currentPoint = e.getPoint();
                viewTranslateX += (currentPoint.x - lastDragPoint.x);
                viewTranslateY += (currentPoint.y - lastDragPoint.y);
                lastDragPoint = currentPoint;
                repaint();
            }
        };
        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);

        this.addMouseWheelListener((MouseWheelEvent e) -> {
            int notches = e.getWheelRotation();
            Point mousePoint = e.getPoint();
            Point2D.Double geoMousePoint = screenToGeo(mousePoint, scaleFactor, viewTranslateX, viewTranslateY);

            if (notches < 0) {
                scaleFactor *= 1.1;
            } else {
                scaleFactor /= 1.1;
            }
            scaleFactor = Math.max(0.01, Math.min(scaleFactor, 200.0));

            Point2D.Double newScreenPosOfGeoMouse = geoToScreen(geoMousePoint.getX(), geoMousePoint.getY(), getWidth(), getHeight(), scaleFactor, viewTranslateX, viewTranslateY);
            viewTranslateX += mousePoint.x - newScreenPosOfGeoMouse.x;
            viewTranslateY += mousePoint.y - newScreenPosOfGeoMouse.y;
            repaint();
        });
    }

    private boolean handleAgentClick(Point clickPoint) {
        if (this.allAgentsList == null) return false;

        AgentObject newlyClickedAgent = null;
        double minDistanceSqToClick = AGENT_CLICK_RADIUS_SQ;

        for (AgentObject agent : this.allAgentsList) {
            Object latObj = agent.getAttribute("latitude");
            Object lonObj = agent.getAttribute("longitude");

            if (latObj instanceof Double && lonObj instanceof Double) {
                double agentLat = (Double) latObj;
                double agentLon = (Double) lonObj;
                Point2D agentScreenPos = geoToScreen(agentLon, agentLat, getWidth(), getHeight(), scaleFactor, viewTranslateX, viewTranslateY);

                double distSq = clickPoint.distanceSq(agentScreenPos.getX(), agentScreenPos.getY());
                if (distSq < minDistanceSqToClick) {
                    newlyClickedAgent = agent;
                    minDistanceSqToClick = distSq;
                }
            }
        }

        if (newlyClickedAgent != null) {
            if (this.selectedAgent == newlyClickedAgent) {
                resetAgentSelectionAndPathDisplay();
            } else {
                this.selectedAgent = newlyClickedAgent;
                updatePathForSelectedAgent();
            }
            repaint();
            return true;
        } else {
            if (selectedAgent != null) {
                resetAgentSelectionAndPathDisplay();
                repaint();
            }
        }
        return false;
    }

    private void updatePathForSelectedAgent() {
        if (this.selectedAgent != null) {
            Object pathObj = this.selectedAgent.getAttribute("calculatedPath");
            if (pathObj instanceof List) {
                try {
                    List<?> rawPathList = (List<?>) pathObj;
                    List<Integer> pathIndices = new ArrayList<>();
                    boolean allIntegers = true;
                    for (Object item : rawPathList) {
                        if (item instanceof Integer) {
                            pathIndices.add((Integer) item);
                        } else if (item instanceof Number) {
                            pathIndices.add(((Number) item).intValue());
                        } else {
                            allIntegers = false;
                            System.err.println("Warning: Non-integer found in agent's calculatedPath: " + item + " for agent " + selectedAgent.getUUID());
                            break;
                        }
                    }
                    if (allIntegers) {
                        this.currentPathToDisplay = pathIndices;
                        // System.out.println("Displaying path for selected agent " + selectedAgent.getUUID() + " with " + pathIndices.size() + " nodes.");
                    } else {
                        this.currentPathToDisplay = new ArrayList<>();
                    }
                } catch (ClassCastException e) {
                    System.err.println("Error casting agent's calculatedPath attribute for agent " + selectedAgent.getUUID() + ": " + e.getMessage());
                    this.currentPathToDisplay = new ArrayList<>();
                }
            } else {
                this.currentPathToDisplay = new ArrayList<>();
                if (pathObj != null) {
                    // System.out.println("Agent " + selectedAgent.getUUID() + " selected, 'calculatedPath' attribute is not a List: " + pathObj.getClass().getName());
                } else {
                    // System.out.println("Agent " + selectedAgent.getUUID() + " selected, but no 'calculatedPath' attribute found or path is empty.");
                }
            }
        } else {
            this.currentPathToDisplay = new ArrayList<>();
        }
        // repaint()
    }


    public void updateAgentObjects(List<AgentObject> agents) {
        this.allAgentsList = (agents != null) ? new ArrayList<>(agents) : new ArrayList<>();
        if (selectedAgent != null) {
            boolean stillSelectedAgentExists = false;
            for (AgentObject agent : this.allAgentsList) {
                if (agent.getUUID().equals(selectedAgent.getUUID())) {
                    selectedAgent = agent;
                    stillSelectedAgentExists = true;
                    break;
                }
            }
            if (!stillSelectedAgentExists) {
                resetAgentSelectionAndPathDisplay();
            } else {
                updatePathForSelectedAgent();
            }
        }
        repaint();
    }

    public void setGlobalPath(List<Integer> pathNodeIndices) {
        resetAgentSelectionAndPathDisplay();
        this.currentPathToDisplay = (pathNodeIndices != null) ? new ArrayList<>(pathNodeIndices) : new ArrayList<>();
        repaint();
    }

    private void resetAgentSelectionAndPathDisplay() {
        this.selectedAgent = null;
        this.currentPathToDisplay = new ArrayList<>();
    }

    public void zoomIn() {
        Point centerPanel = new Point(getWidth() / 2, getHeight() / 2);
        Point2D.Double geoCenter = screenToGeo(centerPanel, scaleFactor, viewTranslateX, viewTranslateY);
        scaleFactor *= 1.2;
        scaleFactor = Math.min(scaleFactor, 200.0);
        Point2D.Double newScreenPosOfGeoCenter = geoToScreen(geoCenter.getX(), geoCenter.getY(), getWidth(), getHeight(), scaleFactor, viewTranslateX, viewTranslateY);
        viewTranslateX += centerPanel.x - newScreenPosOfGeoCenter.x;
        viewTranslateY += centerPanel.y - newScreenPosOfGeoCenter.y;
        repaint();
    }

    public void zoomOut() {
        Point centerPanel = new Point(getWidth() / 2, getHeight() / 2);
        Point2D.Double geoCenter = screenToGeo(centerPanel, scaleFactor, viewTranslateX, viewTranslateY);
        scaleFactor /= 1.2;
        scaleFactor = Math.max(0.01, scaleFactor);
        Point2D.Double newScreenPosOfGeoCenter = geoToScreen(geoCenter.getX(), geoCenter.getY(), getWidth(), getHeight(), scaleFactor, viewTranslateX, viewTranslateY);
        viewTranslateX += centerPanel.x - newScreenPosOfGeoCenter.x;
        viewTranslateY += centerPanel.y - newScreenPosOfGeoCenter.y;
        repaint();
    }

    public void resetViewAndSelection() {
        scaleFactor = 1.0;
        viewTranslateX = 0;
        viewTranslateY = 0;
        resetAgentSelectionAndPathDisplay();
        repaint();
    }

    private Point2D.Double geoToScreen(double lon, double lat, int panelWidth, int panelHeight,
                                       double currentScaleFactor, double currentTranslateX, double currentTranslateY) {
        if (lonRange == 0 || latRange == 0 || panelWidth == 0 || panelHeight == 0) {
            return new Point2D.Double(panelWidth / 2.0, panelHeight / 2.0);
        }
        double baseScaleX = panelWidth / lonRange;
        double baseScaleY = panelHeight / latRange;
        double effectiveBaseScale = Math.min(baseScaleX, baseScaleY);
        double finalScale = effectiveBaseScale * currentScaleFactor;
        double screenX = (lon - initialViewCenterXGeo) * finalScale + panelWidth / 2.0 + currentTranslateX;
        double screenY = (initialViewCenterYGeo - lat) * finalScale + panelHeight / 2.0 + currentTranslateY;
        return new Point2D.Double(screenX, screenY);
    }

    private Point2D.Double screenToGeo(Point screenPoint, double currentScaleFactor,
                                       double currentTranslateX, double currentTranslateY) {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        if (lonRange == 0 || latRange == 0 || panelWidth == 0 || panelHeight == 0) {
            return new Point2D.Double(initialViewCenterXGeo, initialViewCenterYGeo);
        }
        double baseScaleX = panelWidth / lonRange;
        double baseScaleY = panelHeight / latRange;
        double effectiveBaseScale = Math.min(baseScaleX, baseScaleY);
        double finalScale = effectiveBaseScale * currentScaleFactor;
        if (Math.abs(finalScale) < 1e-9) {
            return new Point2D.Double(initialViewCenterXGeo, initialViewCenterYGeo);
        }
        double mapCenterX = panelWidth / 2.0 + currentTranslateX;
        double mapCenterY = panelHeight / 2.0 + currentTranslateY;
        double scaledX = screenPoint.x - mapCenterX;
        double scaledY = screenPoint.y - mapCenterY;
        double lonOffset = scaledX / finalScale;
        double latOffset = scaledY / finalScale;
        double lon = initialViewCenterXGeo + lonOffset;
        double lat = initialViewCenterYGeo - latOffset;
        return new Point2D.Double(lon, lat);
    }



    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(0, 0, width, height);

        if (graph == null) return;

        g2d.setColor(new Color(120, 120, 120));
        g2d.setStroke(new BasicStroke(1.0f));
        if (graph.getAllNodes() != null) {
            for (int i = 0; i < graph.getNumNodes(); i++) {
                Node fromNode = graph.getNodeByIndex(i);
                if (fromNode == null || fromNode.getCoordinate() == null) continue;
                if (graph.getOutgoingEdges(i) != null) {
                    for (Edge edge : graph.getOutgoingEdges(i)) {
                        Node toNode = graph.getNodeByIndex(edge.getHeadNode());
                        if (toNode == null || toNode.getCoordinate() == null) continue;
                        Point2D fromP = geoToScreen(fromNode.getCoordinate().getLongitude(), fromNode.getCoordinate().getLatitude(), width, height, scaleFactor, viewTranslateX, viewTranslateY);
                        Point2D toP = geoToScreen(toNode.getCoordinate().getLongitude(), toNode.getCoordinate().getLatitude(), width, height, scaleFactor, viewTranslateX, viewTranslateY);
                        g2d.draw(new Line2D.Double(fromP, toP));
                    }
                }
            }
        }

        // display of buildings
        if (buildingCentroids != null) {
            g2d.setColor(Color.BLUE);
            for (Point2D.Double building : buildingCentroids) {
                Point2D.Double buildingPoint = geoToScreen(
                        building.getX(), building.getY(),
                        getWidth(), getHeight(),
                        scaleFactor,
                        viewTranslateX,
                        viewTranslateY
                );
                g2d.fillOval((int) buildingPoint.getX() - 2, (int) buildingPoint.getY() - 2, 5, 5);
            }
        }

        if (currentPathToDisplay != null && currentPathToDisplay.size() > 1) {
            g2d.setColor(new Color(255, 0, 0, 200));
            g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < currentPathToDisplay.size() - 1; i++) {
                Node fromNode = graph.getNodeByIndex(currentPathToDisplay.get(i));
                Node toNode = graph.getNodeByIndex(currentPathToDisplay.get(i + 1));
                if (fromNode == null || toNode == null || fromNode.getCoordinate() == null || toNode.getCoordinate() == null)
                    continue;
                Point2D fromP = geoToScreen(fromNode.getCoordinate().getLongitude(), fromNode.getCoordinate().getLatitude(), width, height, scaleFactor, viewTranslateX, viewTranslateY);
                Point2D toP = geoToScreen(toNode.getCoordinate().getLongitude(), toNode.getCoordinate().getLatitude(), width, height, scaleFactor, viewTranslateX, viewTranslateY);
                g2d.draw(new Line2D.Double(fromP, toP));
            }
        }

        if (allAgentsList != null) {
            for (AgentObject agent : allAgentsList) {
                if (agent == null) continue;
                Object latObj = agent.getAttribute("latitude");
                Object lonObj = agent.getAttribute("longitude");

                if (latObj instanceof Double && lonObj instanceof Double) {
                    double agentLat = (Double) latObj;
                    double agentLon = (Double) lonObj;
                    Point2D agentScreenPos = geoToScreen(agentLon, agentLat, width, height, scaleFactor, viewTranslateX, viewTranslateY);

                    int currentAlpha;
                    Color currentFillColor;

                    if (agent == selectedAgent) {
                        currentFillColor = AGENT_SELECTED_FILL_COLOR;
                        currentAlpha = AGENT_ALPHA_OPAQUE;
                    } else {
                        currentFillColor = AGENT_DEFAULT_FILL_COLOR;
                        currentAlpha = (selectedAgent != null) ? AGENT_ALPHA_TRANSLUCENT : AGENT_ALPHA_OPAQUE;
                    }

                    g2d.setColor(new Color(currentFillColor.getRed(), currentFillColor.getGreen(), currentFillColor.getBlue(), currentAlpha));
                    g2d.fillOval((int) agentScreenPos.getX() - AGENT_DIAMETER / 2,
                            (int) agentScreenPos.getY() - AGENT_DIAMETER / 2,
                            AGENT_DIAMETER, AGENT_DIAMETER);

                    g2d.setColor(new Color(AGENT_BORDER_COLOR.getRed(), AGENT_BORDER_COLOR.getGreen(), AGENT_BORDER_COLOR.getBlue(), currentAlpha));
                    g2d.drawOval((int) agentScreenPos.getX() - AGENT_DIAMETER / 2,
                            (int) agentScreenPos.getY() - AGENT_DIAMETER / 2,
                            AGENT_DIAMETER, AGENT_DIAMETER);
                }
            }
        }
        drawOverlayInfo(g2d, width, height);
    }

    private void drawOverlayInfo(Graphics2D g2d, int panelWidth, int panelHeight) {
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(Color.LIGHT_GRAY);
        int xPosText = 15;
        int yPosText = 20;
        final int lineHeight = 15;

        if (this.minLon != Double.MAX_VALUE) {
            String boundsText = String.format("Bounds: (Lon: %.4f, Lat: %.4f) to (Lon: %.4f, Lat: %.4f)",
                    this.minLon, this.minLat, this.maxLon, this.maxLat);
            g2d.drawString(boundsText, xPosText, yPosText);
            yPosText += lineHeight;
        }
        String zoomText = String.format("Zoom: %.2fx", scaleFactor);
        g2d.drawString(zoomText, xPosText, yPosText);
        yPosText += lineHeight;
        if (graph != null) {
            g2d.drawString("Nodes: " + graph.getNumNodes(), xPosText, yPosText);
            yPosText += lineHeight;
            g2d.drawString("Edges: " + graph.getNumEdges(), xPosText, yPosText);
            yPosText += lineHeight;
        }
        if (selectedAgent != null) {
            g2d.setColor(AGENT_SELECTED_FILL_COLOR);
            g2d.drawString("Selected Agent: " + selectedAgent.getUUID().substring(0, Math.min(8, selectedAgent.getUUID().length())) + "...", xPosText, yPosText);
            g2d.setColor(Color.LIGHT_GRAY);
        }
        drawScaleBar(g2d, panelWidth, panelHeight);
    }

    private void drawScaleBar(Graphics2D g2d, int panelWidth, int panelHeight) {
        double targetScreenBarWidth = SCALE_BAR_WIDTH_PX;
        int barY = panelHeight - SCALE_BAR_MARGIN_PX - SCALE_BAR_HEIGHT_PX;

        Point screenBarP1 = new Point((int) (panelWidth - targetScreenBarWidth - SCALE_BAR_MARGIN_PX), barY + SCALE_BAR_HEIGHT_PX / 2);
        Point screenBarP2 = new Point((int) (panelWidth - SCALE_BAR_MARGIN_PX), barY + SCALE_BAR_HEIGHT_PX / 2);

        Point2D.Double geoP1 = screenToGeo(screenBarP1, scaleFactor, viewTranslateX, viewTranslateY);
        Point2D.Double geoP2 = screenToGeo(screenBarP2, scaleFactor, viewTranslateX, viewTranslateY);

        double actualDistanceMeters = 0;
        if (geoP1 != null && geoP2 != null &&
                Math.abs(geoP1.getX() - initialViewCenterXGeo) > 1e-9 && Math.abs(geoP1.getY() - initialViewCenterYGeo) > 1e-9 &&
                Math.abs(geoP2.getX() - initialViewCenterXGeo) > 1e-9 && Math.abs(geoP2.getY() - initialViewCenterYGeo) > 1e-9) {
            actualDistanceMeters = HaversineUtil.calculateDistance(geoP1.getY(), geoP1.getX(), geoP2.getY(), geoP2.getX());
        }

        if (Double.isNaN(actualDistanceMeters) || Double.isInfinite(actualDistanceMeters) || actualDistanceMeters < 1e-3) {
            if (graph != null && graph.getNumNodes() > 0) {
                actualDistanceMeters = (lonRange / panelWidth) * targetScreenBarWidth * 111000 * Math.cos(Math.toRadians(initialViewCenterYGeo));
                if (actualDistanceMeters < 1e-3) {
                    return;
                }
            } else {
                return;
            }
        }

        double[] niceNumSteps = {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 25000, 50000, 100000};
        double niceDistanceMeters = 0;
        for (double d : niceNumSteps) {
            if (d > actualDistanceMeters / 3.0) {
                niceDistanceMeters = d;
                break;
            }
        }
        if (niceDistanceMeters == 0) {
            double order = Math.pow(10, Math.floor(Math.log10(actualDistanceMeters)));
            niceDistanceMeters = Math.round(actualDistanceMeters / order) * order;
            if (niceDistanceMeters > actualDistanceMeters * 1.5 && order > 1)
                niceDistanceMeters = Math.round(actualDistanceMeters / (order / 10.0)) * (order / 10.0);
            if (niceDistanceMeters == 0 && actualDistanceMeters > 0)
                niceDistanceMeters = actualDistanceMeters;
        }
        if (niceDistanceMeters == 0) return;


        double screenWidthForNiceDistance = (niceDistanceMeters / actualDistanceMeters) * targetScreenBarWidth;
        if (Double.isNaN(screenWidthForNiceDistance) || Double.isInfinite(screenWidthForNiceDistance) || screenWidthForNiceDistance <= 0 || screenWidthForNiceDistance > panelWidth * 0.8) {
            // screenWidthForNiceDistance = targetScreenBarWidth;
            return;
        }


        int barScreenX = panelWidth - (int) screenWidthForNiceDistance - SCALE_BAR_MARGIN_PX;

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(barScreenX, barY, barScreenX + (int) screenWidthForNiceDistance, barY);
        g2d.drawLine(barScreenX, barY - 2, barScreenX, barY + 2);
        g2d.drawLine(barScreenX + (int) screenWidthForNiceDistance, barY - 2, barScreenX + (int) screenWidthForNiceDistance, barY + 2);

        String unit = "m";
        double displayVal = niceDistanceMeters;
        if (niceDistanceMeters >= 1000) {
            displayVal = niceDistanceMeters / 1000.0;
            unit = "km";
        }

        String textToDraw = String.format(displayVal == (int) displayVal ? "%.0f %s" : "%.1f %s", displayVal, unit);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(textToDraw);
        g2d.drawString(textToDraw, barScreenX + ((int) screenWidthForNiceDistance - textWidth) / 2, barY - 5);
    }
}
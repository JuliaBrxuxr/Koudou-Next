package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.test;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraph;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Edge;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class RoadNetworkViewer extends JPanel {
    private final RoadGraph graph;
    private final List<Integer> path;
    private double minLat, maxLat, minLon, maxLon;
    private double latRange, lonRange;
    private double aspectRatio;
    private double centerLon, centerLat;

    private double scaleFactor = 1.0;
    private double translateX = 0;
    private double translateY = 0;
    private Point lastDragPoint;

    private static final int SCALE_BAR_WIDTH = 100;
    private static final int SCALE_BAR_HEIGHT = 20;
    private static final int SCALE_BAR_MARGIN = 10;

    private List<Point2D.Double> buildingCentroids;

    public RoadNetworkViewer(RoadGraph graph, List<Integer> path, List<Point2D.Double> buildingCentroids, String viewTitle) {
        JFrame frame = new JFrame(viewTitle);
        this.graph = graph;
        this.path = path != null ? path : new ArrayList<>();
        this.buildingCentroids = buildingCentroids;
        computeMapBounds();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        addMouseListeners();
        addControlButtons(frame);
        frame.add(this);
        frame.setVisible(true);
    }

    private void addMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastDragPoint = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point currentPoint = e.getPoint();
                int dx = currentPoint.x - lastDragPoint.x;
                int dy = currentPoint.y - lastDragPoint.y;
                translateX += dx / scaleFactor;
                translateY += dy / scaleFactor;
                lastDragPoint = currentPoint;
                repaint();
            }
        });
    }

    private void addControlButtons(JFrame frame) {
        JPanel controlPanel = new JPanel();

        JButton zoomInButton = new JButton("+");
        zoomInButton.addActionListener(e -> {
            scaleFactor *= 1.2;
            scaleFactor = Math.min(scaleFactor, 10);
            repaint();
        });

        JButton zoomOutButton = new JButton("-");
        zoomOutButton.addActionListener(e -> {
            scaleFactor *= 0.8;
            scaleFactor = Math.max(scaleFactor, 0.1);
            repaint();
        });

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            scaleFactor = 1.0;
            translateX = 0;
            translateY = 0;
            repaint();
        });

        controlPanel.add(zoomInButton);
        controlPanel.add(zoomOutButton);
        controlPanel.add(resetButton);

        frame.add(controlPanel, BorderLayout.NORTH);
    }

    private void computeMapBounds() {
        minLat = Double.MAX_VALUE;
        maxLat = -Double.MAX_VALUE;
        minLon = Double.MAX_VALUE;
        maxLon = -Double.MAX_VALUE;

        for (Node node : graph.getAllNodes()) {
            if (node == null) continue;
            minLat = Math.min(minLat, node.getCoordinate().getLatitude());
            maxLat = Math.max(maxLat, node.getCoordinate().getLatitude());
            minLon = Math.min(minLon, node.getCoordinate().getLongitude());
            maxLon = Math.max(maxLon, node.getCoordinate().getLongitude());
        }

        if (minLat == maxLat) {
            minLat -= 0.001;
            maxLat += 0.001;
        }
        if (minLon == maxLon) {
            minLon -= 0.001;
            maxLon += 0.001;
        }

        latRange = maxLat - minLat;
        lonRange = maxLon - minLon;
        aspectRatio = lonRange / latRange;
        centerLon = (minLon + maxLon) / 2;
        centerLat = (minLat + maxLat) / 2;
    }

    private Point2D geoToScreen(double lon, double lat, int width, int height) {
        double mapAspectRatio = (double) width / height;
        double scale;
        int offsetX = 0, offsetY = 0;

        if (aspectRatio > mapAspectRatio) {
            scale = width / lonRange;
            double mapHeight = width / aspectRatio;
            offsetY = (int) ((height - mapHeight) / 2);
        } else {
            scale = height / latRange;
            double mapWidth = height * aspectRatio;
            offsetX = (int) ((width - mapWidth) / 2);
        }
        scale *= scaleFactor;
        double x = offsetX + (lon - minLon) * scale + translateX;
        double y = offsetY + height - (lat - minLat) * scale + translateY;
        return new Point2D.Double(x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(0.3f));
        for (int i = 0; i < graph.getNumNodes(); i++) {
            Node fromNode = graph.getNodeByIndex(i);
            if (fromNode == null) continue;
            for (Edge edge : graph.getOutgoingEdges(i)) {
                Node toNode = graph.getNodeByIndex(edge.getHeadNode());
                if (toNode == null) continue;
                Point2D fromPoint = geoToScreen(fromNode.getCoordinate().getLongitude(), fromNode.getCoordinate().getLatitude(), width, height);
                Point2D toPoint = geoToScreen(toNode.getCoordinate().getLongitude(), toNode.getCoordinate().getLatitude(), width, height);
                if (fromPoint.distance(toPoint) > 0.1) {
                    g2d.draw(new Line2D.Double(fromPoint, toPoint));
                }
            }
        }
         if (buildingCentroids != null) {
    g2d.setColor(Color.BLUE);
    for (Point2D.Double building : buildingCentroids) {
        Point2D buildingPoint = geoToScreen(building.getX(), building.getY(), width, height);
        g2d.fillOval((int) buildingPoint.getX() - 2, (int) buildingPoint.getY() - 2, 5, 5);
    }
} 

        if (path.size() > 1) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2f));
            for (int i = 0; i < path.size() - 1; i++) {
                Node fromNode = graph.getNodeByIndex(path.get(i));
                Node toNode = graph.getNodeByIndex(path.get(i + 1));
                if (fromNode == null || toNode == null) continue;
                Point2D fromPoint = geoToScreen(fromNode.getCoordinate().getLongitude(), fromNode.getCoordinate().getLatitude(), width, height);
                Point2D toPoint = geoToScreen(toNode.getCoordinate().getLongitude(), toNode.getCoordinate().getLatitude(), width, height);
                if (fromPoint.distance(toPoint) > 0.1) {
                    g2d.draw(new Line2D.Double(fromPoint, toPoint));
                }
            }
        } 

        g2d.setColor(Color.BLACK);
        g2d.drawString("Nodes: " + graph.getNumNodes(), 10, 20);
        g2d.drawString("Edges: " + graph.getNumEdges(), 10, 40);
        g2d.drawString(String.format("Bounds: (%.5f, %.5f) to (%.5f, %.5f)",
                minLon, minLat, maxLon, maxLat), 10, 60);
        g2d.drawString(String.format("Zoom: %.1fx", scaleFactor), 10, 80);
        drawScaleBar(g2d, width, height);
    }

    private void drawScaleBar(Graphics2D g2d, int width, int height) {
        int x = width - SCALE_BAR_WIDTH - SCALE_BAR_MARGIN;
        int y = height - SCALE_BAR_HEIGHT - SCALE_BAR_MARGIN;
        double pixelDistance = SCALE_BAR_WIDTH / scaleFactor;
        double actualDistance = calculateActualDistance(pixelDistance);
        String unit = "m";
        int displayDistance = (int) actualDistance;
        if (actualDistance >= 1000) {
            displayDistance = (int) (actualDistance / 1000);
            unit = "km";
        }
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRect(x, y, SCALE_BAR_WIDTH, SCALE_BAR_HEIGHT);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, SCALE_BAR_WIDTH, SCALE_BAR_HEIGHT);
        g2d.drawLine(x, y + SCALE_BAR_HEIGHT, x, y);
        g2d.drawLine(x + SCALE_BAR_WIDTH / 2, y + SCALE_BAR_HEIGHT,
                x + SCALE_BAR_WIDTH / 2, y + SCALE_BAR_HEIGHT / 2);
        g2d.drawLine(x + SCALE_BAR_WIDTH, y + SCALE_BAR_HEIGHT,
                x + SCALE_BAR_WIDTH, y);
        String scaleText = displayDistance + " " + unit;
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(scaleText);
        int textX = x + (SCALE_BAR_WIDTH - textWidth) / 2;
        int textY = y + SCALE_BAR_HEIGHT - 5;
        g2d.drawString(scaleText, textX, textY);
    }

    private double calculateActualDistance(double pixelDistance) {
        double kmPerDegree = 111.0;
        double kmPerPixel = (lonRange * kmPerDegree * Math.cos(Math.toRadians(centerLat))) / getWidth();
        return pixelDistance * kmPerPixel * 1000;
    }
}
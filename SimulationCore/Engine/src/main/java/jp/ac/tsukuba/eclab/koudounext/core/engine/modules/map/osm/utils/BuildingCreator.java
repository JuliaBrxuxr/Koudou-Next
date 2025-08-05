package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.InputStream;
import java.awt.geom.Point2D;
import java.util.*;


public class BuildingCreator {

   
    private final Map<String, Point2D.Double> nodeMap = new HashMap<>();
    private final List<Point2D.Double> buildingCentroids = new ArrayList<>();

    public List<Point2D.Double> parse(InputStream osmInputStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(osmInputStream);
            doc.getDocumentElement().normalize();

        
            NodeList nodeList = doc.getElementsByTagName("node");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element nodeElem = (Element) nodeList.item(i);
                String id = nodeElem.getAttribute("id");
                double lat = Double.parseDouble(nodeElem.getAttribute("lat"));
                double lon = Double.parseDouble(nodeElem.getAttribute("lon"));
                nodeMap.put(id, new Point2D.Double(lon, lat));
            }

        
            NodeList wayList = doc.getElementsByTagName("way");
            for (int i = 0; i < wayList.getLength(); i++) {
                Element wayElem = (Element) wayList.item(i);
                NodeList childNodes = wayElem.getChildNodes();

                List<Point2D.Double> buildingNodes = new ArrayList<>();
                boolean isBuilding = false;

                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node child = childNodes.item(j);
                    if (child.getNodeType() != Node.ELEMENT_NODE) continue;

                    Element el = (Element) child;

                    if (el.getTagName().equals("nd")) {
                        String ref = el.getAttribute("ref");
                        Point2D.Double pt = nodeMap.get(ref);
                        if (pt != null) {
                            buildingNodes.add(pt);
                        }
                    }

                    if (el.getTagName().equals("tag")) {
                        String k = el.getAttribute("k");
                        if ("building".equals(k)) {
                            isBuilding = true;
                        }
                    }
                }

                if (isBuilding && buildingNodes.size() > 0) {
                    Point2D.Double centroid = computeCentroid(buildingNodes);
                    if (centroid != null) {
                        buildingCentroids.add(centroid);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error parsing OSM: " + e.getMessage());
            e.printStackTrace();
        }

        return buildingCentroids;
    }

    private Point2D.Double computeCentroid(List<Point2D.Double> points) {
        double x = 0, y = 0;
        int count = 0;

        for (Point2D.Double pt : points) {
            x += pt.getX();
            y += pt.getY();
            count++;
        }

        if (count == 0) return null;
        return new Point2D.Double(x / count, y / count);
    }



}

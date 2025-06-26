package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Node;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.HaversineUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoadGraphBuilder {
    private File osmFile;
    private InputStream osmInputStream;
    private String regionName = "default";
    private Map<String, Integer> speeds;
    private RoadGraph roadNetworkUnderConstruction;
    private ArrayList<Long> currentWayNodes;
    private boolean currentWayIsHighway;
    private String currentWayHighwayType;
    private String currentWayOnewayValue;
    private boolean inWayElement;

    public RoadGraphBuilder() {
        this.speeds = new HashMap<>();
        setDefaultSpeeds();
        this.currentWayNodes = new ArrayList<>();
    }

    private void setDefaultSpeeds() {
        speeds.put("motorway", 110);
        speeds.put("trunk", 110);
        speeds.put("primary", 70);
        speeds.put("secondary", 60);
        speeds.put("tertiary", 50);
        speeds.put("motorway_link", 50);
        speeds.put("trunk_link", 50);
        speeds.put("primary_link", 50);
        speeds.put("secondary_link", 50);
        speeds.put("road", 40);
        speeds.put("unclassified", 40);
        speeds.put("residential", 30);
        speeds.put("unsurfaced", 30);
        speeds.put("living_street", 10);
        speeds.put("service", 5);
    }

    public RoadGraphBuilder setOsmFile(File osmFile) {
        this.osmFile = osmFile;
        this.osmInputStream = null;
        return this;
    }

    public RoadGraphBuilder setOsmInputStream(InputStream osmInputStream) {
        this.osmInputStream = osmInputStream;
        this.osmFile = null;
        return this;
    }

    public RoadGraphBuilder setRegionName(String regionName) {
        this.regionName = regionName;
        return this;
    }

    public RoadGraphBuilder setSpeeds(Map<String, Integer> customSpeeds) {
        this.speeds = new HashMap<>(customSpeeds);
        return this;
    }

    public RoadGraph build() throws ParserConfigurationException, SAXException, IOException {
        if (this.osmFile == null && this.osmInputStream == null) {
            throw new IllegalStateException("OSM data source (file or input stream) not set.");
        }

        this.roadNetworkUnderConstruction = new RoadGraph(this.regionName);
        this.roadNetworkUnderConstruction.mOsmIdToNodeIndex = new HashMap<>();
        this.roadNetworkUnderConstruction.mNodes = new ArrayList<>();
        this.roadNetworkUnderConstruction.mOutgoingEdges = new ArrayList<>();
        this.roadNetworkUnderConstruction.mIncomingEdges = new ArrayList<>();
        this.roadNetworkUnderConstruction.mRoadTypes = new ArrayList<>();
        this.roadNetworkUnderConstruction.mNumNodes = 0;
        this.roadNetworkUnderConstruction.mNumEdges = 0;


        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        OSMHandler handler = new OSMHandler(this.roadNetworkUnderConstruction, this.speeds, this.currentWayNodes);

        if (this.osmFile != null) {
            saxParser.parse(this.osmFile, handler);
        } else {
            saxParser.parse(this.osmInputStream, handler);
        }

        return this.roadNetworkUnderConstruction;
    }

    private static class OSMHandler extends DefaultHandler {
        private RoadGraph roadNetwork;
        private Map<String, Integer> speeds;

        private List<Long> wayNodesList;
        private boolean inWayElement;
        private boolean isCurrentWayHighway;
        private String currentWayHighwayType;
        private String currentWayOnewayValue;

        public OSMHandler(RoadGraph roadNetwork, Map<String, Integer> speeds, List<Long> wayNodesListRef) {
            this.roadNetwork = roadNetwork;
            this.speeds = speeds;
            this.wayNodesList = wayNodesListRef;
        }

        @Override
        public void startDocument() throws SAXException {

        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("node".equalsIgnoreCase(qName)) {
                long osmId = Long.parseLong(attributes.getValue("id"));
                double lat = Double.parseDouble(attributes.getValue("lat"));
                double lon = Double.parseDouble(attributes.getValue("lon"));
                roadNetwork.addNodeInternal(osmId, lat, lon);
            } else if ("way".equalsIgnoreCase(qName)) {
                inWayElement = true;
                wayNodesList.clear();
                isCurrentWayHighway = false;
                currentWayHighwayType = null;
                currentWayOnewayValue = "no";
            } else if ("nd".equalsIgnoreCase(qName) && inWayElement) {
                wayNodesList.add(Long.parseLong(attributes.getValue("ref")));
            } else if ("tag".equalsIgnoreCase(qName) && inWayElement) {
                String k = attributes.getValue("k");
                String v = attributes.getValue("v");
                if ("highway".equals(k)) {
                    isCurrentWayHighway = true;
                    currentWayHighwayType = v;
                    if (!roadNetwork.mRoadTypes.contains(v)) {
                        roadNetwork.mRoadTypes.add(v);
                    }
                } else if ("oneway".equals(k)) {
                    currentWayOnewayValue = v;
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("way".equalsIgnoreCase(qName)) {
                if (isCurrentWayHighway && currentWayHighwayType != null && speeds.containsKey(currentWayHighwayType)) {
                    if (wayNodesList.size() >= 2) {
                        int speedKmph = speeds.get(currentWayHighwayType);
                        double speedMps = speedKmph * 1000.0 / 3600.0;

                        Long baseOsmId = wayNodesList.get(0);
                        Integer baseNodeIndex = roadNetwork.mOsmIdToNodeIndex.get(baseOsmId);

                        if (baseNodeIndex != null) {
                            for (int i = 1; i < wayNodesList.size(); i++) {
                                Long headOsmId = wayNodesList.get(i);
                                Integer headNodeIndex = roadNetwork.mOsmIdToNodeIndex.get(headOsmId);

                                if (headNodeIndex != null) {
                                    if (baseNodeIndex.equals(headNodeIndex)) {
                                        continue;
                                    }

                                    Node n1 = roadNetwork.mNodes.get(baseNodeIndex);
                                    Node n2 = roadNetwork.mNodes.get(headNodeIndex);
                                    double length = HaversineUtil.calculateDistance(
                                            n1.getCoordinate().getLatitude(),
                                            n1.getCoordinate().getLongitude(),
                                            n2.getCoordinate().getLatitude(),
                                            n2.getCoordinate().getLongitude()
                                    );
                                    double travelTime = (speedMps > 0.01) ? (length / speedMps) : (length / (5.0 * 1000.0 / 3600.0));

                                    if ("yes".equals(currentWayOnewayValue) || "1".equals(currentWayOnewayValue)) {
                                        roadNetwork.addEdgeInternal(baseNodeIndex, headNodeIndex, length, travelTime);
                                    } else if ("-1".equals(currentWayOnewayValue)) {
                                        roadNetwork.addEdgeInternal(headNodeIndex, baseNodeIndex, length, travelTime);
                                    } else {
                                        roadNetwork.addEdgeInternal(baseNodeIndex, headNodeIndex, length, travelTime);
                                        roadNetwork.addEdgeInternal(headNodeIndex, baseNodeIndex, length, travelTime);
                                    }
                                    baseNodeIndex = headNodeIndex;
                                } else {
                                    // System.err.println("Warning: Head node OSM ID " + headOsmId + " not found in index. Skipping edge segment.");
                                }
                            }
                        } else {
                            // System.err.println("Warning: Base node OSM ID " + baseOsmId + " not found in index. Skipping way.");
                        }
                    }
                }
                inWayElement = false;
            }
        }
    }
}

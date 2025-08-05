package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.IModuleManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraph;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraphBuilder;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Node;
import java.io.File;
import java.util.Objects;

public class MapManagerImpl implements IModuleManager {
    private RoadGraph roadGraph;
    private double minLat, maxLat, minLon, maxLon;

    @Override
    public boolean load() {
        //TODO: here needs to load map dynamically
        String osmFilepath = "/osm/Tx-To-TU.osm";
        String region = "tsukuba";
         try {
            roadGraph = new RoadGraphBuilder()
                    .setOsmInputStream(this.getClass().getResourceAsStream(osmFilepath))
                    .setRegionName(region)
                    .build();
            double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;

/* for (Node node : graph.getAllNodes()) {
    double lat = node.getLatitude();
    double lon = node.getLongitude();

    if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
        minLat = Math.min(minLat, lat);
        maxLat = Math.max(maxLat, lat);
        minLon = Math.min(minLon, lon);
        maxLon = Math.max(maxLon, lon);
    }
}
 */
            System.out.println("Nodes before LCC: " + roadGraph.getAllNodes().size());
            roadGraph.reduceToLargestConnectedComponent();
            System.out.println("Nodes after LCC: " + roadGraph.getAllNodes().size());
        } catch (Exception ex) {
            System.err.println("Error loading map: " + ex.getMessage());
            return false;
        }
        return true; 


        




    }

    

    @Override
    public boolean step() {
        return false;
    }

    @Override
    public boolean preStep() {
        return false;
    }

    @Override
    public boolean conflictStep() {
        return false;
    }

    @Override
    public boolean postStep() {
        return false;
    }

    public RoadGraph getRoadGraph() {
        return roadGraph;
    }
}
 
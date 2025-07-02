package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.IModuleManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraph;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraphBuilder;

import java.io.File;
import java.util.Objects;

public class MapManagerImpl implements IModuleManager {
    private RoadGraph roadGraph;

    @Override
    public boolean load() {
        //TODO: here needs to load map dynamically
        String osmFilepath = "/osm/Tx-To-TU.osm";
        String region = "tsukuba";
         try {
            roadGraph = new RoadGraphBuilder()
                    .setOsmFile(new File(Objects.requireNonNull(this.getClass().getResource(osmFilepath)).getFile()))
                    .setRegionName(region)
                    .build();
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

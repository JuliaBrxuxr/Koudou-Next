package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Node;
import java.util.List;
import java.util.Map;

public class Way {

      private long id;
    private List<Node> nodes;
    private Map<String, String> tags;

    public Way(long id, List<Node> nodes, Map<String, String> tags) {
        this.id = id;
        this.nodes = nodes;
        this.tags = tags;
    }

    public long getId() {
        return id;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public boolean hasTag(String key) {
        return tags != null && tags.containsKey(key);
    }

    public String getTag(String key) {
        return tags != null ? tags.get(key) : null;
    }

    public Map<String, String> getTags() {
        return tags;
    }

}

package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent;


//TODO:This class is just for testing, DELETE IT
public class ActivityMovePoint extends AgentActivity {
    private double mLatitude;
    private double mLongitude;
    private String mStrategy;

    public ActivityMovePoint(double latitude, double longitude) {
        super();
        mLatitude = latitude;
        mLongitude = longitude;

        double flag = Math.random() * 10;
        if (flag >= 0 && flag < 4) {
            mStrategy = "A_STAR";
        } else if (flag >= 4 && flag < 6) {
            mStrategy = "DIJKSTRA";
        } else if (flag >= 6 && flag < 8) {
            mStrategy = "IDA_STAR";
        } else if (flag >= 8 && flag < 9) {
            mStrategy = "DFS";
        } else if (flag >= 9 && flag < 10) {
            mStrategy = "BFS";
        } else {
            mStrategy = "A_STAR";
        }
    }

    @Override
    public Object getAttribute(String name) {
        return switch (name) {
            case "latitude" -> mLatitude;
            case "longitude" ->mLongitude;
            case "pathfinding" -> mStrategy;
            case "movement" -> "move_to_point";
            default -> null;
        };
    }
}

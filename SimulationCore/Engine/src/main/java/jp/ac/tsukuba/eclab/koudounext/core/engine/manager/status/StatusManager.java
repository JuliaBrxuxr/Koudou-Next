package jp.ac.tsukuba.eclab.koudounext.core.engine.manager.status;

public class StatusManager {
    private StatusLoader mLoader;
    private StatusSaver mSaver;

    public StatusManager() {
        mLoader = new StatusLoader();
        mSaver = new StatusSaver();
    }

    public boolean saveStatus(){
        return mSaver.save();
    }

    public boolean loadStatus(int offset){
        return mLoader.load(offset);
    }
}

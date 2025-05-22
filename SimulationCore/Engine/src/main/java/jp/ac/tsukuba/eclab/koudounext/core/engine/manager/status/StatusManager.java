package jp.ac.tsukuba.eclab.koudounext.core.engine.manager.status;

import jp.ac.tsukuba.eclab.koudounext.cache.CacheConfig;
import jp.ac.tsukuba.eclab.koudounext.cache.CacheManager;
import jp.ac.tsukuba.eclab.koudounext.cache.exception.UnableStartCacheException;
import jp.ac.tsukuba.eclab.koudounext.cache.exception.UnableUnserializeObjectException;
import jp.ac.tsukuba.eclab.koudounext.core.engine.controller.SimulationController;
import jp.ac.tsukuba.eclab.koudounext.core.engine.utils.StepCounter;

public class StatusManager {
    private static volatile StatusManager instance = null;
    private StatusBean mStatus;
    private CacheManager mCache;

    public StatusManager() {
        mStatus = new StatusBean();
        mCache = CacheManager.getInstance();
        try {
            mCache.init(new CacheConfig(CacheConfig.CacheEngineTypeEnum.REDIS, "localhost", 6379));
        } catch (UnableStartCacheException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public boolean saveStatus() {
        try {
            mCache.saveObject(SimulationController.getInstance().getTaskUUID() + ":"
                    + mStatus.getStepCounter().getStepCount(), mStatus);
            return true;
        } catch (UnableUnserializeObjectException e) {
            return false;
        }
    }

    public boolean loadStatus(int offset) {
        try {
            mStatus = (StatusBean) mCache.getObject(SimulationController.getInstance().getTaskUUID() + ":"
                    + (mStatus.getStepCounter().getStepCount() + offset));
            return true;
        } catch (UnableUnserializeObjectException ex) {
            return false;
        }

    }

    public static StatusManager getInstance() {
        if (instance == null) {
            synchronized (StatusManager.class) {
                if (instance == null) {
                    instance = new StatusManager();
                }
            }
        }
        return instance;
    }

    public StatusBean getStatus() {
        return mStatus;
    }
}

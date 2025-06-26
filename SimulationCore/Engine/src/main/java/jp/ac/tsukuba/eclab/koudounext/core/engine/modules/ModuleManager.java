package jp.ac.tsukuba.eclab.koudounext.core.engine.modules;

import jp.ac.tsukuba.eclab.koudounext.core.engine.executor.SimulationThreadPoolManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent.AgentManagerImpl;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.environment.EnvironmentManagerImpl;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.expansion.ExpansionManagerImpl;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.MapManagerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ModuleManager {
    private AgentManagerImpl mAgentManager;
    private EnvironmentManagerImpl mEnvironmentManager;
    private MapManagerImpl mMapManager;
    private ExpansionManagerImpl mExpansionManager;

    private List<IModuleManager> mLoaders = new ArrayList<>();
    private static volatile ModuleManager instance = null;
    ExecutorService mThreadPool = SimulationThreadPoolManager.getThreadPool();

    public ModuleManager() {
        //TODO: Just for demo, needs to refactor to Factory Pattern
        mAgentManager = new AgentManagerImpl(mThreadPool);
        mEnvironmentManager = new EnvironmentManagerImpl();
        mMapManager = new MapManagerImpl();
        mExpansionManager = new ExpansionManagerImpl();


        mLoaders.add(mMapManager);
        mLoaders.add(mExpansionManager);
        mLoaders.add(mEnvironmentManager);
        mLoaders.add(mAgentManager);
    }

    public boolean loadAll(){
        mLoaders.forEach(IModuleManager::load);
        return true;
    }

    public boolean unloadAll(){
        mThreadPool.shutdown();
        return true;
    }

    public void load(String moduleName) {}

    public void preStep(){
        mLoaders.forEach(IModuleManager::preStep);
    }

    public void conflictStep(){
        mLoaders.forEach(IModuleManager::conflictStep);
    }

    public void postStep(){
        mLoaders.forEach(IModuleManager::postStep);
    }

    public void step(){
        mLoaders.forEach(IModuleManager::step);
    }

    public static ModuleManager getInstance() {
        if (instance == null) {
            synchronized (ModuleManager.class) {
                if (instance == null) {
                    instance = new ModuleManager();
                }
            }
        }
        return instance;
    }

    public MapManagerImpl getMapManager(){
        return mMapManager;
    }
}

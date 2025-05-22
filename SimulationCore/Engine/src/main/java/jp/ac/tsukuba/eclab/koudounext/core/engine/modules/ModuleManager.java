package jp.ac.tsukuba.eclab.koudounext.core.engine.modules;

import jp.ac.tsukuba.eclab.koudounext.core.engine.executor.SimulationThreadPoolManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent.AgentManagerImpl;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.environment.EnvironmentManagerImpl;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.expansion.ExpansionManagerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ModuleManager {
    private List<IModuleManager> mLoaders = new ArrayList<>();
    private static volatile ModuleManager instance = null;
    ExecutorService mThreadPool = SimulationThreadPoolManager.getThreadPool();

    public ModuleManager() {
        //TODO: Just for demo, needs to refactor to Factory Pattern
        mLoaders.add(new AgentManagerImpl(mThreadPool));
        mLoaders.add(new EnvironmentManagerImpl());
        mLoaders.add(new ExpansionManagerImpl());
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
}

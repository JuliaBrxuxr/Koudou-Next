package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.expansion;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.IModuleManager;

public class ExpansionManagerImpl implements IModuleManager
{
    @Override
    public boolean load() {
        //TODO: loader for others config
        return true;
    }

    @Override
    public boolean step() {
        return false;
    }
}

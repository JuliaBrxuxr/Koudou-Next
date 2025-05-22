package jp.ac.tsukuba.eclab.koudounext.core.engine.modules;

public interface IModuleManager {
    public boolean load();
    public boolean step();
    public boolean preStep();
    public boolean conflictStep();
    public boolean postStep();
}

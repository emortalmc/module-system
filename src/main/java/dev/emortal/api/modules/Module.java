package dev.emortal.api.modules;

import org.jetbrains.annotations.NotNull;

public abstract class Module {

    protected final ModuleManager moduleManager;

    protected Module(@NotNull ModuleEnvironment environment) {
        this.moduleManager = environment.moduleManager();
    }

    public abstract boolean onLoad();

    public abstract void onUnload();

    public void onReady() {
        // do nothing by default
    }
}

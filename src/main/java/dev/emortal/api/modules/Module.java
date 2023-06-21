package dev.emortal.api.modules;

import org.jetbrains.annotations.NotNull;

public abstract class Module {

    protected final ModuleEnvironment environment;

    protected Module(@NotNull ModuleEnvironment environment) {
        this.environment = environment;
    }

    public abstract boolean onLoad();

    public abstract void onUnload();

    public void onReady() {
        // do nothing by default
    }
}

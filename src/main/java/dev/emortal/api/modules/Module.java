package dev.emortal.api.modules;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Module {

    protected final ModuleEnvironment environment;

    protected Module(@NotNull ModuleEnvironment environment) {
        this.environment = environment;
    }

    protected <T extends Module> @Nullable T getModule(@NotNull Class<T> type) {
        return environment.moduleProvider().getModule(type);
    }

    public abstract boolean onLoad();

    public abstract void onUnload();

    public void onReady() {
        // do nothing by default
    }
}

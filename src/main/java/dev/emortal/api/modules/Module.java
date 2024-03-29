package dev.emortal.api.modules;

import dev.emortal.api.modules.env.ModuleEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Module {

    protected final ModuleEnvironment environment;

    protected Module(@NotNull ModuleEnvironment environment) {
        this.environment = environment;
    }

    protected <T extends Module> @NotNull T getModule(@NotNull Class<T> type) {
        T module = this.environment.moduleProvider().getModule(type);
        if (module == null) {
            throw new IllegalStateException("Required module was not available! Module should not have loaded if it was not.");
        }
        return module;
    }

    protected <T extends Module> @Nullable T getOptionalModule(@NotNull Class<T> type) {
        return this.environment.moduleProvider().getModule(type);
    }

    public abstract boolean onLoad();

    public abstract void onUnload();

    public void onReady() {
        // do nothing by default
    }
}

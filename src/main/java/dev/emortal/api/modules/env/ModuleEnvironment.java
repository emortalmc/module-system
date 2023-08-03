package dev.emortal.api.modules.env;

import dev.emortal.api.modules.ModuleData;
import dev.emortal.api.modules.ModuleProvider;
import org.jetbrains.annotations.NotNull;

/**
 * The environment that a module is loaded in.
 */
public interface ModuleEnvironment {

    /**
     * Data provided by the module.
     */
    @NotNull ModuleData data();

    /**
     * Provides access to existing modules to facilitate module dependencies.
     */
    @NotNull ModuleProvider moduleProvider();

    @FunctionalInterface
    interface Provider {

        @NotNull ModuleEnvironment create(@NotNull ModuleData data, @NotNull ModuleProvider moduleProvider);
    }
}

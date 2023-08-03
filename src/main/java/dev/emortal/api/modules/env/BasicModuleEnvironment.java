package dev.emortal.api.modules.env;

import dev.emortal.api.modules.ModuleData;
import dev.emortal.api.modules.ModuleProvider;
import org.jetbrains.annotations.NotNull;

/**
 * The basic environment that only includes the required fields.
 */
public record BasicModuleEnvironment(@NotNull ModuleData data, @NotNull ModuleProvider moduleProvider) implements ModuleEnvironment {
}

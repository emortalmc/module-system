package dev.emortal.api.modules.extension;

import dev.emortal.api.modules.ModuleProvider;
import dev.emortal.api.modules.annotation.ModuleData;
import dev.emortal.api.modules.env.ModuleEnvironment;
import org.jetbrains.annotations.NotNull;

public interface ModuleEnvironmentProvider {

    @NotNull ModuleEnvironment create(@NotNull ModuleData data, @NotNull ModuleProvider provider);
}

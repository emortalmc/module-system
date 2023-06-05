package dev.emortal.api.modules;

import org.jetbrains.annotations.NotNull;

public record ModuleEnvironment(@NotNull ModuleData data, @NotNull ModuleManager moduleManager) {
}

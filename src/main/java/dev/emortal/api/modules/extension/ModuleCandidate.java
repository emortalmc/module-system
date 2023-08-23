package dev.emortal.api.modules.extension;

import dev.emortal.api.modules.LoadableModule;
import dev.emortal.api.modules.Module;
import dev.emortal.api.modules.annotation.ModuleData;
import org.jetbrains.annotations.NotNull;

public record ModuleCandidate(@NotNull Class<? extends Module> clazz, @NotNull LoadableModule.Creator creator, @NotNull ModuleData data) {
}

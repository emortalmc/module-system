package dev.emortal.api.modules.extension;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface ModuleSorter {

    @NotNull List<ModuleCandidate> sortModules(@NotNull Collection<ModuleCandidate> modules);
}

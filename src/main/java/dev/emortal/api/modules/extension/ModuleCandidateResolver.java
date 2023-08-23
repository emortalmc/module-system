package dev.emortal.api.modules.extension;

import dev.emortal.api.modules.LoadableModule;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface ModuleCandidateResolver {

    @NotNull List<ModuleCandidate> resolveCandidates(@NotNull Collection<LoadableModule> modules);
}

package dev.emortal.api.modules.internal;

import dev.emortal.api.modules.LoadableModule;
import dev.emortal.api.modules.annotation.ModuleData;
import dev.emortal.api.modules.extension.ModuleCandidate;
import dev.emortal.api.modules.extension.ModuleCandidateResolver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class DefaultModuleCandidateResolver implements ModuleCandidateResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModuleCandidateResolver.class);

    @Override
    public @NotNull List<ModuleCandidate> resolveCandidates(@NotNull Collection<LoadableModule> modules) {
        List<ModuleCandidate> result = new ArrayList<>();

        for (LoadableModule module : modules) {
            ModuleData data = module.clazz().getDeclaredAnnotation(ModuleData.class);
            if (data == null) {
                LOGGER.error("ModuleData annotation not found on module class {}", module.clazz().getSimpleName());
                continue;
            }

            result.add(new ModuleCandidate(module.clazz(), module.creator(), data));
        }

        return result;
    }
}

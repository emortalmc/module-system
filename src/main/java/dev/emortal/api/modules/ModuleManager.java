package dev.emortal.api.modules;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dev.emortal.api.modules.annotation.Dependency;
import dev.emortal.api.modules.annotation.ModuleData;
import dev.emortal.api.modules.env.BasicModuleEnvironment;
import dev.emortal.api.modules.env.ModuleEnvironment;
import dev.emortal.api.modules.extension.ModuleCandidate;
import dev.emortal.api.modules.extension.ModuleCandidateResolver;
import dev.emortal.api.modules.extension.ModuleEnvironmentProvider;
import dev.emortal.api.modules.extension.ModuleSorter;
import dev.emortal.api.modules.internal.DefaultModuleCandidateResolver;
import dev.emortal.api.modules.internal.DefaultModuleSorter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModuleManager implements ModuleProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleManager.class);

    public static @NotNull Builder builder() {
        return new Builder();
    }

    private final @NotNull ModuleCandidateResolver candidateResolver;
    private final @NotNull ModuleSorter sorter;
    private final @NotNull ModuleEnvironmentProvider environmentProvider;

    private final Map<Class<? extends Module>, Module> modules = new ConcurrentHashMap<>();

    public ModuleManager(@NotNull ModuleCandidateResolver candidateResolver, @NotNull ModuleSorter sorter,
                          @NotNull ModuleEnvironmentProvider environmentProvider) {
        this.candidateResolver = candidateResolver;
        this.sorter = sorter;
        this.environmentProvider = environmentProvider;
    }

    public void loadModules(@NotNull Collection<LoadableModule> modules) {
        if (modules.isEmpty()) {
            LOGGER.warn("No modules provided to ModuleManager to be loaded");
            return;
        }

        List<ModuleCandidate> loadedModules = this.candidateResolver.resolveCandidates(modules);
        List<ModuleCandidate> sortedModules = this.sorter.sortModules(loadedModules);
        Set<String> loadedModuleNames = new HashSet<>();

        for (ModuleCandidate candidate : sortedModules) {
            if (!this.checkDependencies(candidate, loadedModuleNames)) continue;

            ModuleData data = candidate.data();
            ModuleEnvironment environment = this.environmentProvider.create(candidate.data(), this);

            Module module;
            try {
                module = candidate.creator().create(environment);
            } catch (Exception exception) {
                LOGGER.error("Failed to create module {}", data.name(), exception);
                continue;
            }

            Instant loadStart = Instant.now();
            boolean loadResult;
            try {
                loadResult = module.onLoad();
            } catch (Exception exception) {
                LOGGER.error("Failed to load module {}", data.name(), exception);
                continue;
            }

            Duration loadDuration = Duration.between(loadStart, Instant.now());
            if (!loadResult) continue; // Failed to load

            loadedModuleNames.add(data.name());
            this.modules.put(candidate.clazz(), module);
            LOGGER.info("Loaded module {} in {}ms", data.name(), loadDuration.toMillis());
        }
    }

    private boolean checkDependencies(@NotNull ModuleCandidate candidate, @NotNull Set<String> loadedModuleNames) {
        for (Dependency dependency : candidate.data().dependencies()) {
            if (!dependency.required()) continue; // Only fail load for required dependencies
            if (loadedModuleNames.contains(dependency.name())) continue; // Dependency is loaded

            LOGGER.error("Failed to load module {} due to missing dependency {}", candidate.data().name(), dependency.name());
            return false;
        }
        return true;
    }

    @Override
    public <T extends Module> @Nullable T getModule(@NotNull Class<T> type) {
        return type.cast(this.modules.get(type));
    }

    public void onReady() {
        for (Module module : this.modules.values()) {
            Instant readyStart = Instant.now();
            module.onReady();

            Duration readyDuration = Duration.between(readyStart, Instant.now());
            LOGGER.info("Fired onReady for module {} in {}ms", module.getClass().getSimpleName(), readyDuration.toMillis());
        }
    }

    public void onUnload() {
        for (Module module : this.modules.values()) {
            Instant unloadStart = Instant.now();
            module.onUnload();

            Duration unloadDuration = Duration.between(unloadStart, Instant.now());
            LOGGER.info("Unloaded module {} in {}ms", module.getClass().getSimpleName(), unloadDuration.toMillis());
        }
    }

    public static final class Builder {

        private @Nullable ModuleCandidateResolver candidateResolver;
        private @Nullable ModuleSorter sorter;
        private @Nullable ModuleEnvironmentProvider environmentProvider;

        private final Map<Class<? extends Module>, LoadableModule> modules = new HashMap<>();

        private Builder() {
        }

        public @NotNull Builder candidateResolver(@NotNull ModuleCandidateResolver candidateResolver) {
            this.candidateResolver = candidateResolver;
            return this;
        }

        public @NotNull Builder sorter(@NotNull ModuleSorter sorter) {
            this.sorter = sorter;
            return this;
        }

        public @NotNull Builder environmentProvider(@NotNull ModuleEnvironmentProvider environmentProvider) {
            this.environmentProvider = environmentProvider;
            return this;
        }

        public @NotNull Builder module(@NotNull Class<? extends Module> type, @NotNull LoadableModule.Creator creator) {
            this.modules.put(type, new LoadableModule(type, creator));
            return this;
        }

        public @NotNull ModuleManager build() {
            if (this.candidateResolver == null) this.candidateResolver = new DefaultModuleCandidateResolver();
            if (this.sorter == null) this.sorter = new DefaultModuleSorter();
            if (this.environmentProvider == null) this.environmentProvider = BasicModuleEnvironment::new;

            ModuleManager manager = new ModuleManager(this.candidateResolver, this.sorter, this.environmentProvider);
            manager.loadModules(this.modules.values());
            return manager;
        }
    }
}

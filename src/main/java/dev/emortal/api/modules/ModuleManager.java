package dev.emortal.api.modules;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import dev.emortal.api.modules.env.BasicModuleEnvironment;
import dev.emortal.api.modules.env.ModuleEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModuleManager implements ModuleProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleManager.class);

    private final @NotNull ModuleEnvironment.Provider moduleEnvironmentProvider;

    private final Map<Class<? extends Module>, Module> modules = new ConcurrentHashMap<>();

    public ModuleManager(@NotNull List<LoadableModule> modules, @NotNull ModuleEnvironment.Provider moduleEnvironmentProvider) {
        this.moduleEnvironmentProvider = moduleEnvironmentProvider;
        this.loadModules(modules);
    }

    public ModuleManager(@NotNull List<LoadableModule> modules) {
        this(modules, BasicModuleEnvironment::new);
    }

    private void loadModules(@NotNull List<LoadableModule> modules) {
        if (modules.isEmpty()) {
            LOGGER.warn("No modules provided to ModuleManager to be loaded");
            return;
        }

        List<LoadableModule> sortedModules = this.sortModules(modules);

        for (LoadableModule loadable : sortedModules) {
            ModuleData data = loadable.clazz().getDeclaredAnnotation(ModuleData.class);
            if (data == null) {
                LOGGER.error("Module class {} does not have a ModuleData annotation! Skipping...", loadable.clazz().getSimpleName());
                continue;
            }

            ModuleEnvironment environment = this.moduleEnvironmentProvider.create(data, this);
            Module module;
            try {
                module = loadable.creator().create(environment);
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

            if (loadResult) {
                this.modules.put(loadable.clazz(), module);
                LOGGER.info("Loaded module {} in {}ms (required: {})", data.name(), loadDuration.toMillis(), data.required());
            }
        }
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

    private @NotNull List<LoadableModule> sortModules(@NotNull Collection<LoadableModule> modules) {
        Graph<LoadableModule, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);

        for (LoadableModule module : modules) {
            graph.addVertex(module);

            ModuleData data = module.clazz().getDeclaredAnnotation(ModuleData.class);
            for (Class<? extends Module> dependency : data.softDependencies()) {
                // find the LoadableModule for the dependency's Class
                LoadableModule dependencyModule = modules.stream()
                        .filter(targetModule -> targetModule.clazz().equals(dependency))
                        .findFirst()
                        .orElse(null);

                if (dependencyModule == null) {
                    LOGGER.error("Module {} requires module {} to be loaded first.", module.clazz().getSimpleName(), dependency.getSimpleName());
                    continue;
                }
                graph.addVertex(dependencyModule);
                graph.addEdge(dependencyModule, module);
            }
        }

        TopologicalOrderIterator<LoadableModule, DefaultEdge> sortedIterator = new TopologicalOrderIterator<>(graph);
        List<LoadableModule> sorted = new ArrayList<>();
        sortedIterator.forEachRemaining(sorted::add);

        LOGGER.info("Loading modules: [{}]", sorted.stream().map(module -> module.clazz().getSimpleName()).collect(Collectors.joining(", ")));
        return sorted;
    }
}

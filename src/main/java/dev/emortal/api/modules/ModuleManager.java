package dev.emortal.api.modules;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModuleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleManager.class);

    private final Map<Class<? extends Module>, Module> modules = new ConcurrentHashMap<>();

    public ModuleManager(@NotNull Map<Class<? extends Module>, LoadableModule> modules) {
        final List<LoadableModule> sortedModules = sortModules(modules.values());

        for (final LoadableModule loadable : sortedModules) {
            final ModuleData data = loadable.clazz().getDeclaredAnnotation(ModuleData.class);

            final Module module;
            try {
                module = loadable.creator().create(new ModuleEnvironment(data, this));
            } catch (Exception exception) {
                LOGGER.error("Failed to create module {}", data.name(), exception);
                continue;
            }

            final Instant loadStart = Instant.now();
            final boolean loadResult = module.onLoad();
            final Duration loadDuration = Duration.between(loadStart, Instant.now());

            if (loadResult) {
                this.modules.put(loadable.clazz(), module);
                LOGGER.info("Loaded module {} in {}ms (required: {})", data.name(), loadDuration.toMillis(), data.required());
            }
        }
    }

    public @NotNull Collection<Module> getModules() {
        return modules.values();
    }

    public <T extends Module> @Nullable T getModule(@NotNull Class<T> type) {
        return type.cast(modules.get(type));
    }

    public void onReady() {
        for (final Module module : modules.values()) {
            final Instant readyStart = Instant.now();
            module.onReady();
            final Duration readyDuration = Duration.between(readyStart, Instant.now());

            LOGGER.info("Fired onReady for module {} in {}ms", module.getClass().getSimpleName(), readyDuration.toMillis());
        }
    }

    public void onUnload() {
        for (final Module module : modules.values()) {
            final Instant unloadStart = Instant.now();
            module.onUnload();
            final Duration unloadDuration = Duration.between(unloadStart, Instant.now());

            LOGGER.info("Unloaded module {} in {}ms", module.getClass().getSimpleName(), unloadDuration.toMillis());
        }
    }

    private List<LoadableModule> sortModules(Collection<LoadableModule> modules) throws IllegalArgumentException {
        final Graph<LoadableModule, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);

        for (final LoadableModule module : modules) {
            graph.addVertex(module);

            final ModuleData data = module.clazz().getDeclaredAnnotation(ModuleData.class);
            for (final Class<? extends Module> dependency : data.softDependencies()) {
                // find the LoadableModule for the dependency's Class
                final LoadableModule dependencyModule = modules.stream()
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

        final TopologicalOrderIterator<LoadableModule, DefaultEdge> sortedIterator = new TopologicalOrderIterator<>(graph);
        final List<LoadableModule> sorted = new java.util.ArrayList<>();
        sortedIterator.forEachRemaining(sorted::add);

        LOGGER.info("Loading modules: [{}]", sorted.stream().map(module -> module.clazz().getSimpleName()).collect(Collectors.joining(", ")));
        return sorted;
    }
}

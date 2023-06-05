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
        List<LoadableModule> sortedModules = sortModules(modules.values());

        for (LoadableModule loadable : sortedModules) {
            ModuleData data = loadable.clazz().getDeclaredAnnotation(ModuleData.class);

            Module module;
            try {
                module = loadable.creator().create(new ModuleEnvironment(data, this));
            } catch (Exception exception) {
                LOGGER.error("Failed to create module {}", data.name(), exception);
                continue;
            }

            Instant loadStart = Instant.now();
            boolean loadResult = module.onLoad();
            Duration loadDuration = Duration.between(loadStart, Instant.now());

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
        for (Module module : modules.values()) {
            Instant readyStart = Instant.now();
            module.onReady();
            Duration readyDuration = Duration.between(readyStart, Instant.now());

            LOGGER.info("Fired onReady for module {} in {}ms", module.getClass().getSimpleName(), readyDuration.toMillis());
        }
    }

    private List<LoadableModule> sortModules(Collection<LoadableModule> modules) throws IllegalArgumentException {
        Graph<LoadableModule, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);

        for (LoadableModule module : modules) {
            graph.addVertex(module);

            for (Class<? extends Module> dependency : module.clazz().getDeclaredAnnotation(ModuleData.class).softDependencies()) {
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
        List<LoadableModule> sorted = new java.util.ArrayList<>();

        sortedIterator.forEachRemaining(sorted::add);

        LOGGER.info("Loading modules: [{}]", sorted.stream().map(module -> module.clazz().getSimpleName()).collect(Collectors.joining(", ")));

        return sorted;
    }
}

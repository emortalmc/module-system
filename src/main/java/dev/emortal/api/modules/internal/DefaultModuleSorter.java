package dev.emortal.api.modules.internal;

import dev.emortal.api.modules.annotation.Dependency;
import dev.emortal.api.modules.extension.ModuleCandidate;
import dev.emortal.api.modules.extension.ModuleSorter;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DefaultModuleSorter implements ModuleSorter {

    @Override
    public @NotNull List<ModuleCandidate> sortModules(@NotNull Collection<ModuleCandidate> modules) {
        List<ModuleCandidate> sortedCandidates = new ArrayList<>(modules);
        sortedCandidates.sort(Comparator.comparing(candidate -> candidate.data().name()));

        Graph<ModuleCandidate, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        Map<String, ModuleCandidate> moduleMap = new HashMap<>();
        for (ModuleCandidate module : modules) {
            moduleMap.put(module.data().name(), module);
        }

        for (ModuleCandidate module : modules) {
            graph.addVertex(module);

            for (Dependency dependency : module.data().dependencies()) {
                // find the LoadableModule for the dependency's Class
                ModuleCandidate dependencyModule = moduleMap.get(dependency.name());
                if (dependencyModule == null) continue;

                graph.addVertex(dependencyModule);
                graph.addEdge(dependencyModule, module);
            }
        }
        TopologicalOrderIterator<ModuleCandidate, DefaultEdge> iterator = new TopologicalOrderIterator<>(graph);

        List<ModuleCandidate> sorted = new ArrayList<>();
        iterator.forEachRemaining(sorted::add);

        return sorted;
    }
}

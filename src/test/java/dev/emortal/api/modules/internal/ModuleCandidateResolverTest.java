package dev.emortal.api.modules.internal;

import dev.emortal.api.modules.LoadableModule;
import dev.emortal.api.modules.annotation.ModuleData;
import dev.emortal.api.modules.env.ModuleEnvironment;
import dev.emortal.api.modules.extension.ModuleCandidate;
import dev.emortal.testing.DummyModule;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ModuleCandidateResolverTest {

    @Test
    void testModuleNotResolvedIfModuleDataNotPresent() {
        DefaultModuleCandidateResolver resolver = new DefaultModuleCandidateResolver();
        List<LoadableModule> modules = List.of(new LoadableModule(NotAnnotatedModule.class, NotAnnotatedModule::new));

        List<ModuleCandidate> candidates = resolver.resolveCandidates(modules);
        assertTrue(candidates.isEmpty());
    }

    @Test
    void testModuleResolvedWhenModuleDataPresent() {
        DefaultModuleCandidateResolver resolver = new DefaultModuleCandidateResolver();
        List<LoadableModule> modules = List.of(new LoadableModule(AnnotatedModule.class, AnnotatedModule::new));

        List<ModuleCandidate> candidates = resolver.resolveCandidates(modules);
        assertEquals(1, candidates.size());
        assertSame(AnnotatedModule.class, candidates.get(0).clazz());
    }

    @ModuleData(name = "annotated")
    private static final class AnnotatedModule extends DummyModule {

        public AnnotatedModule(@NotNull ModuleEnvironment environment) {
            super(environment);
        }
    }

    private static final class NotAnnotatedModule extends DummyModule {

        public NotAnnotatedModule(@NotNull ModuleEnvironment environment) {
            super(environment);
        }
    }
}

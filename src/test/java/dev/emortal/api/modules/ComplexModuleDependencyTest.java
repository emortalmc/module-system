package dev.emortal.api.modules;

import dev.emortal.api.modules.annotation.Dependency;
import dev.emortal.api.modules.annotation.ModuleData;
import dev.emortal.api.modules.env.ModuleEnvironment;
import dev.emortal.testing.DummyModule;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class ComplexModuleDependencyTest {

    @Test
    void testModuleLoadsWithRequiredDependencyIfNoOptionalDependency() {
        ModuleManager.builder()
                .module(RequiredDependencyModule.class, RequiredDependencyModule::new)
                .module(DependentModule.class, env -> new DependentModule(env, false))
                .build();
    }

    @ModuleData(name = "required-dependency")
    private static final class RequiredDependencyModule extends DummyModule {

        public RequiredDependencyModule(@NotNull ModuleEnvironment environment) {
            super(environment);
        }
    }

    @ModuleData(name = "optional-dependency")
    private static final class OptionalDependencyModule extends DummyModule {

        public OptionalDependencyModule(@NotNull ModuleEnvironment environment) {
            super(environment);
        }
    }

    @ModuleData(name = "dependent", dependencies = {
            @Dependency(name = "required-dependency"),
            @Dependency(name = "optional-dependency", required = false)
    })
    private static final class DependentModule extends DummyModule {

        private final boolean expectingOptionalModule;

        public DependentModule(@NotNull ModuleEnvironment environment, boolean expectingOptionalModule) {
            super(environment);
            this.expectingOptionalModule = expectingOptionalModule;
        }

        @Override
        public boolean onLoad() {
            RequiredDependencyModule required = assertDoesNotThrow(() -> this.getModule(RequiredDependencyModule.class));
            assertNotNull(required);

            if (this.expectingOptionalModule) {
                assertNotNull(this.getOptionalModule(OptionalDependencyModule.class));
            } else {
                assertNull(this.getOptionalModule(OptionalDependencyModule.class));
            }

            return true;
        }
    }
}

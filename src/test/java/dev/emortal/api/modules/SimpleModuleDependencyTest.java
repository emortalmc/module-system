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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public final class SimpleModuleDependencyTest {

    @Test
    void testRequiredDependentModuleDoesNotLoadIfDependencyNotPresent() {
        ModuleManager manager = ModuleManager.builder()
                .module(RequiredDependentModule.class, env -> new RequiredDependentModule(env, false))
                .build();

        assertNull(manager.getModule(RequiredDependentModule.class));
    }

    @Test
    void testRequiredDependentModuleLoadsWithDependency() {
        ModuleManager manager = ModuleManager.builder()
                .module(DependencyModule.class, DependencyModule::new)
                .module(RequiredDependentModule.class, env -> new RequiredDependentModule(env, true))
                .build();

        assertNotNull(manager.getModule(DependencyModule.class));
        assertNotNull(manager.getModule(RequiredDependentModule.class));
    }

    @Test
    void testOptionalDependentModuleLoadsIfDependencyNotPresent() {
        ModuleManager manager = ModuleManager.builder()
                .module(OptionalDependentModule.class, env -> new OptionalDependentModule(env, false))
                .build();

        assertNotNull(manager.getModule(OptionalDependentModule.class));
    }

    @Test
    void testOptionalDependentModuleLoadsWithDependency() {
        ModuleManager manager = ModuleManager.builder()
                .module(DependencyModule.class, DependencyModule::new)
                .module(OptionalDependentModule.class, env -> new OptionalDependentModule(env, true))
                .build();

        assertNotNull(manager.getModule(OptionalDependentModule.class));
    }

    @ModuleData(name = "dependency")
    private static final class DependencyModule extends DummyModule {

        public DependencyModule(@NotNull ModuleEnvironment environment) {
            super(environment);
        }
    }

    @ModuleData(name = "required-dependent", dependencies = {@Dependency(name = "dependency")})
    private static final class RequiredDependentModule extends DummyModule {

        private final boolean expectingDependency;

        public RequiredDependentModule(@NotNull ModuleEnvironment environment, boolean expectingDependency) {
            super(environment);
            this.expectingDependency = expectingDependency;
        }

        @Override
        public boolean onLoad() {
            if (!this.expectingDependency) {
                fail("Hard dependent module loaded without dependency present!");
            }

            DependencyModule module = assertDoesNotThrow(() -> this.getModule(DependencyModule.class));
            assertNotNull(module);
            return true;
        }
    }

    @ModuleData(name = "optional-dependent", dependencies = {@Dependency(name = "dependency", required = false)})
    private static final class OptionalDependentModule extends DummyModule {

        private final boolean expectingDependency;

        public OptionalDependentModule(@NotNull ModuleEnvironment environment, boolean expectingDependency) {
            super(environment);
            this.expectingDependency = expectingDependency;
        }

        @Override
        public boolean onLoad() {
            if (!this.expectingDependency) {
                assertThrows(IllegalStateException.class, () -> this.getModule(DependencyModule.class));
                return true;
            }

            DependencyModule module = assertDoesNotThrow(() -> this.getModule(DependencyModule.class));
            assertNotNull(module);
            return true;
        }
    }
}

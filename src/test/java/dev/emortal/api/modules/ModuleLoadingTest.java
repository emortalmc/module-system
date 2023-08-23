package dev.emortal.api.modules;

import dev.emortal.api.modules.annotation.ModuleData;
import dev.emortal.api.modules.env.ModuleEnvironment;
import dev.emortal.testing.DummyModule;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

public final class ModuleLoadingTest {

    @Test
    void testModuleNotLoadedIfCreatorThrowsException() {
        ModuleManager manager = ModuleManager.builder()
                .module(NeverCreatedModule.class, env -> {
                    throw new RuntimeException();
                })
                .build();

        assertNull(manager.getModule(DummyModule.class));
    }

    @Test
    void testModuleNotLoadedIfLoadThrowsException() {
        ModuleManager manager = ModuleManager.builder()
                .module(ExceptionThrowingModule.class, ExceptionThrowingModule::new)
                .build();

        assertNull(manager.getModule(ExceptionThrowingModule.class));
    }

    @Test
    void testModuleNotLoadedIfLoadReturnsFalse() {
        ModuleManager manager = ModuleManager.builder()
                .module(AlwaysFailingModule.class, AlwaysFailingModule::new)
                .build();

        assertNull(manager.getModule(AlwaysFailingModule.class));
    }

    @Test
    void testModuleLoadedWhenSuccessful() {
        ModuleManager manager = ModuleManager.builder()
                .module(AlwaysSuccessfulModule.class, AlwaysSuccessfulModule::new)
                .build();

        assertNotNull(manager.getModule(AlwaysSuccessfulModule.class));
    }

    @ModuleData(name = "never-created")
    private static final class NeverCreatedModule extends DummyModule {

        public NeverCreatedModule(@NotNull ModuleEnvironment environment) {
            super(environment);
            fail("Module should never be created!");
        }
    }

    @ModuleData(name = "exception-throwing")
    private static final class ExceptionThrowingModule extends DummyModule {

        public ExceptionThrowingModule(@NotNull ModuleEnvironment environment) {
            super(environment);
        }

        @Override
        public boolean onLoad() {
            throw new RuntimeException();
        }
    }

    @ModuleData(name = "always-failing")
    private static final class AlwaysFailingModule extends DummyModule {

        public AlwaysFailingModule(@NotNull ModuleEnvironment environment) {
            super(environment);
        }

        @Override
        public boolean onLoad() {
            return false;
        }
    }

    @ModuleData(name = "always-successful")
    private static final class AlwaysSuccessfulModule extends DummyModule {

        public AlwaysSuccessfulModule(@NotNull ModuleEnvironment environment) {
            super(environment);
        }

        @Override
        public boolean onLoad() {
            return true;
        }
    }
}

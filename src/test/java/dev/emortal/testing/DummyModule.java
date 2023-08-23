package dev.emortal.testing;

import dev.emortal.api.modules.Module;
import dev.emortal.api.modules.env.ModuleEnvironment;
import org.jetbrains.annotations.NotNull;

public class DummyModule extends Module {

    public DummyModule(@NotNull ModuleEnvironment environment) {
        super(environment);
    }

    @Override
    public boolean onLoad() {
        return true;
    }

    @Override
    public void onUnload() {
        // do nothing
    }
}

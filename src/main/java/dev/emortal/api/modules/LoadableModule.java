package dev.emortal.api.modules;

import dev.emortal.api.modules.env.ModuleEnvironment;
import org.jetbrains.annotations.NotNull;

public record LoadableModule(@NotNull Class<? extends Module> clazz, @NotNull Creator creator) {

    @FunctionalInterface
    public interface Creator {

        @NotNull Module create(@NotNull ModuleEnvironment environment) throws Exception;
    }
}

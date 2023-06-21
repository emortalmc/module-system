package dev.emortal.api.modules;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ModuleProvider {

    <T extends Module> @Nullable T getModule(@NotNull Class<T> type);
}

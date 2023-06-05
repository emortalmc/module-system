package dev.emortal.api.modules;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.jetbrains.annotations.NotNull;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleData {

    @NotNull String name();

    boolean required();

    @NotNull Class<? extends Module>@NotNull[] softDependencies() default {};
}

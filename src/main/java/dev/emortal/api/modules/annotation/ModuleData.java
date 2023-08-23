package dev.emortal.api.modules.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.jetbrains.annotations.NotNull;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleData {

    @NotNull String name();

    @NotNull Dependency[] dependencies() default {};
}

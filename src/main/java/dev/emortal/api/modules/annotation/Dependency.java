package dev.emortal.api.modules.annotation;

import org.jetbrains.annotations.NotNull;

public @interface Dependency {

    @NotNull String name();

    boolean required() default true;
}

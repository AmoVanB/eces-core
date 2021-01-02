package de.tum.ei.lkn.eces.core.annotations;

import de.tum.ei.lkn.eces.core.RootSystem;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation allowing to specify to which System (default: to RootSystem class)
 * a Component belongs.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentBelongsTo {
	Class<? extends RootSystem> system() default RootSystem.class;
}
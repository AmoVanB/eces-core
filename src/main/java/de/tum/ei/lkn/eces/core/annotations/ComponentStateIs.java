package de.tum.ei.lkn.eces.core.annotations;

import de.tum.ei.lkn.eces.core.ComponentStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allowing to define a listener method. This means that it allows to
 * specify that a method should be run when an event associated to a specific
 * Component is triggered (default: any event).
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ComponentStateIs {
	ComponentStatus State() default ComponentStatus.Any;
}
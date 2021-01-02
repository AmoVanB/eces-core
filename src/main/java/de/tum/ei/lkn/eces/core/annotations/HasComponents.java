package de.tum.ei.lkn.eces.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation automatically used to allow the HasComponent annotation to be
 * repeated (in which case the Filter will be applied several times and the
 * method will finally be executed if all the Filters accept the Component).
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HasComponents {
	HasComponent[] value();
}
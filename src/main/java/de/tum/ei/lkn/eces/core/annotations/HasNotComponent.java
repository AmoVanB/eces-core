package de.tum.ei.lkn.eces.core.annotations;

import de.tum.ei.lkn.eces.core.Component;

import java.lang.annotation.*;

/**
 * Annotation used on listeners methods (i.e. methods with ComponentStateIs
 * annotation). It allows to execute the method only when the Component which
 * triggered the listener is accepted by the HasNotComponentFilter (source of
 * the HasNotComponentFilter is specified by the 'component' attribute).
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@Repeatable(value = HasNotComponents.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HasNotComponent {
	Class<? extends Component> component();
}
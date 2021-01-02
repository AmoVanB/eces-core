package de.tum.ei.lkn.eces.core.annotations;

import de.tum.ei.lkn.eces.core.Component;

import java.lang.annotation.*;

/**
 * Annotation used on listeners methods (i.e. methods with a ComponentStateIs
 * annotation). It allows to execute the method only when the Component which
 * triggered the listener is accepted by the HasComponentFilter (source of the
 * HasComponentFilter is specified by the 'component' attribute).
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@Repeatable(HasComponents.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HasComponent {
	Class<? extends Component> component();
}
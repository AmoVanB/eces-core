# Core

This project implements an *Entity-Component Event-based System* (ECES) framework.
In such a framework, classes are organized as components that attach to entities, and listeners (or callbacks) can be easily configured to asynchronously run upon the creation/deletion/update of selected components.

## Usage

The project can be downloaded from maven central using:
```xml
<dependency>
  <groupId>de.tum.ei.lkn.eces</groupId>
  <artifactId>core</artifactId>
  <version>X.Y.Z</version>
</dependency>
```

### Simple Example 

The following piece of code creates a simple system that counts the number of times a `C1` component has been attached to an entity. 
```java
public class S1 extends RootSystem {
	private int counter = 0;
	private C1Mapper c1Mapper = new C1Mapper(controller);

	public S1(Controller controller) {
		super(controller);
	}

	@ComponentStateIs(State = ComponentStatus.New)
	public synchronized void countC1(C1 c) {
		counter++;
	}
	
	public int getCounter() {
		return counter;
	}
}
```

The following piece of code would then print out `1`, as one `C1` component was attached to an entity.

```java
Controller controller = new Controller();
S1 s = new S1(contoller);
Entity ent = controller.createEntity();
C1 comp = new C1();
C1Mapper c1Mapper = new C1Mapper(controller);
c1Mapper.attachComponent(ent, comp);
System.out.println(s.getCounter());
```

### Advanced Examples

See other ECES repositories (e.g., [graph](https://github.com/AmoVanB/eces-graph), [network](https://github.com/AmoVanB/eces-network), and [routing](https://github.com/AmoVanB/eces-routing)) for more detailed/advanced examples.

## The ECES Framework

The framework consists of *entities* to which *components* can be attached. An entity is an instance of the `Entity.java` class while components are instances of (subclasses of) the `Component.java` class. A component must hence derive from the basic `Component.java` class but can then define any number of new members and/or methods for its operational purpose.

A single entity can hold any number of components but only one instance of each component class.

### Systems

A component belongs to a single *system*. This is defined using the `@ComponentBelongsTo` annotation in the definition of the class of the component. A system is an instance of (a subclass of) `RootSystem.java`. Using the `@ComponentStateIs` annotation, a system can implement methods that will be run on the creation (i.e., attachment to an entity), update or deletion (i.e., detachment from an entity) of a given component class. Since they listen to events on particular components, these methods are called *listeners*. These methods must have a single parameter the type of which defines the type of component to which it has to listen.

### The Controller

Any system, when instantiated, is automatically registered to a *controller* (an automatically created instance of `Controller.java`). A same system class can only be registered once to a controller (i.e., can only be instantiated once). The controller is responsible for handling the events and running the corresponding listeners methods of the systems registered to it.

The controller provides a `createEntity()` method to generate entities.

### The Mapper

The attachment, update and detachment of components from an entity are done using a mapper, i.e., an instance of the `Mapper.java` class. When implementing a new component, a corresponding mapper should be created. It simply has to extend `Mapper<X>` where `X` is replaced by the given component. The `new Mapper(Controller controller)` constructor can then be used. A mapper can simply be obtained by calling its constructor with the responsible controller as parameter. The `attachComponent()`, `updateComponent()` and `detachComponent()` of the mapper can then be used to respectively attach, update or detach a component to/from an entity. The mapper also provides a `get()` method allowing to retrieve the instance of the component class managed by the mapper which is attached to a given entity.

### The Mapper Space

The framework is multi-threaded. For this purpose, a *mapper space* concept is introduced. A mapper space is a block of code in which the mapper methods can be used. All the attachment, detachment and update jobs triggered within a mapper space are effectively executed at  the closure (i.e., at the end) of the mapper space, along with their associated listeners. If the mapper methods are not used within a mapper space, they are automatically enclosed within a mapper space of one statement.

A mapper space is defined as follows using the Java *try-with-resources* statement:

``` java
try (MapperSpace ms = controller.startMapperSpace()) {
    // commands to execute in the mapper space
}
```

In order to ensure consistency when reading data from a component, the mapper also provides an `acquireReadLock()` method. The read lock(s) acquired is (are) automatically released at the closure of the mapper space in which they have been acquired.

Note that it is possible to define a mapper space *within* a mapper space. However, this will result in the internal mapper space not being created. This allows methods using mapper spaces to be called within another mapper space. Note that the attachment, update, detachment and associated listeners will then be executed only at the end of the global mapper space. This means that, if one defines a mapper space, nothing ensures that, when executing the lines of code written *after* the mapper space, the attachment, update, detachment and associated listeners of the mapper space will have been executed. Indeed, these lines of code might also be part of a parent mapper space which is not yet closed.

### The Local Component

In some situations, it might be handy to have different data stored for a single component of a system. This is done by deriving from the `LocalComponent.java` class. Each instance of data is then an instance of the Java `Object.java` class. Such a local component, which is a component, is to be managed by a *local mapper*, which can be obtained using the `getLocalMapper()` method of the controller. When asking for such a mapper, an *owner* must be specified. This owner is a Java object owning a given instance of the data stored in the local component. The local mapper method `get()` method will then take care to return the data instance corresponding to the owner for which this local mapper has been created. Different owners hence define different instances of data stored in the component.

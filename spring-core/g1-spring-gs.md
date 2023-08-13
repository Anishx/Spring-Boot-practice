:book: **Creating a Maven Project**  

XML schema

An XML schema is a description of a type of XML document, typically expressed in terms of constraints on the structure and content of documents of that type, above and beyond the basic syntactical constraints imposed by XML itself.

:book: **Spring Modules**  

:beginner: **The Spring Core Module**  

:point_right: Inversion of Control (IOC)  
- inversion of control (IoC) is a programming principle
- custom-written portions of a computer program receive the flow of control from a generic framework
- in traditional programming, the custom code that expresses the purpose of the program calls into reusable libraries to take care of generic tasks, but with inversion of control, it is the framework that calls into the custom, or task-specific, code.

:point_right: Dependency Injection (DI)
- dependency injection is a technique in which an object receives other objects that it depends on.
- These other objects are called dependencies. 
- In the typical "using" relationship the receiving object is called a client and the passed (that is, "injected") object is called a service.
- The code that passes the service to the client can be many kinds of things and is called the injector. 
- Instead of the client specifying which service it will use, the injector tells the client what service to use. The "injection" refers to the passing of a dependency (a service) into the object (a client) that would use it.  

:point_right: Spring Framework
- Spring Framework’s Inversion of Control (IoC) container.  
- Spring Framework implements of the Inversion of Control (IoC) principle.
- IoC is also known as dependency injection (DI). 
- The org.springframework.context.ApplicationContext interface represents the Spring IoC container and is responsible for instantiating, configuring, and assembling the beans. 
- The configuration metadata is represented in XML, Java annotations, or Java code.
- Several implementations of the ApplicationContext interface are supplied with Spring.
- In stand-alone applications, it is common to create an instance of ClassPathXmlApplicationContext or FileSystemXmlApplicationContext. 

```java
// create and configure beans
ApplicationContext context = new ClassPathXmlApplicationContext("services.xml", "daos.xml");

// retrieve configured instance
PetStoreService service = context.getBean("petStore", PetStoreService.class);

// use configured instance
List<String> userList = service.getUsernameList();
```

:point_right: Plain Old Java Object (POJO)  
- a plain java object
- should not implement interfaces from the API
- no API specific annotations
[POJO wiki](https://en.wikipedia.org/wiki/Plain_old_Java_object)  

:beginner: **Types of Configuration**
- XML Configuration
- Annotation Configuration
- Java Configuration  

:beginner: **Bean Scopes**  

:beginner: **Bean Lifecycle**  
[Prototype Destory Method](https://docs.spring.io/spring-framework/docs/4.0.x/spring-framework-reference/htmlsingle/#beans-factory-scopes-prototype)  


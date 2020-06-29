Spring Destroy Method prototype Scope
===========================================
	- Default scope of bean is singleton i.e, Spring container creates only one instance of bean by default
	- It is in cached in memory
	- All requests for bean will return a shared referenceto the same bean
	- It is just like String literal, e.g.

Spring Bean LifeCycle
===================================
	Spring Container Starts ---> Bean Instantiated ---> Dependencies Injected ---> Internal Spring Processing ---> [Your Custom Init method] ---> Bean is ready to Use ---> Methods are called using Bean Object ---> Container/Application shutdown ---> [Your custom Destroy method]

init and destroy methods can have 
-----------------------------------------
Access modifier
	The method can have any access modifier (public, protected, private)

Return type
	The method can have any return type. However, "void" is most commonly used. If you give a return type just note that you will not be able to capture the return value. As a result, "void" is commonly used.

Method name
	The method can have any method name.

Arguments
	The method can not accept any arguments. The method should be no-arg.

***
Note :-
	There is a subtle point you need to be aware of with "prototype" scoped beans. For "prototype" scoped beans, Spring does not call the destroy method.

	In contrast to the other scopes, Spring does not manage the complete lifecycle of a prototype bean: the container instantiates, configures, and otherwise assembles a prototype object, and hands it to the client, with no further record of that prototype instance.

	Thus, although initialization lifecycle callback methods are called on all objects regardless of scope, in the case of prototypes, configured destruction lifecycle callbacks are not called. The client code must clean up prototype-scoped objects and release expensive resources that the prototype bean(s) are holding. 
***

To call destroy method on prototype scoped beans
=============================================================
1. Create a custom bean processor. This bean processor will keep track of prototype scoped beans. During shutdown it will call the destroy() method on the prototype scoped beans. The custom processor is configured in the spring config file.
	
	<bean id="customProcessor"
    		class="demo.MyCustomBeanProcessor">
   	</bean>

2. The prototype scoped beans MUST implement the DisposableBean interface. This interface defines a "destory()" method. 
   
    public class BadmintonCoach implements Coach, DisposableBean {
     
    	...
    	
    	// add a destroy method
    	@Override
    	public void destroy() throws Exception {
    		System.out.println("BadmintonCoach: inside destroy method");		
    	}
     
    }

3. The spring configuration must be updated to use the destroy-method of "destroy". 
     
     	<bean id="myCoach"
     		class="demo.BadmintonCoach"
     		init-method="doMyStartupStuff"
     		destroy-method="destroy"
     		scope="prototype">	
     		
     		<!-- set up constructor injection -->
     		<constructor-arg ref="myFortuneService" />
     	</bean>

4. In this app, BeanLifeCycleDemoApp.java is the main program.  BadmintonCoach.java is the prototype scoped bean. BadmintonCoach implements the DisposableBean interface and provides the destroy() method. The custom bean processing is handled in the MyCustomBeanProcessor class.


Programs
--------------------
1. Coach.java
----------------
package demo;

public interface Coach {

	public String getDailyWorkout();

	public String getDailyFortune();

}

2. BadmintonCoach.java
-------------------------------
package demo;

import org.springframework.beans.factory.DisposableBean;

public class BadmintonCoach implements Coach, DisposableBean {

	private FortuneService fortuneService;

	public BadmintonCoach() {
		
	}
	
	public BadmintonCoach(FortuneService fortuneService) {
		this.fortuneService = fortuneService;
	}

	@Override
	public String getDailyWorkout() {
		return "Spend 2 hours on Badminton";
	}

	@Override
	public String getDailyFortune() {
		return "Just Do It: " + fortuneService.getFortune();
	}

	// add an init method
	public void doMyStartupStuff() {
		System.out.println("BadmintonCoach: inside method doMyStartupStuff");
	}
	
	// add a destroy method
	@Override
	public void destroy() throws Exception {
		System.out.println("BadmintonCoach: inside destroy method");		
	}
}

3. FortuneService.java
-------------------------------
package demo;

public interface FortuneService {

	public String getFortune();
	
}

4. HappyFortuneService.java
-------------------------------------
package demo;

public class HappyFortuneService implements FortuneService {

	@Override
	public String getFortune() {
		return "Today is your lucky day!";
	}

}

5. BeanLifeCycleDemoApp.java
-------------------------------
package demo;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BeanLifeCycleDemoApp {
	
	public static void main(String[] args) {

		// load the spring configuration file
		ClassPathXmlApplicationContext context =
				new ClassPathXmlApplicationContext("beanLifeCycle-applicationContext.xml");
				
		// retrieve bean from spring container
		Coach theCoach = context.getBean("myCoach", Coach.class);
		Coach alphaCoach = context,getBean("myCoach", Coach.class);

		System.out.println(theCoach.getDailyWorkout());
		System.out.println(theCoach.getDailyFortune());
		
		// close the context
		context.close();
	}

}

6. MyCustomBeanProcessor.java
------------------------------------
package demo;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class MyCustomBeanProcessor implements BeanPostProcessor, BeanFactoryAware, DisposableBean {

	private BeanFactory beanFactory;

	private final List<Object> prototypeBeans = new LinkedList<>();

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

		// after start up, keep track of the prototype scoped beans. 
		// we will need to know who they are for later destruction
		
		if (beanFactory.isPrototype(beanName)) {
			synchronized (prototypeBeans) {
				prototypeBeans.add(bean);
			}
		}

		return bean;
	}


	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}


	@Override
	public void destroy() throws Exception {

		// loop through the prototype beans and call the destroy() method on each one
		
        synchronized (prototypeBeans) {

        	for (Object bean : prototypeBeans) {

        		if (bean instanceof DisposableBean) {
                    DisposableBean disposable = (DisposableBean)bean;
                    try {
                        disposable.destroy();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        	prototypeBeans.clear();
        }
        
	}
}

7. beanLifecycle-applicationContext.xml
-------------------------------------------
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.springframework.org/schema/context
    http://www.springframework.org/schema/context/spring-context.xsd">
	
    <!-- Define your beans here -->
    
    <!-- define the dependency -->
    <bean id="myFortune"
    		class="demo.HappyFortuneService">
    	</bean>
    
 	<bean id="myCoach"
 		class="demo.BadmintonCoach"
 		init-method="doMyStartupStuff"
 		destroy-method="destroy"
 		scope="prototype">	
 		
 		<!-- set up constructor injection -->
 		<constructor-arg ref="myFortune" />
 	</bean>

	<!-- Bean custom processor to handle calling destroy methods on prototype scoped beans -->
    <bean id="customProcessor"
    		class="demo.MyCustomBeanProcessor">
   	</bean>
    
</beans>


synchronized block
-----------------------
- if we need synchronized code or few lines of code and not entire method then we can use synchronized block inside the method instead of making the method synchronized.

- Advantage is Multiple threads can enter the method and execute the code before the synchronized block , but once one thread enters this block, no other thread will be able to execute the synchronized block untill the first thread finishes.

- It will make the wait time go down and will improve the performance because the threads can atleast start running the codes prior to the synchronized block.

- It can be defined by 3 ways 

1. by passing current object
	synchronized(this){

	}

2. by passing any object
	synchronized(x){

	}
3. by passing a class name
	synchronized(Display.class){

	}


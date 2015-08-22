# java-groovy-cli
Quickly create a Groovy CLI for testing and debugging Java applications

## usage
1. [Download the jar](https://github.com/lovromazgon/java-groovy-cli/raw/master/lib/java-groovy-cli-0.1.jar) and add it to the classpath of your project
2. Make sure [Groovy]() is also on the classpath (this project uses version 2.4.4)
3. Create a main method, where you create the `GroovyCLI` object, inject all variables, which can be used in the scripts, and call `runGroovyConsole()`:
```
public static void main(String[] args) {
	GroovyCLI groovyCLI = new GroovyCLI();
	groovyCLI.setVariable("testService", new TestService("groovy cli"));
	groovyCLI.runGroovyConsole();
}
```

Run the application and you can write Groovy scripts in the command line. After you have written the whole script, you can execute it with the keyword ";;". If you want to exit the CLI, you can write the keyword "exit".

## Spring
If you are using Spring in your Java application, you can test the components easily and invoke methods on demand. You have to load the application context and create bindings for the beans, which you want to use.
```
public static void main(String[] args) {
	//load Spring application context
	AbstractApplicationContext context = new AnnotationConfigApplicationContext(MySpringConfiguration.class);
	//load the beans, which you want to use
	MyService myService = context.getAutowireCapableBeanFactory().getBean(MyService.class);
	
	//create the GroovyCLI object and bind the beans to variables
	//alternatively you can bind the whole context and acquire the beans inside the script
	GroovyCLI groovyCLI = new GroovyCLI();
	groovyCLI.setVariable("myService", myService);
	groovyCLI.setVariable("appContext", context);
	groovyCLI.runGroovyConsole();
	
	//close Spring context
	context.close();
}
```

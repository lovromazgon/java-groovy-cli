package org.codehaus.groovy.cli;

import groovy.lang.Binding;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class GroovyCLITest {
	/*
	 * Main method for running practical tests - that's what this project is actually trying to achieve
	 */
	public static void main(String[] args) {
		GroovyCLI groovyCLI = new GroovyCLI();
		groovyCLI.setVariable("testService", new TestService("groovy cli"));
		groovyCLI.runGroovyConsole();
	}

	@Test
	public void scriptSuccess() {
		TestService testService = new TestService("groovy cli");

		String script = "out.println(testService.hello());\n" +
				";;\nexit";
		GroovyCLI groovyCLI = new GroovyCLI(new Binding(), new ByteArrayInputStream(script.getBytes()), System.out);
		groovyCLI.setVariable("testService", testService);
		groovyCLI.runGroovyConsole();

		Assert.assertEquals("The script output was not as expected!", testService.hello(), groovyCLI.getScriptOutputStreamAll().toString().trim());
	}

	@Test
	public void scriptPropertyException() {
		String script = "out.println(testService.hello());\n" +
				";;\nexit";

		GroovyCLI groovyCLI = new GroovyCLI(new Binding(), new ByteArrayInputStream(script.getBytes()), System.out);
		groovyCLI.runGroovyConsole();

		Assert.assertEquals("The script output was not as expected!", "", groovyCLI.getScriptOutputStreamAll().toString());
	}

	@Test
	public void scriptCompilationException() {
		String script = "out.println(\"this is not printed\");\n" +
				"notascript?;\n" +
				";;\nexit";

		GroovyCLI groovyCLI = new GroovyCLI(new Binding(), new ByteArrayInputStream(script.getBytes()), System.out);
		groovyCLI.runGroovyConsole();

		Assert.assertEquals("The script output was not as expected!", "", groovyCLI.getScriptOutputStreamAll().toString());
	}

	@Test
	public void scriptNullPointerException() {
		String script = "out.println(\"this is printed\");\n" +
				"List a = null;\n" +
				"a.remove(1);\n" +
				";;\nexit";

		GroovyCLI groovyCLI = new GroovyCLI(new Binding(), new ByteArrayInputStream(script.getBytes()), System.out);
		groovyCLI.runGroovyConsole();

		Assert.assertEquals("The script output was not as expected!", "this is printed", groovyCLI.getScriptOutputStreamAll().toString().trim());
	}

	@Test
	public void differentEndOfScriptCommand() {
		String script = "out.println(\"test\");\n" +
				"EOS;\nexit";

		GroovyCLI groovyCLI = new GroovyCLI(new Binding(), new ByteArrayInputStream(script.getBytes()), System.out);
		groovyCLI.setEndOfScript("EOS;");
		groovyCLI.runGroovyConsole();

		Assert.assertEquals("The script output was not as expected!", "test", groovyCLI.getScriptOutputStreamAll().toString().trim());
	}

	@Test
	public void differentExitCommand() {
		String script = "out.println(\"test\");\n" +
				";;\nquit";

		GroovyCLI groovyCLI = new GroovyCLI(new Binding(), new ByteArrayInputStream(script.getBytes()), System.out);
		groovyCLI.setExit("quit");
		groovyCLI.runGroovyConsole();

		Assert.assertEquals("The script output was not as expected!", "test", groovyCLI.getScriptOutputStreamAll().toString().trim());
	}

	@Test
	public void outputPrintStreamAllVsCurrent() {
		String script = "out.println(\"test\");\n" +
				";;\n" +
				"out.println(\"test2\");\n" +
				";;\nexit";

		GroovyCLI groovyCLI = new GroovyCLI(new Binding(), new ByteArrayInputStream(script.getBytes()), System.out);
		groovyCLI.runGroovyConsole();

		Assert.assertEquals("The script output was not as expected!", "test\ntest2", groovyCLI.getScriptOutputStreamAll().toString().trim());
		Assert.assertEquals("The script output was not as expected!", "test2", groovyCLI.getScriptOutputStreamCurrent().toString().trim());
	}

	@Test
	public void returnObject() {
		TestService testService = new TestService("groovy cli");

		String script = "testService.hello();\n" +
				";;\nexit";

		GroovyCLI groovyCLI = new GroovyCLI(new Binding(), new ByteArrayInputStream(script.getBytes()), System.out);
		groovyCLI.setVariable("testService", testService);
		groovyCLI.runGroovyConsole();

		Assert.assertEquals("The returned object was not as expected!", testService.hello(), groovyCLI.getCurrentResult());
	}

	private static class TestService {
		private String name;

		public TestService(String name) {
			this.name = name;
		}

		public String hello() {
			return "Hello " + name + "!";
		}
	}
}

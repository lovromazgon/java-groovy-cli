package org.codehaus.groovy.cli;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;

/**
 * A command line interface, which allows the user to execute scripts until he inputs the exit command.
 *
 * Example usage:
 * <code>
 * GroovyCLI groovyCLI = new GroovyCLI();
 * groovyCLI.setVariable("myService", new MyService()); //set bindings
 * groovyCLI.runGroovyConsole();
 * </code>
 *
 * @author lovro
 */
public class GroovyCLI {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_BLUE = "\u001B[34m";

	private static final Logger log = LoggerFactory.getLogger(GroovyCLI.class);
	private static final String OUTPUT_VARIABLE_NAME = "out";
	private static final String END_OF_SCRIPT_DEFAULT = ";;";
	private static final String EXIT_DEFAULT = "exit";
	private static final InputStream INPUT_STREAM_DEFAULT = System.in;
	private static final OutputStream OUTPUT_STREAM_DEFAULT = System.out;

	private final GroovyClassLoader groovyClassLoader;
	private final Binding binding;
	private InputStream inputStream;
	private OutputStream outputStream;
	private OutputStream scriptOutputStream;
	private String endOfScript;
	private String exit;
	private boolean colors;

	/**
	 * Creates the GroovyCLI object, with the default settings
	 */
	public GroovyCLI() {
		this(new Binding());
	}

	/**
	 * Creates the GroovyCLI object, with the supplied parameters
	 *
	 * @param binding variable bindings
	 */
	public GroovyCLI(Binding binding) {
		this(binding, INPUT_STREAM_DEFAULT, OUTPUT_STREAM_DEFAULT);
	}

	/**
	 * Creates the GroovyCLI object, with the supplied parameters
	 *
	 * @param binding variable bindings
	 * @param inputStream the stream, from where the input will be taken
	 * @param outputStream the stream, to which the output of the script will be written
	 */
	public GroovyCLI(Binding binding, InputStream inputStream, OutputStream outputStream) {
		this(binding, inputStream, outputStream, END_OF_SCRIPT_DEFAULT, EXIT_DEFAULT);
	}

	/**
	 * Creates the GroovyCLI object, with the supplied parameters
	 *
	 * @param binding variable bindings
	 * @param inputStream the stream, from where the input will be taken
	 * @param outputStream the stream, to which the output of the script will be written
	 * @param endOfScript when this string is entered, all previous lines will be executed
	 * @param exit when this string is entered, the CLI will exit
	 */
	public GroovyCLI(Binding binding, InputStream inputStream, OutputStream outputStream, String endOfScript, String exit) {
		this.groovyClassLoader = new GroovyClassLoader();
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.binding = binding;
		this.endOfScript = endOfScript;
		this.exit = exit;
		scriptOutputStream = new ByteArrayOutputStream();
		colors = true;
	}

	/**
	 * The main method, which runs a loop and reads from the input stream until a previously defined termination string is detected
	 */
	public void runGroovyConsole() {
		boolean run = true;
		StringBuilder scriptSb = new StringBuilder();
		Scanner scanner = new Scanner(inputStream);
		PrintStream out = new PrintStream(outputStream);
		binding.setVariable(OUTPUT_VARIABLE_NAME, new PrintStream(scriptOutputStream));

		if (colors) out.print(ANSI_BLUE);
		out.println("--- Groovy script CLI ---");
		printInstructions(out);
		out.println();
		printBindings(out);
		out.println();
		if (colors) out.print(ANSI_RESET);
		do {
			String line = scanner.nextLine();

			if (line.equals(endOfScript)) {
				//execute script
				out.println("Executing script...");
				try {
					String result = executeGroovyScript(scriptSb.toString());
					scriptSb.delete(0, scriptSb.length());

					out.println("Script output:");

					if (colors)	out.print(ANSI_BLUE);
					out.println(result);
					if (colors)	out.print(ANSI_RESET);
				} catch (Exception e) {
					out.println("Exception while executing script:");
					if (colors)	out.print(ANSI_RED);
					e.printStackTrace(out);
					if (colors)	out.print(ANSI_RESET);
				}
				out.println("--------------");
				out.println("Write another script:");
			} else if (line.equals(exit)) {
				//exit loop
				out.println("Bye!");
				run = false;
			} else {
				//regular script line
				scriptSb.append(line);
				scriptSb.append("\n");
			}
		} while(run);
	}

	private void printInstructions(PrintStream out) {
		out.println("Write a groovy script, which you want to execute.");
		out.println("After you are done, write the command for \"end of script\" and the script will be executed.");
		out.println("If you want to exit the CLI, write the command for \"exit\".");
		out.println("");
		out.println("Special commands:");
		out.println("end of script - " + endOfScript);
		out.println("exit - " + exit);
	}

	private void printBindings(PrintStream out) {
		out.println("Bindings (variable name - object class):");
		Map variables = binding.getVariables();
		for (Object key : variables.keySet()) {
			out.println(key + " - " + variables.get(key));
		}
	}

	private String executeGroovyScript(String code) {
		log.debug("About to execute script:\n{}", code);
		GroovyShell shell = new GroovyShell(groovyClassLoader, binding);
		shell.evaluate(code);
		return scriptOutputStream.toString().trim();
	}

	/*
	 * Colors
	 */
	public void enableColors() {
		colors = true;
	}
	public void disableColors() {
		colors = false;
	}

	/*
	 * Getters and setters
	 */
	public Binding getBinding() {
		return binding;
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	public OutputStream getOutputStream() {
		return outputStream;
	}
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	public String getEndOfScript() {
		return endOfScript;
	}
	public void setEndOfScript(String endOfScript) {
		this.endOfScript = endOfScript;
	}
	public String getExit() {
		return exit;
	}
	public void setExit(String exit) {
		this.exit = exit;
	}
	public OutputStream getScriptOutputStream() {
		return scriptOutputStream;
	}

	/*
	 * Delegated methods
	 */
	public Object getVariable(String name) {
		return binding.getVariable(name);
	}
	public void setVariable(String name, Object value) {
		binding.setVariable(name, value);
	}
	public boolean hasVariable(String name) {
		return binding.hasVariable(name);
	}
	public Map getVariables() {
		return binding.getVariables();
	}
	public Object getProperty(String property) {
		return binding.getProperty(property);
	}
	public void setProperty(String property, Object newValue) {
		binding.setProperty(property, newValue);
	}
}

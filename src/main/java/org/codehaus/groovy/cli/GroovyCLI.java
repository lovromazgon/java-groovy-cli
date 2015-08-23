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
import java.util.HashMap;
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
	private static final String STORE_VARIABLE_NAME = "store";
	private static final String END_OF_SCRIPT_DEFAULT = ";;";
	private static final String EXIT_DEFAULT = "exit";
	private static final InputStream INPUT_STREAM_DEFAULT = System.in;
	private static final OutputStream OUTPUT_STREAM_DEFAULT = System.out;

	private final GroovyClassLoader groovyClassLoader;
	private final Binding binding;
	private final Map<Object, Object> store;
	private final OutputStream scriptOutputStreamAll;
	private OutputStream scriptOutputStreamCurrent;
	private InputStream cliInputStream;
	private OutputStream cliOutputStream;
	private Object currentResult;
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
	 * @param cliInputStream the stream, from where the input will be taken
	 * @param cliOutputStream the stream, to which the output of the script will be written
	 */
	public GroovyCLI(Binding binding, InputStream cliInputStream, OutputStream cliOutputStream) {
		this(binding, cliInputStream, cliOutputStream, END_OF_SCRIPT_DEFAULT, EXIT_DEFAULT);
	}

	/**
	 * Creates the GroovyCLI object, with the supplied parameters
	 *
	 * @param binding variable bindings
	 * @param cliInputStream the stream, from where the input will be taken
	 * @param cliOutputStream the stream, to which the output of the script will be written
	 * @param endOfScript when this string is entered, all previous lines will be executed
	 * @param exit when this string is entered, the CLI will exit
	 */
	public GroovyCLI(Binding binding, InputStream cliInputStream, OutputStream cliOutputStream, String endOfScript, String exit) {
		this.groovyClassLoader = new GroovyClassLoader();
		this.cliInputStream = cliInputStream;
		this.cliOutputStream = cliOutputStream;
		this.binding = binding;
		this.endOfScript = endOfScript;
		this.exit = exit;
		scriptOutputStreamAll = new ByteArrayOutputStream();
		store = new HashMap<>();
		colors = true;
	}

	/**
	 * The main method, which runs a loop and reads from the input stream until a previously defined termination string is detected
	 */
	public void runGroovyConsole() {
		boolean run = true;
		StringBuilder scriptSb = new StringBuilder();
		Scanner scanner = new Scanner(cliInputStream);
		PrintStream out = new PrintStream(cliOutputStream);
		PrintStream scriptOutputPrintStream = new PrintStream(scriptOutputStreamAll);

		binding.setVariable(STORE_VARIABLE_NAME, store);

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
					currentResult = executeGroovyScript(scriptSb.toString());
					scriptSb.delete(0, scriptSb.length());

					out.println("Script output:");
					if (colors)	out.print(ANSI_BLUE);
					out.println(scriptOutputStreamCurrent.toString().trim());
					if (colors)	out.print(ANSI_RESET);

					out.println("Script returned:");
					if (colors)	out.print(ANSI_BLUE);
					out.println(currentResult);
					if (colors)	out.print(ANSI_RESET);
				} catch (Exception e) {
					out.println("Exception while executing script:");
					if (colors)	out.print(ANSI_RED);
					e.printStackTrace(out);
					if (colors)	out.print(ANSI_RESET);
				}

				scriptOutputPrintStream.print(scriptOutputStreamCurrent.toString());
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

	private Object executeGroovyScript(String code) {
		log.debug("About to execute script:\n{}", code);

		scriptOutputStreamCurrent = new ByteArrayOutputStream();
		binding.setVariable(OUTPUT_VARIABLE_NAME, new PrintStream(scriptOutputStreamCurrent));

		GroovyShell shell = new GroovyShell(groovyClassLoader, binding);
		return shell.evaluate(code);
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
	public InputStream getCliInputStream() {
		return cliInputStream;
	}
	public void setCliInputStream(InputStream cliInputStream) {
		this.cliInputStream = cliInputStream;
	}
	public OutputStream getCliOutputStream() {
		return cliOutputStream;
	}
	public void setCliOutputStream(OutputStream cliOutputStream) {
		this.cliOutputStream = cliOutputStream;
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
	public OutputStream getScriptOutputStreamAll() {
		return scriptOutputStreamAll;
	}
	public OutputStream getScriptOutputStreamCurrent() {
		return scriptOutputStreamCurrent;
	}
	public Object getCurrentResult() {
		return currentResult;
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

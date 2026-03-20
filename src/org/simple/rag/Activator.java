package org.simple.rag;

/**
 * The activator class - standalone version without Eclipse/OSGi dependencies
 */
public class Activator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.simple.rag"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private Logger logger;
	
	public Activator() {
		this.logger = Logger.getInstance();
	}

	public static Activator getDefault() {
		if (plugin == null) {
			plugin = new Activator();
		}
		return plugin;
	}
	
	public Logger getLog() {
		return logger;
	}
}

package org.simple.rag.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import java.util.concurrent.Callable;

import org.simple.rag.Logger;
import org.simple.rag.cli.commands.InitCommand;
import org.simple.rag.cli.commands.IngestCommand;
import org.simple.rag.cli.commands.AskCommand;
import org.simple.rag.cli.commands.InfoCommand;
import org.simple.rag.cli.commands.ConfigCommand;

/**
 * Main CLI entry point for the Simple RAG Application.
 * Provides a command-line interface for initialization, ingestion, and querying.
 */
@Command(
    name = "rag",
    version = "1.0.0",
    description = "Simple RAG - Retrieval-Augmented Generation for Document Q&A",
    subcommands = {
        InitCommand.class,
        IngestCommand.class,
        AskCommand.class,
        ConfigCommand.class,
        InfoCommand.class,
        HelpCommand.class
    }
)
public class RagCLI implements Callable<Integer> {
    
    private static final Logger logger = Logger.getInstance();
    
    public static void main(String[] args) {
        try {
            // Initialize application directories and configs if needed
            FolderManager.initializeDirectories();
            
            // Create and execute CLI
            CommandLine cmd = new CommandLine(new RagCLI());
            
            int exitCode;
            if (args.length == 0) {
                // Show info if no commands provided
                cmd.execute("info");
                exitCode = 0;
            } else {
                exitCode = cmd.execute(args);
            }
            
            System.exit(exitCode);
            
        } catch (Exception e) {
            Logger.getInstance().error("Fatal error: " + e.getMessage(), e);
            System.exit(1);
        }
    }
    
    @Override
    public Integer call() throws Exception {
        // This is called when no subcommand is provided
        Logger.getInstance().info("Use 'rag --help' for usage information");
        return 0;
    }
}

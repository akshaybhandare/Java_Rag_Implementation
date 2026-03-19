package org.simple.rag.ingestion;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.simple.rag.GlobalConstants;

public interface IIngestFileProcessor {
	
	public default List<Path> getIngestFiles(String ext) throws Exception {
		Set<Path> files = new HashSet<>();
		Path start = Paths.get(GlobalConstants.KNOWLEDGE_BASE_DIR);
		try (Stream<Path> stream = Files.walk(start)) {
		    stream.filter(p -> p.toString().endsWith(ext))
		          .forEach(files::add);
		}
		
		return files.isEmpty() ? Collections.emptyList() : List.copyOf(files);
	}

	public void processFile() throws Exception;
}

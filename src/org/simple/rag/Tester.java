package org.simple.rag;

import org.simple.rag.ingestion.PDFFileIngester;

/**
 * Simple test class for PDF file ingestion.
 * Processes PDF files from the knowledge base directory.
 */
public class Tester {

	public static void main(String[] args) throws Exception {
		PDFFileIngester ingest = new PDFFileIngester();
		ingest.processFile();
	}
}

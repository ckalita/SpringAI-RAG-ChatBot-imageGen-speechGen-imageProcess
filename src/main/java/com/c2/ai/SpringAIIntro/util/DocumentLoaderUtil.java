package com.c2.ai.SpringAIIntro.util;

import org.springframework.ai.document.Document;
//import org.springframework.ai.reader.markdown.MarkdownLoader;
import org.springframework.ai.reader.tika.TikaDocumentReader;

import java.io.File;
import java.nio.file.*;
import java.util.*;

public class DocumentLoaderUtil {

    public List<Document> loadAllDocuments(String folderPath) throws Exception {
        List<Document> allDocs = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folderPath))) {
            for (Path path : stream) {
                File file = path.toFile();

                if (!file.isFile()) continue;

                String name = file.getName().toLowerCase();

                if (name.endsWith(".md")) {
                    /*MarkdownDocumentReader reader = new MarkdownDocumentReader(file);
                    allDocs.addAll(reader.get());*/
                } else if (name.endsWith(".pdf") || name.endsWith(".docx") ||
                        name.endsWith(".pptx") || name.endsWith(".txt")) {
                    /*TikaDocumentReader tikaReader = new TikaDocumentReader(file);
                    allDocs.addAll(tikaReader.get());*/
                } else {
                    System.out.println("Skipping unsupported file: " + name);
                }
            }
        }
        return allDocs;
    }
}

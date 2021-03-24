package com.riguz.tool;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HexoExporter {
    public static final Namespace nsp = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");

    public static void main(String[] args) throws IOException {
        Path blogPath = Paths.get(
                "/Users/pwc/Documents/PROJECTS/blog/source/_posts/");
        List<Path> files = listFiles(blogPath);


        Document document = new Document();
        Element root = createXmlRoot();
        document.setRootElement(root);
        files.forEach(path -> {
            System.out.println("Processing:" + path.getFileName());
            HexoParser parser = null;
            try {
                parser = new HexoParser(Files.readAllLines(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Post parsed = parser.parse();
            Element page = parsed.toXml(root);
            root.addContent(page);
        });

        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        out.output(document, new FileWriter("/Users/pwc/Documents/blogs.xml"));

        System.out.println("Done!");
    }

    private static Element createXmlRoot() {
        Element root = new Element("mediawiki");
        root.setNamespace(Namespace.getNamespace("http://www.mediawiki.org/xml/export-0.11/"));
        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.addNamespaceDeclaration(xsi);
        root.setAttribute("schemaLocation",
                "http://www.mediawiki.org/xml/export-0.11/ http://www.mediawiki.org/xml/export-0.11.xsd",
                xsi);
        root.setAttribute("version", "0.11");

        root.setAttribute("lang", "en", nsp);

        return root;
    }

    public static List<Path> listFiles(Path path) throws IOException {

        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk.filter(Files::isRegularFile)
                    .filter(p -> p.toFile().getName().endsWith("md"))
                    .collect(Collectors.toList());
        }
        return result;
    }
}

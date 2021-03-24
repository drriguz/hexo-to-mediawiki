package com.riguz.tool;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexoParser {
    private final List<String> lines;

    public HexoParser(List<String> lines) {
        this.lines = Collections.unmodifiableList(lines);
    }

    private int findLine(int start, String prefix) {
        for (int i = start; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith(prefix)) {
                return i;
            }
        }
        return -1;
    }

    public Post parse() {
        Header header = null;

        int headerStart = findLine(0, "---");
        if (headerStart < 0)
            throw new RuntimeException("Unable to get header start");
        int headerEnd = findLine(headerStart + 1, "---");
        if (headerStart < 0)
            throw new RuntimeException("Unable to get header start");
        header = parseHeader(headerStart + 1, headerEnd);

        int contentStart = headerEnd + 1;
        String postAbstract = null;
        List<String> content = new ArrayList<>();
        for (int i = contentStart; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.trim().matches("<!--\\s*more\\s*-->")) {
                postAbstract = parseAbstract(contentStart, i);
            } else
                content.add(processLine(line));
        }
        return new Post(1, 1, header, postAbstract,
                String.join("\n", content));
    }

    private Header parseHeader(int start, int end) {
        Yaml yaml = new Yaml(new Constructor(Header.class));
        String headerStr = String.join("\n", lines.subList(start, end))
                .replaceAll("\\t", " ");
        return yaml.load(headerStr);
    }

    private String parseAbstract(int start, int end) {
        return String.join("\n", lines.subList(start, end));
    }

    /*
        0: no code yet
        1: ```lang
        2: pre
     */
    private int codeBlockState = 0;

    static final List<String> supportedLangs = Arrays.asList("java",
            "python", "scala", "javascript", "bash", "php", "tex",
            "latex", "lua", "properties", "ini",
            "yaml", "sql", "mysql", "json", "groovy", "scheme", "log",
            "c", "c++", "c#", "rust", "antlr", "ruby",
            "makefile", "xml", "dart", "swift", "cmake");


    private String processLine(String content) {
        if (codeBlockState == 0) {
            content = content.replaceAll("!\\[([^\\]]*)\\]\\(([^)]+)\\)", "[[File:$2|600px|$1]]");
            content = content.replaceAll("\\[([^\\]]*)\\]\\(([^)]+)\\)", "[$2 $1]");

            // fix relative link
            content = content.replaceAll("\\[\\[File:/images/", "\\[\\[File:");
        }

        if (content.trim().matches("```.+")) {
            if (codeBlockState > 0)
                throw new IllegalArgumentException("Code open multiple times:" + content);
            String lang = content.substring(3);
            if (!supportedLangs.contains(lang))
                throw new IllegalArgumentException("Unsupported lang:" + lang);
            content = String.format("<syntaxhighlight lang=\"%s\">", lang);
            codeBlockState = 1;
        } else if (content.trim().matches("```")) {
            if (codeBlockState == 0) {
                codeBlockState = 2;
                content = "<pre>";
            } else if (codeBlockState == 1) {
                codeBlockState = 0;
                content = "</syntaxhighlight>";
            } else if (codeBlockState == 2) {
                codeBlockState = 0;
                content = "</pre>";
            }
        } else if (content.trim().startsWith("#")) {
            if (codeBlockState == 0) {
                // not a comment
                String title = content.trim();
                int last = title.lastIndexOf('#');
                String prefix = title.substring(0, last + 1).replaceAll("#", "=");
                String realTitle = title.substring(last + 1);
                content = prefix + realTitle + prefix;
            }
        }
        return content;
    }
}

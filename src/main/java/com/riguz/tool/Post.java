package com.riguz.tool;

import com.google.common.hash.Hashing;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static com.riguz.tool.HexoExporter.nsp;

public class Post {
    private final int id;
    private final int revisionId;
    private final Header header;
    private final String postAbstract;
    private final String content;

    public Post(int id, int revisionId, Header header, String postAbstract, String content) {
        this.id = id;
        this.revisionId = revisionId;
        this.header = header;
        this.postAbstract = postAbstract;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public Header getHeader() {
        return header;
    }

    public String getPostAbstract() {
        return postAbstract;
    }

    public Element toXml(Element root) {
        return createPage(root);
    }

    private static final int BLOG_NAMESPACE = 3000;

    private Element createPage(Element root) {
        Element element = new Element("page", root.getNamespace());
        element.addContent(new Element("title", root.getNamespace())
                .setText("Blog:" + header.getTitle()));
        element.addContent(new Element("ns", root.getNamespace()).setText(String.valueOf(BLOG_NAMESPACE)));
        element.addContent(new Element("id", root.getNamespace()).setText(String.valueOf(id)));
        element.addContent(createReversion(root));
        return element;
    }

    private String formatDate() {
        if (header.getDate().length() != 10)
            throw new IllegalArgumentException("Wrong format of date:" + header.getDate());
        return header.getDate() + "T00:00:00Z";
    }

    private Element createReversion(Element root) {
        Element element = new Element("revision", root.getNamespace());
        element.addContent(new Element("id", root.getNamespace())
                .setText(String.valueOf(revisionId)));
        element.addContent(new Element("timestamp", root.getNamespace())
                .setText(formatDate()));
        element.addContent(
                new Element("contributor", root.getNamespace())
                        .addContent(new Element("username", root.getNamespace()).setText("Riguz"))
                        .addContent(new Element("id", root.getNamespace()).setText("1"))
        );
        element.addContent(new Element("comment", root.getNamespace())
                .setText(postAbstract));
        element.addContent(new Element("origin", root.getNamespace())
                .setText(String.valueOf(revisionId)));
        element.addContent(new Element("model", root.getNamespace())
                .setText("wikitext"));
        element.addContent(new Element("format", root.getNamespace())
                .setText("text/x-wiki"));
        element.addContent(new Element("text", root.getNamespace())
                .setAttribute("space", "preserve", nsp)
                .setText(content)
                .setAttribute("bytes", String.valueOf(getBytes()))
                .setAttribute("sha1", computeSha1()));
        element.addContent(new Element("sha1", root.getNamespace())
                .setText(computeSha1()));
        return element;
    }

    private int getBytes() {
        return content.getBytes(StandardCharsets.UTF_8).length;
    }

    private String computeSha1() {
        return new BigInteger(1, Hashing.sha1()
                .hashString(content, StandardCharsets.UTF_8).asBytes()).toString(36);
    }
}

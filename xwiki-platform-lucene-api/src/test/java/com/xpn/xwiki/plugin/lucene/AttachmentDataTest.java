/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.plugin.lucene;

import java.io.IOException;

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.display.internal.DisplayConfiguration;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.syntax.Syntax;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.internal.AttachmentData;
import com.xpn.xwiki.test.AbstractBridgedComponentTestCase;

/**
 * Unit tests for {@link AttachmentData}.
 * 
 * @version $Id$
 */
public class AttachmentDataTest extends AbstractBridgedComponentTestCase
{
    private XWikiDocument document;

    private XWikiAttachment attachment;

    private AttachmentData attachmentData;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        this.document = new XWikiDocument(new DocumentReference("wiki", "space", "page"));
        this.document.setSyntax(Syntax.XWIKI_2_0);
        this.attachment = new XWikiAttachment(this.document, "filename");
        this.document.getAttachmentList().add(this.attachment);

        this.attachmentData = new AttachmentData(this.attachment, getContext(), false);
    }

    @Override
    protected void registerComponents() throws Exception
    {
        super.registerComponents();

        // Setup display configuration.
        final DisplayConfiguration mockDisplayConfiguration = registerMockComponent(DisplayConfiguration.class);
        getMockery().checking(new Expectations()
        {
            {
                allowing(mockDisplayConfiguration).getDocumentDisplayerHint();
                will(returnValue("default"));
                allowing(mockDisplayConfiguration).getTitleHeadingDepth();
                will(returnValue(2));
            }
        });
    }

    private void assertGetFullText(String expect, String filename) throws IOException
    {
        this.attachment.setFilename(filename);
        this.attachment.setContent(getClass().getResourceAsStream("/" + filename));

        this.attachmentData.setFilename(filename);

        String fullText = this.attachmentData.getFullText(this.document, getContext());

        Assert.assertEquals("Wrong attachment content indexed", expect, fullText);
    }

    private void assertGetMimeType(String expect, String filename)
    {

        this.attachmentData.setMimeType(this.attachment.getMimeType(getContext()));
        String mimeType = this.attachmentData.getMimeType();
        Assert.assertEquals("Wrong mimetype content indexed", expect, mimeType);

    }

    @Test
    public void getFullTextFromTxt() throws IOException, XWikiException
    {
        assertGetFullText("text content\n", "txt.txt");
        assertGetMimeType("text/plain", "txt.txt");
    }

    @Test
    public void getFullTextFromMSOffice97() throws IOException, XWikiException
    {
        assertGetFullText("ms office 97 content\n\n", "msoffice97.doc");
        assertGetMimeType("application/msword", "msoffice97.doc");

    }

    @Test
    public void getFullTextFromOpenXML() throws IOException, XWikiException
    {
        assertGetFullText("openxml content\n", "openxml.docx");
        assertGetMimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "openxml.docx");

    }

    @Test
    public void getFullTextFromOpenDocument() throws IOException, XWikiException
    {
        assertGetFullText("opendocument content\n", "opendocument.odt");
        assertGetMimeType("application/vnd.oasis.opendocument.text", "opendocument.odt");
    }

    @Test
    public void getFullTextFromPDF() throws IOException, XWikiException
    {
        assertGetFullText("\npdf content\n\n\n", "pdf.pdf");
        assertGetMimeType("application/pdf", "pdf.pdf");
    }

    @Test
    public void getFullTextFromZIP() throws IOException, XWikiException
    {
        assertGetFullText("\nzip.txt\nzip content\n\n\n\n", "zip.zip");
        assertGetMimeType("application/zip", "zip.zip");
    }

    @Test
    public void getFullTextFromHTML() throws IOException, XWikiException
    {
        assertGetFullText("something\n", "html.html");
        assertGetMimeType("text/html", "html.html");
    }

    @Test
    public void getFullTextFromClass() throws IOException, XWikiException
    {
        String expectedContent =
            "public synchronized class helloworld {\n" + "    public void helloworld();\n"
                + "    public static void main(string[]);\n" + "}\n\n";
        assertGetFullText(expectedContent, "HelloWorld.class");
        assertGetMimeType("application/java-vm", "HelloWorld.class");
    }
}

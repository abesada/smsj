/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is "SMS Library for the Java platform".
 *
 * The Initial Developer of the Original Code is Markus Eriksson.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.marre.wap.wbxml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.marre.util.StringUtil;
import org.marre.wap.WapConstants;
import org.marre.wap.WspUtil;
import org.marre.xml.XmlAttribute;
import org.marre.xml.XmlWriter;

public class WbxmlWriter implements XmlWriter
{
    private Map myStringTable;
    private ByteArrayOutputStream myStringTableBuf;

    private OutputStream myOs;
    private ByteArrayOutputStream myWbxmlBody;

    private String[] myTagTokens;
    private String[] myAttrStartTokens;
    private String[] myAttrValueTokens;

    private String myPublicID;
    
    public WbxmlWriter(OutputStream os, String[] theTagTokens, String[] theAttrStrartTokens, String[] theAttrValueTokens)
    {
        myWbxmlBody = new ByteArrayOutputStream();
        myStringTableBuf = new ByteArrayOutputStream();
        myStringTable = new HashMap();
        myOs = os;

        setTagTokens(theTagTokens);
        setAttrStartTokens(theAttrStrartTokens);
        setAttrValueTokens(theAttrValueTokens);
    }

    public WbxmlWriter(OutputStream os)
    {
        this(os, null, null, null);
    }
    
    /**
     * Writes the wbxml to stream.
     * 
     * @throws IOException
     */
    public void flush() throws IOException
    {
        // WBXML v 0.1
        WspUtil.writeUint8(myOs, 0x01);
        // Public ID
        writePublicIdentifier(myOs, myPublicID);
        // Charset - "UTF-8"
        WspUtil.writeUintvar(myOs, WapConstants.MIB_ENUM_UTF_8);
        // String table
        writeStringTable(myOs);

        // Write body
        myWbxmlBody.close();
        myWbxmlBody.writeTo(myOs);

        myOs.flush();
    }

    /////// XmlWriter

    public void setDoctype(String name, String systemURI)
    {
        myPublicID = null; //Liquidterm: Defaults to unknown
    }

    public void setDoctype(String name, String publicID, String publicURI)
    {
        myPublicID = publicID;
    }

    public void setDoctype(String publicID)
    {
        myPublicID = publicID;
    }

    public void addStartElement(String tag) throws IOException
    {
        int tagIndex = StringUtil.findString(myTagTokens, tag);
        if (tagIndex >= 0)
        {
            // Known tag
            tagIndex += 0x05; // Tag token table starts at #5
            myWbxmlBody.write(WbxmlConstants.TOKEN_KNOWN_C | tagIndex);
        }
        else
        {
            // Unknown. Add as literal
            myWbxmlBody.write(WbxmlConstants.TOKEN_LITERAL_C);
            writeStrT(myWbxmlBody, tag);
        }
    }

    public void addStartElement(String tag, XmlAttribute[] attribs) throws IOException
    {
        int tagIndex = StringUtil.findString(myTagTokens, tag);
        if (tagIndex >= 0)
        {
            // Known tag
            tagIndex += 0x05; // Tag token table starts at #5
            myWbxmlBody.write(WbxmlConstants.TOKEN_KNOWN_AC | tagIndex);
        }
        else if (tag != null)
        {
            // Unknown. Add as literal (Liquidterm: only if not null)
            myWbxmlBody.write(WbxmlConstants.TOKEN_LITERAL_AC);
            writeStrT(myWbxmlBody, tag);
        }

        // Write attributes
        writeAttributes(myWbxmlBody, attribs);
    }

    public void addEmptyElement(String tag) throws IOException
    {
        int tagIndex = StringUtil.findString(myTagTokens, tag);
        if (tagIndex >= 0)
        {
            // Known tag
            tagIndex += 0x05; // Tag token table starts at #5
            myWbxmlBody.write(WbxmlConstants.TOKEN_KNOWN | tagIndex);
        }
        else if (tag != null)
        {
            // Unknown. Add as literal (Liquidterm: if not null)
            myWbxmlBody.write(WbxmlConstants.TOKEN_LITERAL);
            writeStrT(myWbxmlBody, tag);
        }
    }

    public void addEmptyElement(String tag, XmlAttribute[] attribs) throws IOException
    {
        int tagIndex = StringUtil.findString(myTagTokens, tag);

        if (tagIndex >= 0)
        {
            // Known tag
            tagIndex += 0x05; // Tag token table starts at #5
            myWbxmlBody.write(WbxmlConstants.TOKEN_KNOWN_A | tagIndex);
        }
        else
        {
            // Unknown. Add as literal
            myWbxmlBody.write(WbxmlConstants.TOKEN_LITERAL_A);
            writeStrT(myWbxmlBody, tag);
        }

        // Add attributes
        writeAttributes(myWbxmlBody, attribs);
    }

    public void addEndElement() throws IOException
    {
        myWbxmlBody.write(WbxmlConstants.TOKEN_END);
    }

    public void addCharacters(char[] ch, int start, int length) throws IOException
    {
        addCharacters(new String(ch, start, length));
    }

    public void addCharacters(String str) throws IOException
    {
        myWbxmlBody.write(WbxmlConstants.TOKEN_STR_I);
        writeStrI(myWbxmlBody, str);
    }

    // WBXML specific stuff

    public void addOpaqueData(byte[] buff) throws IOException
    {
        addOpaqueData(buff, 0, buff.length);
    }

    public void addOpaqueData(byte[] buff, int off, int len) throws IOException
    {
        myWbxmlBody.write(WbxmlConstants.TOKEN_OPAQ);
        WspUtil.writeUintvar(myWbxmlBody, buff.length);
        myWbxmlBody.write(buff, off, len);
    }

    /**
     * Sets the tag tokens.
     * 
     * @param theTagTokens
     *            first element in this array defines tag #5
     */
    public void setTagTokens(String[] theTagTokens)
    {
        if (theTagTokens != null)
        {
            myTagTokens = new String[theTagTokens.length];
            System.arraycopy(theTagTokens, 0, myTagTokens, 0, theTagTokens.length);
        }
        else
        {
            myTagTokens = null;
        }
    }

    /**
     * Sets the attribute start tokens.
     * 
     * @param theAttrStrartTokens
     *            first element in this array defines attribute #85
     */
    public void setAttrStartTokens(String[] theAttrStrartTokens)
    {
        if (theAttrStrartTokens != null)
        {
            myAttrStartTokens = new String[theAttrStrartTokens.length];
            System.arraycopy(theAttrStrartTokens, 0, myAttrStartTokens, 0, theAttrStrartTokens.length);
        }
        else
        {
            myAttrStartTokens = null;
        }
    }

    /**
     * Sets the attribute value tokens.
     * 
     * @param theAttrValueTokens
     *            first element in this array defines attribute #05
     */
    public void setAttrValueTokens(String[] theAttrValueTokens)
    {
        if (theAttrValueTokens != null)
        {
            myAttrValueTokens = new String[theAttrValueTokens.length];
            System.arraycopy(theAttrValueTokens, 0, myAttrValueTokens, 0, theAttrValueTokens.length);
        }
        else
        {
            myAttrValueTokens = null;
        }
    }

    /////////////////////////////////////////////////////////

    private void writePublicIdentifier(OutputStream os, String publicID) throws IOException
    {
        if (publicID == null)
        {
            // "Unknown or missing public identifier."
            WspUtil.writeUintvar(os, 0x01);
        }
        else
        {
            int idx = StringUtil.findString(WbxmlConstants.KNOWN_PUBLIC_DOCTYPES, publicID);
            if (idx != -1)
            {
                // Known ID
                idx += 2; // Skip 0 and 1
                WspUtil.writeUintvar(os, idx);
            }
            else
            {
                // Unknown ID, add string
                WspUtil.writeUintvar(os, 0x00); // String reference following
                writeStrT(os, publicID);
            }
        }
    }

    private void writeStrI(OutputStream theOs, String str) throws IOException
    {
        //Liquidterm: protection against null values
        if (str != null)
        {
            theOs.write(str.getBytes("UTF-8"));
            theOs.write(0x00);
        }
    }

    private void writeStrT(OutputStream theOs, String str) throws IOException
    {
        Integer index = (Integer) myStringTable.get(str);

        if (index == null)
        {
            index = new Integer(myStringTableBuf.size());
            myStringTable.put(str, index);
            writeStrI(myStringTableBuf, str);
        }

        WspUtil.writeUintvar(theOs, index.intValue());
    }

    private void writeStringTable(OutputStream theOs) throws IOException
    {
        // Write length of string table
        WspUtil.writeUintvar(theOs, myStringTableBuf.size());
        // Write string table
        myStringTableBuf.writeTo(theOs);
    }

    // FIXME: Unsure how to do this stuff with the attributes
    // more efficient...
    private void writeAttributes(OutputStream os, XmlAttribute[] attribs) throws IOException
    {
        int idx;

        for (int i = 0; i < attribs.length; i++)
        {
            // TYPE=VALUE
            String typeValue = attribs[i].getType() + "=" + attribs[i].getValue();
            idx = StringUtil.findString(myAttrStartTokens, typeValue);
            if (idx >= 0)
            {
                // Found a matching type-value pair
                idx += 0x05; // Attr start token table starts at #5
                myWbxmlBody.write(idx);
            }
            else
            {
                // Try with separate type and values
                
                // TYPE
                idx = StringUtil.findString(myAttrStartTokens, attribs[i].getType());
                if (idx >= 0)
                {
                    idx += 0x05; // Attr start token table starts at #5
                    myWbxmlBody.write(idx);
                }
                else
                {
                    myWbxmlBody.write(WbxmlConstants.TOKEN_LITERAL);
                    writeStrT(myWbxmlBody, attribs[i].getType());
                }

                // VALUE
                String attrValue = attribs[i].getValue();
                if (attrValue != null && (!attrValue.equals("")))
                {
                    idx = StringUtil.findString(myAttrValueTokens, attrValue);
                    if (idx >= 0)
                    {
                        idx += 0x85; // Attr value token table starts at 85
                        myWbxmlBody.write(idx);
                    }
                    else
                    {
                        myWbxmlBody.write(WbxmlConstants.TOKEN_STR_I);
                        writeStrI(myWbxmlBody, attrValue);
                    }
                }
            }            
        }

        // End of attributes
        myWbxmlBody.write(WbxmlConstants.TOKEN_END);
    }

    /*
     * public static void main(String argv[]) throws Exception { XmlWriter
     * handler = new WbxmlWriter();
     * 
     * handler.addStartElement("element", new XmlAttribute[] { new
     * XmlAttribute("type1", "value1") }); handler.addCharacters("Some text");
     * handler.addEmptyElement("empty", new XmlAttribute[] { new
     * XmlAttribute("type2", "value2") }); handler.addStartElement("element",
     * new XmlAttribute[] { new XmlAttribute("type3", "value3"), new
     * XmlAttribute("type4", "value4") }); handler.addEmptyElement("empty", new
     * XmlAttribute[] { new XmlAttribute("type2", "value2") });
     * handler.addEmptyElement("empty", new XmlAttribute[] { new
     * XmlAttribute("type2", "value2") }); handler.addEndTag();
     * handler.addEndTag();
     * 
     * handler.writeTo(new FileOutputStream("demo.wbxml")); }
     */
}
package pw.itcircle.javaBeanFactory.tools;

import java.io.IOException;
import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class XmlTool {

	/**
	 * 创建一个空的dom4j的Document对象
	 * @return
	 */
	public static Document createDocument(){
		Document doc = DocumentHelper.createDocument();
		return doc;
	}
	
	public static Document createDocument(String rootElementName){
		Element rootElement = DocumentHelper.createElement(rootElementName);
		Document doc = DocumentHelper.createDocument(rootElement);
		return doc;
	}
	
	/**
	 * 把String类型的xml转换成Document对象
	 * @param xml
	 * @return
	 * @throws DocumentException 
	 */
	public static Document stringToDocument(String xml) throws DocumentException{
		Document document = DocumentHelper.parseText(xml);
		return document;
	}
	
	/**  
     * 格式化XML文档  
     *  
     * @param document xml文档  
     * @param charset    字符串的编码  
     * @param istrans    是否对属性和元素值进行转移  
     * @return 格式化后XML字符串  
     */   
    public static String formatXml(Document document, String charset, boolean istrans) {   
    	
        OutputFormat format = OutputFormat.createPrettyPrint();   
        format.setEncoding(charset);   
	    format.setIndentSize(2);    
	    format.setNewlines(true);   
	    format.setTrimText(false);   
	    format.setPadText(true);    
	    //以上4行用于处理base64图片编码以后放入xml时的回车变空格问题      
        StringWriter sw = new StringWriter();   
        XMLWriter xw = new XMLWriter(sw, format);   
        xw.setEscapeText(istrans);   
        try {   
                xw.write(document);   
                xw.flush();   
                xw.close();   
        } catch (IOException e) {   
                System.out.println("格式化XML文档发生异常，请检查！");   
                e.printStackTrace();   
        }   
        return sw.toString();   
	}   
}

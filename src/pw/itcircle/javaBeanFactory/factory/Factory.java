package pw.itcircle.javaBeanFactory.factory;

import org.dom4j.Document;

import pw.itcircle.javaBeanFactory.exception.NameConflictException;

public interface Factory {

	/**
	 * XML 生成 JavaBean
	 * @param document
	 * @param target
	 * @return
	 * @throws Exception
	 */
	public Object createObject(String document, Object target) throws Exception;
	
	/**
	 * XML 生成 JavaBean
	 * @param document
	 * @param target
	 * @return
	 * @throws Exception
	 */
	public Object createObject(Document document, Object target) throws Exception;
	
	/**
	 * JavaBean 生成  XML Dom4j
	 * @param input
	 * @param rootElementName
	 * @return
	 * @throws NameConflictException
	 */
	public Document getObjectDocument(Object input, String rootElementName) throws NameConflictException;
	
	/**
	 * JavaBean 生成  XML 字符串
	 * @param input
	 * @param rootElementName
	 * @return
	 * @throws NameConflictException
	 */
	public String getObjectString(Object input, String rootElementName) throws NameConflictException;
}

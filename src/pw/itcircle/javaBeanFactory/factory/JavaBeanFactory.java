package pw.itcircle.javaBeanFactory.factory;


import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import pw.itcircle.javaBeanFactory.exception.NameConflictException;
import pw.itcircle.javaBeanFactory.tools.StringUtil;
import pw.itcircle.javaBeanFactory.tools.XmlTool;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

public class JavaBeanFactory implements Factory
{
	private static JavaBeanFactory instance = null;
	private Object finalTargetObject = null;
	private Object fatherTargetObject = null;
	private Document returnDocument = null;
	private Element fatherElement = null;
	private final String DEFAULTROOTELEMENT = "Calculate-Result";
	private XPath xpathSelector;
	private static Gson gson;
	private String jsonMessage = "<![CDATA[json]]>";
	
	public static JavaBeanFactory newInstance()
	{
		if(instance == null)
		{
			instance = new JavaBeanFactory();
			gson = new Gson();
		}
		return instance;
	}

	private JavaBeanFactory(){};
	
	public Object createObject(Document document, Object target) throws Exception
	{
		Class<?> targetClass = Class.forName(getFullClassName(target));
		finalTargetObject = createObject(targetClass);
		
		assignmentValuesForObject(document.getRootElement().element(getClassName(target)), finalTargetObject);
		return finalTargetObject;
	}
	
	public Document getObjectDocument(Object input, String rootElementName) throws NameConflictException
	{
		if(StringUtil.haveContent(rootElementName) && getClassName(input.getClass()).equalsIgnoreCase(rootElementName))
		{
			throw new NameConflictException("root Element name:" + rootElementName + "is conflict with class name.");
		}
		createDocument(input, rootElementName);
		String xml = XmlTool.formatXml(returnDocument, "UTF-8", false);
		try {
			returnDocument = XmlTool.stringToDocument(xml);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return returnDocument;
	}
	
	private Document createDocument(Object input, String rootElementName)
	{
		String rootName = DEFAULTROOTELEMENT;
		if(StringUtil.haveContent(rootElementName))
		{
			rootName = rootElementName.trim();
			if(rootName.equalsIgnoreCase(input.getClass().getName()))
			{
				rootName = rootName + "_" + input.getClass().getName();
			}
		}
		
		Document doc = XmlTool.createDocument(rootName);
		returnDocument = doc;
		
		Element rootElement = doc.getRootElement();
		Element childElement = DocumentHelper.createElement(getClassName(input.getClass()));
		childElement.setParent(rootElement);
		childElement = rootElement.addElement(childElement.getQualifiedName(), childElement.getNamespaceURI());
		
		assignmentValuesForDocument(childElement, input);
		return doc;
	}
	
	public String getObjectString(Object input, String rootElementName) throws NameConflictException
	{
		return this.getObjectDocument(input, rootElementName).asXML();
	}
	
	private Object createObject(Class<?> targetClass) throws Exception
	{
		Object targetObject = null;
		if(targetClass != null)
		{
			targetObject = targetClass.newInstance();
		}
		return targetObject;
	}
	
	private void assignmentValuesForDocument(Element element, Object input)
	{
		xpathSelector = DocumentHelper.createXPath(element.getUniquePath());
		if(returnDocument != null)
		{
			Class<?> objectClass = input.getClass();
			Field[] fields = objectClass.getDeclaredFields();
			for(Field field : fields)
			{
				String getMethodName = getGetterMethodName(field);
				Method getMethod = null;
				Object value = null;
				try {
					getMethod = objectClass.getMethod(getMethodName);
					value = getMethod.invoke(input);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				String fillValue = null;
				if(value != null)
				{
					xpathSelector = DocumentHelper.createXPath(element.getUniquePath());
					fatherElement = (Element)xpathSelector.selectSingleNode(returnDocument);
					String elementName = field.getName();
					Element newElement = DocumentHelper.createElement(elementName);
					newElement = fatherElement.addElement(newElement.getQualifiedName(), newElement.getNamespaceURI());
					
					//简单类型字段
					if(judgeFieldType(field) == FieldType.SIMPLE_TYPE)
					{
						fillValue = value.toString();
						newElement.setText(fillValue);
					}
					//集合
					else if(judgeFieldType(field) == FieldType.COLLECTION_TYPE || judgeFieldType(field) == FieldType.ARRAY)
					{
						try {
							if(value != null)
							{
								fillValue = gson.toJson(value);
								newElement.setText(jsonMessage.replace("json", fillValue));
								break;
							}
						} catch (JsonSyntaxException e) {
							System.out.println("json转集合出错:" + e.getMessage());
						}
					}
					//复合类型
					else
					{
						assignmentValuesForDocument(newElement, value);
					}
				}
			}
		}
	}
			
	private void assignmentValuesForObject(Element element, Object outObject)
	{
		if(outObject != null)
		{
			Class<?> objectClass = outObject.getClass();
			Field[] fields = objectClass.getDeclaredFields();
			for(Field field : fields)
			{
				if(element.element(field.getName()) != null)//拿到这个字段并且XML中有对应的节点就进行赋值操作
				{
					String setMethodName = getSetterMethodName(field);
					//简单类型字段
					if(judgeFieldType(field) == FieldType.SIMPLE_TYPE)
					{
						try {
							Method setMethod = objectClass.getMethod(setMethodName, field.getType());
							Object value = getElementValue(element, field);
							if(value != null)
							{
								setMethod.invoke(outObject, value);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					//集合
					else if(judgeFieldType(field) == FieldType.COLLECTION_TYPE)
					{
						try {
							Object value = getElementValue(element, field);
							if(value != null)
							{
								fatherTargetObject = outObject;
								CollectionType collectionType = judgeCollectionType(field);
								
								//Use JsonReader.setLenient(true) to accept malformed JSON
								Reader reader = new StringReader(value.toString());
								JsonReader jr = new JsonReader(reader);
								jr.setLenient(true);
								JsonParser parser = new JsonParser();
								JsonElement je = parser.parse(jr);
								
								Type t = field.getGenericType();//获取泛型类型
								switch(collectionType)
								{
									case COLLECTION:
									case LIST:
										
										Collection<Object> collection = gson.fromJson(je, t);
										if(collection != null)
										{
											try {
												Method setMethod = objectClass.getMethod(setMethodName, field.getType());
												setMethod.invoke(fatherTargetObject, collection);
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
										break;
										
									case MAP:
										// TODO map是否也需要类似泛型类型获取操作 有待验证
										Map<Object, Object> map = gson.fromJson(je, t);
										if(map != null)
										{
											try {
												Method setMethod = objectClass.getMethod(setMethodName, field.getType());
												setMethod.invoke(fatherTargetObject, map);
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
										break;
								}
								
							}
						} catch (JsonSyntaxException e) {
							System.out.println("json转集合出错:" + e.getMessage());
						}
					}
					//数组
					else if(judgeFieldType(field) == FieldType.ARRAY)
					{
						try {
							Object value = getElementValue(element, field);
							if(value != null)
							{
								fatherTargetObject = outObject;
								
								//Use JsonReader.setLenient(true) to accept malformed JSON
								Reader reader = new StringReader(value.toString());
								JsonReader jr = new JsonReader(reader);
								jr.setLenient(true);
								JsonParser parser = new JsonParser();
								JsonElement je = parser.parse(jr);
								
								Type t = field.getGenericType();//获取泛型类型
								Object array = gson.fromJson(je, t);
								if(array != null)
								{
									try {
										Method setMethod = objectClass.getMethod(setMethodName, field.getType());
										setMethod.invoke(fatherTargetObject, array);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						} catch (JsonSyntaxException e) {
							System.out.println("json转集合出错:" + e.getMessage());
						}
					}
					//复合类型
					else
					{
						fatherTargetObject = outObject;
						Object newObject = null;
						try {
							newObject = createObject(field.getType());
							if(newObject != null)
							{
								Method setMethod = objectClass.getMethod(setMethodName, field.getType());
								setMethod.invoke(fatherTargetObject, newObject);
							}
							assignmentValuesForObject(element.element(field.getName()), newObject);
						} catch (Exception e) {
							try {
								if(newObject != null)
								{
									Method setMethod = objectClass.getMethod(setMethodName, field.getType());
									setMethod.invoke(fatherTargetObject, newObject);
								}
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
	
	private Object getElementValue(Element element, Field field)
	{
		Object value = null;
		Object valueType = field.getType();
		String xmlValue = textTrim(element.element(field.getName()));
		if(xmlValue != null)
		{
			// TODO 能否改为万能类型转换
			//FieldType.COLLECTION_TYPE集合类型用json传输，所以用String处理
			if (valueType.equals(String.class) || judgeFieldType(field) == FieldType.COLLECTION_TYPE || 
					judgeFieldType(field) == FieldType.ARRAY)
			{
				value = xmlValue;
				return value;
			}
			if (valueType.equals(Integer.class) || valueType.equals(int.class))
			{
				value = Integer.parseInt(xmlValue);
				return value;
			}
			if (valueType.equals(Float.class) || valueType.equals(float.class))
			{
				value = Float.parseFloat(xmlValue);
				return value;
			}
			if (valueType.equals(Long.class) || valueType.equals(long.class))
			{
				value = Long.valueOf(xmlValue);
				return value;
			}
			if (valueType.equals(Double.class) || valueType.equals(double.class))
			{
				value = Double.valueOf(xmlValue);
				return value;
			}
			if (valueType.equals(Boolean.class) || valueType.equals(boolean.class))
			{
				value = Boolean.parseBoolean(xmlValue);
				return value;
			}
		}
		return value;
	}
	
	private String textTrim(Element element)
	{
		String value = null;
		if(element != null)
		{
			value = element.getTextTrim();
		}
		return value;
	}
	
	//标准Java Bean获取字段的set方法名
	private String getSetterMethodName(Field field)
	{
		StringBuilder name = new StringBuilder("set");
		String name2 = field.getName();
		if(!Character.isUpperCase(name2.charAt(0)))
		{
			StringBuilder s = new StringBuilder();
			s.append(Character.toUpperCase(name2.charAt(0))).append(name2.substring(1)).toString();
			return name + s.toString();
		}
		
		return name + name2;
	}
	
	//标准Java Bean获取字段的get方法名
	private String getGetterMethodName(Field field)
	{
		StringBuilder name = new StringBuilder("get");
		String name2 = field.getName();
		if(!Character.isUpperCase(name2.charAt(0)))
		{
			StringBuilder s = new StringBuilder();
			s.append(Character.toUpperCase(name2.charAt(0))).append(name2.substring(1)).toString();
			return name + s.toString();
		}
		
		return name + name2;
	}
	
	private enum FieldType
	{
		SIMPLE_TYPE,COLLECTION_TYPE,COMPOUND_TYPE,ARRAY;
	}
	
	private enum CollectionType
	{
		COLLECTION,LIST,MAP,ARRAY//数组;
	}
	
	private String[] SIMPLETYPEARRAY = {"int;","Integer;","double;","String;","boolean;","byte;","shot;","long;","float;","char;","Date;"};//数组类型
	private String[] SIMPLETYPE = {"int","Integer","double","String","boolean","byte","shot","long","float","char","Date"};
	private String[] COLLECTIONYPE = {"ArrayList","List","Map","HashMap","AbstractCollection","ArrayList","Arrays","Collection"};
	private boolean include(String val, String ...vals)
	{
		boolean include = false;
		for(String s : vals)
		{
			if(s.equalsIgnoreCase(val))
			{
				include = true;
				return include;
			}
		}
		return include;
	}
	
	private FieldType judgeFieldType(Field field)
	{
		FieldType type = FieldType.COMPOUND_TYPE;
		if(include(field.getType().toString(), SIMPLETYPE) || include(getClassType(field.getType().toString()), SIMPLETYPE))
		{
			type = FieldType.SIMPLE_TYPE;
			return type;
		}
		if(include(field.getType().toString(), COLLECTIONYPE) || include(getClassType(field.getType().toString()), COLLECTIONYPE))
		{
			type = FieldType.COLLECTION_TYPE;
			return type;
		}
		if(include(field.getType().toString(), SIMPLETYPEARRAY) || include(getClassType(field.getType().toString()), SIMPLETYPEARRAY))
		{
			type = FieldType.ARRAY;
			return type;
		}
		return type;
	}
	
	private CollectionType judgeCollectionType(Field field)
	{
		CollectionType type = CollectionType.COLLECTION;
		if(include(field.getType().toString(), COLLECTIONYPE) || include(getClassType(field.getType().toString()), COLLECTIONYPE) || 
				include(field.getType().toString(), SIMPLETYPEARRAY) || include(getClassType(field.getType().toString()), SIMPLETYPEARRAY))
		{
			return type;
		}
		try {
			type = CollectionType.valueOf(getClassType(field.getType().toString()).toUpperCase());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("没有对应的" + getClassType(field.getType().toString()).toUpperCase() + "类型");
		}
		return type;
	}
	
	private String getClassType(String calssType)
	{
		String type = calssType;
		String[] temp = calssType.split("\\.");
		if(temp != null && temp.length > 0)
		{
			type = temp[temp.length - 1];
		}
		return type;
	}
	
	private String getFullClassName(Object target)
	{
		String name = null;
		String[] temp = target.toString().split(" ");
		if(temp != null && temp.length == 2)
		{
			name = temp[1];
		}
		return name;
	}
	
	private String getClassName(Object target)
	{
		String name = null;
		String[] temp = getFullClassName(target).split("\\.");
		if(temp != null && temp.length > 0)
		{
			name = temp[temp.length - 1];
		}
		return name;
	}
}

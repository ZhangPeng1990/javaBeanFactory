package pw.itcircle.javaBeanFactory.test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import pw.itcircle.javaBeanFactory.factory.JavaBeanFactory;
import pw.itcircle.javaBeanFactory.tools.XmlTool;

public class Tester {

	public static void main(String[] args) 
	{
		try {
			Document calDocument = getDocumentByURI("D:\\test.xml");
			
			Student s = new Student();
			s.setName("张鹏");
			s.setAge(24);
			s.setSex("男");
			
			List<Course> cs = new ArrayList<Course>();
			for(int i = 0; i < 15; i++)
			{
				Course c = new Course();
				c.setName("课程" + i);
				c.setTearcher("老师" + i);
				cs.add(c);
				if(i == 0)
				{
					s.setZhuxiu(c);
				}
			}
			s.setAllKecheng(cs);
			String stu1Xml = JavaBeanFactory.newInstance().getObjectString(s, "School");
			System.out.println(stu1Xml);
			Student s3 = (Student)JavaBeanFactory.newInstance().createObject(XmlTool.stringToDocument(stu1Xml), Student.class);
			System.out.println(s3);
	//		private Course zhuxiu;
	//		
	//		private List<Course> allKecheng;
			
			Student stu = null;
		
			stu = (Student)JavaBeanFactory.newInstance().createObject(calDocument, Student.class);
			
			String stuXml = JavaBeanFactory.newInstance().getObjectString(stu, "School");
			System.out.println(stuXml);
			
			Student s2 = (Student)JavaBeanFactory.newInstance().createObject(XmlTool.stringToDocument(stuXml), Student.class);
			System.out.println(s2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static Document getDocumentByURI(String path){
		SAXReader reader = new SAXReader();
        Document document = null;
		try {
			document = reader.read(new FileInputStream(path));
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return document;
	}
}

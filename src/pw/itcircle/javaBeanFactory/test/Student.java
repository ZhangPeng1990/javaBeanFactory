package pw.itcircle.javaBeanFactory.test;

import java.util.List;

public class Student {

	private String name;
	private Integer age;
	private String sex;
	
	private Course zhuxiu;
	
	private List<Course> allKecheng;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public Course getZhuxiu() {
		return zhuxiu;
	}
	public void setZhuxiu(Course zhuxiu) {
		this.zhuxiu = zhuxiu;
	}
	public List<Course> getAllKecheng() {
		return allKecheng;
	}
	public void setAllKecheng(List<Course> allKecheng) {
		this.allKecheng = allKecheng;
	}
}

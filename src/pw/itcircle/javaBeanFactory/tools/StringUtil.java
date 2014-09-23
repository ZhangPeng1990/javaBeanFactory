package pw.itcircle.javaBeanFactory.tools;

public class StringUtil {

	public static boolean haveContent(String str)
	{
		boolean have = false;
		if(str != null && str.trim().length() > 0)
		{
			have = true;
		}
		return have;
	}
}

package com.hisun.app.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 用来加载类，ｃｌａｓｓｐａｔｈ下的资源文件，属性文件等。
 * getExtendResource(StringrelativePath)方法，可以使用../符号来加载classpath外部的资源。
 **/
public class HiClassLoaderUtil {
	private static Log log = LogFactory.getLog(HiClassLoaderUtil.class);

	public static String getCurrentClassloaderDetail() {

		StringBuffer classLoaderDetail = new StringBuffer();

		Stack<ClassLoader> classLoaderStack = new Stack<ClassLoader>();

		ClassLoader currentClassLoader = Thread.currentThread()
				.getContextClassLoader();

		classLoaderDetail
				.append("\n-----------------------------------------------------------------\n");

		// Build a Stack of the current ClassLoader chain

		while (currentClassLoader != null) {

			classLoaderStack.push(currentClassLoader);

			currentClassLoader = currentClassLoader.getParent();

		}

		// Print ClassLoader parent chain

		while (classLoaderStack.size() > 0) {

			ClassLoader classLoader = classLoaderStack.pop();

			// Print current

			classLoaderDetail.append(classLoader);

			if (classLoaderStack.size() > 0) {

				classLoaderDetail.append("\n--- delegation ---\n");

			} else {

				classLoaderDetail.append(" **Current ClassLoader**");

			}

		}

		classLoaderDetail
				.append("\n-----------------------------------------------------------------\n");

		return classLoaderDetail.toString();

	}

	/**
	 * *Thread.currentThread().getContextClassLoader().getResource("")
	 */
	/**
	 * 加载Java类。 使用全限定类名
	 * 
	 * @paramclassName
	 * @return
	 */
	public static Class loadClass(String className) {
		try {
			return getClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("class not found '" + className + "'", e);
		}
	}

	/**
	 * *得到类加载器 *
	 * 
	 * @return
	 */
	public static ClassLoader getClassLoader() {
		return HiClassLoaderUtil.class.getClassLoader();
	}

	/**
	 * 提供相对于classpath的资源路径，返回文件的输入流
	 * 
	 * @param relativePath必须传递资源的相对路径
	 *            。是相对于classpath的路径。 如果需要查找classpath外部的资源，需要使用../来查找
	 * @return 文件输入流
	 * @throwsIOException
	 * @throwsMalformedURLException
	 */
	public static InputStream getStream(String relativePath)
			throws MalformedURLException, IOException {
		if (!relativePath.contains("../")) {
			return getClassLoader().getResourceAsStream(relativePath);

		} else {
			return HiClassLoaderUtil.getStreamByExtendResource(relativePath);
		}
	}

	/**
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static InputStream getStream(URL url) throws IOException {
		if (url != null) {
			return url.openStream();
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param relativePath
	 *            relativePath必须传递资源的相对路径。是相对于classpath的路径。
	 *            如果需要查找classpath外部的资源，需要使用../来查找
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static InputStream getStreamByExtendResource(String relativePath)
			throws MalformedURLException, IOException {
		return HiClassLoaderUtil.getStream(HiClassLoaderUtil
				.getExtendResource(relativePath));

	}

	/**
	 * 提供相对于classpath的资源路径，返回属性对象，它是一个散列表
	 * 
	 * @param resource
	 * @return
	 */
	public static Properties getProperties(String resource) {
		Properties properties = new Properties();
		try {
			properties.load(getStream(resource));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("couldn't load properties file '"
					+ resource + "'", e);
		}

		return properties;
	}

	/**
	 * 得到本Class所在的ClassLoader的Classpat的绝对路径。 URL形式的
	 * 
	 * @return
	 */
	public static String getAbsolutePathOfClassLoaderClassPath() {
		HiClassLoaderUtil.log.info(HiClassLoaderUtil.getClassLoader()
				.getResource("").toString());
		return HiClassLoaderUtil.getClassLoader().getResource("").toString();
	}

	/**
	 * relativePath 必须传递资源的相对路径。是相对于classpath的路径。
	 * 如果需要查找classpath外部的资源，需要使用../来查找
	 * 
	 * @param relativePath
	 * @return
	 * @throws MalformedURLException
	 */
	public static URL getExtendResource(String relativePath)
			throws MalformedURLException {
		HiClassLoaderUtil.log.info("传入的相对路径：" + relativePath);
		if (!relativePath.contains("../")) {
			return HiClassLoaderUtil.getResource(relativePath);
		}

		String classPathAbsolutePath = HiClassLoaderUtil
				.getAbsolutePathOfClassLoaderClassPath();
		if (relativePath.substring(0, 1).equals("/")) {
			relativePath = relativePath.substring(1);
		}

		HiClassLoaderUtil.log.info(Integer.valueOf(relativePath
				.lastIndexOf("../")));

		String wildcardString = relativePath.substring(0,
				relativePath.lastIndexOf("../") + 3);
		relativePath = relativePath
				.substring(relativePath.lastIndexOf("../") + 3);
		int containSum = HiClassLoaderUtil.containSum(wildcardString, "../");
		classPathAbsolutePath = HiClassLoaderUtil.cutLastString(
				classPathAbsolutePath, "/", containSum);
		String resourceAbsolutePath = classPathAbsolutePath + relativePath;
		HiClassLoaderUtil.log.info("绝对路径：" + resourceAbsolutePath);
		URL resourceAbsoluteURL = new URL(resourceAbsolutePath);
		return resourceAbsoluteURL;
	}

	/**
	 * 
	 * @param source
	 * @param dest
	 * @return
	 */
	private static int containSum(String source, String dest) {
		int containSum = 0;
		int destLength = dest.length();
		while (source.contains(dest)) {
			containSum = containSum + 1;
			source = source.substring(destLength);
		}
		return containSum;
	}

	/**
	 * 
	 * @param source
	 * @param dest
	 * @param num
	 * @return
	 */
	private static String cutLastString(String source, String dest, int num) {
		for (int i = 0; i < num; i++) {
			source = source.substring(0,
					source.lastIndexOf(dest, source.length() - 2) + 1);
		}

		return source;
	}

	public static URL getResource(String resource) {
		HiClassLoaderUtil.log.info("传入的相对于classpath的路径：" + resource);
		return HiClassLoaderUtil.getClassLoader().getResource(resource);
	}

	public static void main(String[] args) {
		try {
			System.out.println(HiClassLoaderUtil
					.getExtendResource("../HiServiceVisitor.class"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getContextClassLoader()
				.getResource("../HiServiceVisitor.class"));
	}
}
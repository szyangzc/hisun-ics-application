package com.hisun.app.tools;

import com.hisun.util.system.HiResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;


public class HiConfigure {
	public static void loadProperties() {
		InputStream is = null;
		Throwable error = null;
		Properties properties = new Properties();
		try {
			is = HiResource.getResourceAsStream("conf/classpath.properties");
			if (is != null)
				properties.load(is);
			is = null;
			Enumeration enumeration = properties.propertyNames();

			while (enumeration.hasMoreElements()) {
				String name = (String) enumeration.nextElement();
				String value = properties.getProperty(name);
				System.setProperty(name, value);
			}
			properties.clear();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

	}
}

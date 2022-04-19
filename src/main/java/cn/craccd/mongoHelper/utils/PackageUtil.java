package cn.craccd.mongoHelper.utils;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class PackageUtil {
	@Autowired
	ApplicationContext context;

	/**
	 * 找到主程序包
	 * @return
	 */
	public String getMainPackage() {
		Map<String, Object> annotatedBeans = context.getBeansWithAnnotation(SpringBootApplication.class);
		String packageName = annotatedBeans.isEmpty() ? null : annotatedBeans.values().toArray()[0].getClass().getPackage().getName();

		return packageName;
	}
}

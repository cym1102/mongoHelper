package cn.craccd.mongoHelper.utils;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.stereotype.Service;

import cn.craccd.mongoHelper.bean.Page;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;

/**
 * 数据库导入导出工具
 *
 */
@Service
public class ImportExportUtil {
	// 写链接(写到主库,可使用事务)
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private MongoHelper mongoHelper;
	
	@Autowired
	PackageUtil packageUtil;
	public void exportDb(String path) {
		path = path.replace(".zip", "");
		FileUtil.del(path);
		FileUtil.del(path + ".zip");
		try {

			// 找到主程序包
			Set<Class<?>> set = ClassUtil.scanPackage(packageUtil.getMainPackage());
			Page page = new Page();
			page.setLimit(1000);

			for (Class<?> clazz : set) {
				Document document = clazz.getAnnotation(Document.class);
				if (document == null) {
					continue;
				}

				page.setCurr(1);
				while (true) {
					page = mongoHelper.findPage(page, clazz);
					if (page.getList().size() == 0) {
						break;
					}

					List<String> lines = new ArrayList<String>();
					for (Object object : page.getList()) {
						lines.add(JSONUtil.toJsonStr(object));
					}
					FileUtil.appendLines(lines, path + File.separator + clazz.getSimpleName() + ".json", "UTF-8");
					StaticLog.info(clazz.getSimpleName() + "表导出了" + page.getList().size() + "条数据");
					page.setCurr(page.getCurr() + 1);
				}
			}
			ZipUtil.zip(path);

		} catch (Exception e) {
			e.printStackTrace();
			FileUtil.del(path + ".zip");
		}

		FileUtil.del(path);
	}

	public void importDb(String path) {
		if (!FileUtil.exist(path)) {
			StaticLog.info(path + "文件不存在");
			return;
		}
		BufferedReader reader = null;

		path = path.replace(".zip", "");
		FileUtil.del(path);
		ZipUtil.unzip(path + ".zip");
		try {

			// 找到主程序包
			Set<Class<?>> set = ClassUtil.scanPackage(packageUtil.getMainPackage());
			for (Class<?> clazz : set) {
				Document document = clazz.getAnnotation(Document.class);
				if (document == null) {
					continue;
				}

				File file = new File(path + File.separator + clazz.getSimpleName() + ".json");
				if (file.exists()) {
					mongoTemplate.dropCollection(clazz);

					reader = FileUtil.getReader(file, "UTF-8");
					List<Object> list = new ArrayList<Object>();
					while (true) {
						String json = reader.readLine();
						if (StrUtil.isEmpty(json)) {
							mongoTemplate.insertAll(list);
							StaticLog.info(clazz.getSimpleName() + "表导入了" + list.size() + "条数据");
							list.clear();
							break;
						}
						list.add(JSONUtil.toBean(json, clazz));
						if (list.size() == 1000) {
							mongoTemplate.insertAll(list);
							StaticLog.info(clazz.getSimpleName() + "表导入了" + list.size() + "条数据");
							list.clear();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IoUtil.close(reader);
		}
		FileUtil.del(path);
	}
}

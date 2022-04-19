package cn.craccd.mongoHelper.config;

import java.lang.reflect.Field;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.UpdateResult;

import cn.craccd.mongoHelper.bean.IgnoreDocument;
import cn.craccd.mongoHelper.bean.InitValue;
import cn.craccd.mongoHelper.utils.PackageUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;

/**
 * 启动时将表初始化
 *
 */
@Service
public class ScanNewField {
	@Autowired
	PackageUtil packageUtil;
	// 写链接(写到主库,可使用事务)
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	MongoMappingContext mongoMappingContext;

	@PostConstruct
	public void scan() {
		// 找到主程序包
		Set<Class<?>> set = ClassUtil.scanPackage(packageUtil.getMainPackage());
		for (Class<?> clazz : set) {
			IgnoreDocument ignoreDocument = clazz.getAnnotation(IgnoreDocument.class);
			if (ignoreDocument != null) {
				continue;
			}

			Document document = clazz.getAnnotation(Document.class);
			if (document == null) {
				continue;
			}

			// 创建表
			if (!mongoTemplate.collectionExists(clazz)) {
				mongoTemplate.createCollection(clazz);
				System.out.println("创建了" + clazz.getSimpleName() + "表");
			}

			// 创建索引
			IndexOperations indexOps = mongoTemplate.indexOps(clazz);
			IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
			resolver.resolveIndexFor(clazz).forEach(indexOps::ensureIndex);

			Field[] fields = ReflectUtil.getFields(clazz);
			for (Field field : fields) {
				// 获取注解
				if (field.isAnnotationPresent(InitValue.class)) {
					InitValue initValue = field.getAnnotation(InitValue.class);
					if (initValue.value() != null) {

						// 更新表默认值
						Query query = new Query();
						query.addCriteria(Criteria.where(field.getName()).is(null));

						Long count = mongoTemplate.count(query, clazz);
						if (count > 0) {
							Object value = null;
							Class<?> type = field.getType();
							
							if (type.equals(String.class)) {
								value = initValue.value();
							}
							if (type.equals(Short.class)) {
								value = Short.parseShort(initValue.value());
							}
							if (type.equals(Integer.class)) {
								value = Integer.parseInt(initValue.value());
							}
							if (type.equals(Long.class)) {
								value =  Long.parseLong(initValue.value());
							}
							if (type.equals(Float.class)) {
								value =  Float.parseFloat(initValue.value());
							}
							if (type.equals(Double.class)) {
								value = Double.parseDouble(initValue.value());
							}
							if (type.equals(Boolean.class)) {
								value =  Boolean.parseBoolean(initValue.value());
							}
							
							Update update = new Update().set(field.getName(), value);
							UpdateResult updateResult = mongoTemplate.updateMulti(query, update, clazz);

							System.out.println(clazz.getSimpleName() + "表更新了" + updateResult.getModifiedCount() + "条默认值");
						}
					}
				}

			}

		}
	}

}

package cn.craccd.mongoHelper.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.QueryMapper;
import org.springframework.data.mongodb.core.convert.UpdateMapper;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import cn.craccd.mongoHelper.bean.CreateTime;
import cn.craccd.mongoHelper.bean.IgnoreColumn;
import cn.craccd.mongoHelper.bean.InitValue;
import cn.craccd.mongoHelper.bean.Page;
import cn.craccd.mongoHelper.bean.SlowQuery;
import cn.craccd.mongoHelper.bean.SortBuilder;
import cn.craccd.mongoHelper.bean.UpdateBuilder;
import cn.craccd.mongoHelper.bean.UpdateTime;
import cn.craccd.mongoHelper.config.Constant;
import cn.craccd.mongoHelper.reflection.ReflectionUtil;
import cn.craccd.mongoHelper.reflection.SerializableFunction;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * mongodb操作器
 *
 */
@Service("mongoHelper")
public class MongoHelper {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	protected MongoConverter mongoConverter;

	protected QueryMapper queryMapper;
	protected UpdateMapper updateMapper;

	@Autowired
	protected MongoTemplate mongoTemplate;

	public MongoTemplate getMongoTemplate() {
		return mongoTemplate;
	}

	@Value("${spring.data.mongodb.print:false}")
	protected Boolean print;

	@Value("${spring.data.mongodb.slowQuery:false}")
	protected Boolean slowQuery;

	@Value("${spring.data.mongodb.slowTime:1000}")
	protected Long slowTime;

	@PostConstruct
	public void init() {
		queryMapper = new QueryMapper(mongoConverter);
		updateMapper = new UpdateMapper(mongoConverter);
	}

	private void insertSlowQuery(String log, Long queryTime) {
		if (slowQuery) {
			SlowQuery slowQuery = new SlowQuery();
			slowQuery.setQuery(log);
			slowQuery.setTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
			slowQuery.setQueryTime(queryTime);
			slowQuery.setSystem(SystemTool.getSystem());
			StackTraceElement stack[] = Thread.currentThread().getStackTrace();

			// 保存堆栈
			String stackStr = "";
			for (int i = 0; i < stack.length; i++) {
				stackStr += stack[i].getClassName() + "." + stack[i].getMethodName() + ":" + stack[i].getLineNumber()
						+ "\n";
			}
			slowQuery.setStack(stackStr);

			mongoTemplate.insert(slowQuery);
		}
	}

	/**
	 * 打印查询语句
	 * 
	 * @param clazz     类
	 * @param query     查询对象
	 * @param startTime 查询开始时间
	 */
	private void logQuery(Class<?> clazz, Query query, Long startTime) {

		MongoPersistentEntity<?> entity = mongoConverter.getMappingContext().getPersistentEntity(clazz);
		Document mappedQuery = queryMapper.getMappedObject(query.getQueryObject(), entity);
		Document mappedField = queryMapper.getMappedObject(query.getFieldsObject(), entity);
		Document mappedSort = queryMapper.getMappedObject(query.getSortObject(), entity);

		String log = "\ndb." + getCollectionName(clazz) + ".find(";

		log += FormatUtils.bson(mappedQuery.toJson()) + ")";

		if (!query.getFieldsObject().isEmpty()) {
			log += ".projection(";
			log += FormatUtils.bson(mappedField.toJson()) + ")";
		}

		if (query.isSorted()) {
			log += ".sort(";
			log += FormatUtils.bson(mappedSort.toJson()) + ")";
		}

		if (query.getLimit() != 0l) {
			log += ".limit(" + query.getLimit() + ")";
		}

		if (query.getSkip() != 0l) {
			log += ".skip(" + query.getSkip() + ")";
		}
		log += ";";

		// 记录慢查询
		Long queryTime = System.currentTimeMillis() - startTime;
		if (queryTime > slowTime) {
			insertSlowQuery(log, queryTime);
		}
		if (print) {
			// 打印语句
			logger.info(log + "\n执行时间:" + queryTime + "ms");
		}

	}

	/**
	 * 根据类获取集合名
	 * 
	 * @param clazz 类
	 * @return String 集合名
	 */
	private String getCollectionName(Class<?> clazz) {
		org.springframework.data.mongodb.core.mapping.Document document = clazz
				.getAnnotation(org.springframework.data.mongodb.core.mapping.Document.class);
		if (document != null) {
			if (StrUtil.isNotEmpty(document.value())) {
				return document.value();
			}
			if (StrUtil.isNotEmpty(document.collection())) {
				return document.collection();
			}
		}

		return StrUtil.lowerFirst(clazz.getSimpleName());
	}

	/**
	 * 打印查询语句
	 * 
	 * @param clazz     类
	 * @param query     查询对象
	 * @param startTime 查询开始时间
	 */
	private void logCount(Class<?> clazz, Query query, Long startTime) {

		MongoPersistentEntity<?> entity = mongoConverter.getMappingContext().getPersistentEntity(clazz);
		Document mappedQuery = queryMapper.getMappedObject(query.getQueryObject(), entity);

		String log = "\ndb." + getCollectionName(clazz) + ".find(";
		log += FormatUtils.bson(mappedQuery.toJson()) + ")";
		log += ".count();";

		// 记录慢查询
		Long queryTime = System.currentTimeMillis() - startTime;
		if (queryTime > slowTime) {
			insertSlowQuery(log, queryTime);
		}
		if (print) {
			// 打印语句
			logger.info(log + "\n执行时间:" + queryTime + "ms");
		}

	}

	/**
	 * 打印查询语句
	 * 
	 * @param clazz     类
	 * @param query     查询对象
	 * @param startTime 查询开始时间
	 */
	private void logDelete(Class<?> clazz, Query query, Long startTime) {

		MongoPersistentEntity<?> entity = mongoConverter.getMappingContext().getPersistentEntity(clazz);
		Document mappedQuery = queryMapper.getMappedObject(query.getQueryObject(), entity);

		String log = "\ndb." + getCollectionName(clazz) + ".remove(";
		log += FormatUtils.bson(mappedQuery.toJson()) + ")";
		log += ";";

		// 记录慢查询
		Long queryTime = System.currentTimeMillis() - startTime;
		if (queryTime > slowTime) {
			insertSlowQuery(log, queryTime);
		}
		if (print) {
			// 打印语句
			logger.info(log + "\n执行时间:" + queryTime + "ms");
		}

	}

	/**
	 * 打印查询语句
	 * 
	 * @param clazz         类
	 * @param query         查询对象
	 * @param updateBuilder 更新对象
	 * @param multi         是否为批量更新
	 * @param startTime     查询开始时间
	 */
	private void logUpdate(Class<?> clazz, Query query, UpdateBuilder updateBuilder, boolean multi, Long startTime) {

		MongoPersistentEntity<?> entity = mongoConverter.getMappingContext().getPersistentEntity(clazz);
		Document mappedQuery = queryMapper.getMappedObject(query.getQueryObject(), entity);
		Document mappedUpdate = updateMapper.getMappedObject(updateBuilder.toUpdate().getUpdateObject(), entity);

		String log = "\ndb." + getCollectionName(clazz) + ".update(";
		log += FormatUtils.bson(mappedQuery.toJson()) + ",";
		log += FormatUtils.bson(mappedUpdate.toJson()) + ",";
		log += FormatUtils.bson("{multi:" + multi + "})");
		log += ";";

		// 记录慢查询
		Long queryTime = System.currentTimeMillis() - startTime;
		if (queryTime > slowTime) {
			insertSlowQuery(log, queryTime);
		}
		if (print) {
			// 打印语句
			logger.info(log + "\n执行时间:" + queryTime + "ms");
		}

	}

	/**
	 * 打印查询语句
	 * 
	 * @param object    保存对象
	 * @param startTime 查询开始时间
	 * @param isInsert  是否为插入
	 */
	private void logSave(Object object, Long startTime, Boolean isInsert) {
		JSONObject jsonObject = JSONUtil.parseObj(object);

		if (isInsert) {
			jsonObject.remove(Constant.ID);
		}

		String log = "\ndb." + getCollectionName(object.getClass()) + ".save(";
		log += JSONUtil.toJsonPrettyStr(jsonObject);
		log += ");";

		// 记录慢查询
		Long queryTime = System.currentTimeMillis() - startTime;
		if (queryTime > slowTime) {
			insertSlowQuery(log, queryTime);
		}
		if (print) {
			// 打印语句
			logger.info(log + "\n执行时间:" + queryTime + "ms");
		}
	}

	/**
	 * 打印查询语句
	 * 
	 * @param list      保存集合
	 * @param startTime 查询开始时间
	 */
	private void logSave(List<?> list, Long startTime) {
		List<JSONObject> cloneList = new ArrayList<>();
		for (Object item : list) {
			JSONObject jsonObject = JSONUtil.parseObj(item);

			jsonObject.remove(Constant.ID);
			cloneList.add(jsonObject);
		}

		Object object = list.get(0);
		String log = "\ndb." + getCollectionName(object.getClass()) + ".save(";
		log += JSONUtil.toJsonPrettyStr(cloneList);
		log += ");";

		// 记录慢查询
		Long queryTime = System.currentTimeMillis() - startTime;
		if (queryTime > slowTime) {
			insertSlowQuery(log, queryTime);
		}
		if (print) {
			// 打印语句
			logger.info(log + "\n执行时间:" + queryTime + "ms");
		}
	}

	/**
	 * 插入或更新
	 * 
	 * @param object 保存对象
	 * @return String 对象id
	 */
	public String insertOrUpdate(Object object) {

		Long time = System.currentTimeMillis();
		String id = (String) ReflectUtil.getFieldValue(object, Constant.ID);
		Object objectOrg = StrUtil.isNotEmpty(id) ? findById(id, object.getClass()) : null;

		if (objectOrg == null) {
			// 插入
			// 设置插入时间
			setCreateTime(object, time);
			// 设置更新时间
			setUpdateTime(object, time);

			// 设置默认值
			setDefaultVaule(object);
			// 去除id值
			ReflectUtil.setFieldValue(object, Constant.ID, null);

			// 克隆一个@IgnoreColumn的字段设为null的对象;
			Object objectClone = BeanUtil.copyProperties(object, object.getClass());
			ignoreColumn(objectClone);

			mongoTemplate.save(objectClone);
			id = (String) ReflectUtil.getFieldValue(objectClone, Constant.ID);

			// 设置id值
			ReflectUtil.setFieldValue(object, Constant.ID, id);

			logSave(objectClone, time, true);

		} else {
			// 更新
			Field[] fields = ReflectUtil.getFields(object.getClass());
			// 拷贝属性
			for (Field field : fields) {
				if (!field.getName().equals(Constant.ID) && ReflectUtil.getFieldValue(object, field) != null) {
					ReflectUtil.setFieldValue(objectOrg, field, ReflectUtil.getFieldValue(object, field));
				}
			}

			// 设置更新时间
			setUpdateTime(objectOrg, time);
			// 克隆一个@IgnoreColumn的字段设为null的对象;
			Object objectClone = BeanUtil.copyProperties(objectOrg, object.getClass());
			ignoreColumn(objectClone);

			mongoTemplate.save(objectClone);
			logSave(objectClone, time, false);
		}

		return id;
	}

	/**
	 * 插入
	 * 
	 * @param object 对象
	 * @return String 对象id
	 */
	public String insert(Object object) {
		ReflectUtil.setFieldValue(object, Constant.ID, null);
		insertOrUpdate(object);
		return (String) ReflectUtil.getFieldValue(object, Constant.ID);
	}

	/**
	 * 批量插入
	 * 
	 * @param list 批量插入对象
	 * @return Collection<T> 批量对象id集合
	 */
	public <T> List<String> insertAll(List<T> list) {
		Long time = System.currentTimeMillis();

		List listClone = new ArrayList<>();
		for (Object object : list) {

			// 去除id以便插入
			ReflectUtil.setFieldValue(object, Constant.ID, null);
			// 设置插入时间
			setCreateTime(object, time);
			// 设置更新时间
			setUpdateTime(object, time);
			// 设置默认值
			setDefaultVaule(object);
			// 克隆一个@IgnoreColumn的字段设为null的对象;
			Object objectClone = BeanUtil.copyProperties(object, object.getClass());
			ignoreColumn(objectClone);
			listClone.add(objectClone);
		}

		mongoTemplate.insertAll(listClone);
		logSave(listClone, time);

		List<String> ids = new ArrayList<>();
		for (Object object : listClone) {
			String id = (String) ReflectUtil.getFieldValue(object, Constant.ID);
			ids.add(id);
		}

		return ids;
	}

	/**
	 * 设置更新时间
	 * 
	 * @param object 对象
	 * @param time   更新时间
	 */
	private void setUpdateTime(Object object, Long time) {
		Field[] fields = ReflectUtil.getFields(object.getClass());
		for (Field field : fields) {
			// 获取注解
			if (field.isAnnotationPresent(UpdateTime.class) && field.getType().equals(Long.class)) {
				ReflectUtil.setFieldValue(object, field, time);
			}
		}
	}

	/**
	 * 设置创建时间
	 * 
	 * @param object 对象
	 * @param time   创建时间
	 */
	private void setCreateTime(Object object, Long time) {
		Field[] fields = ReflectUtil.getFields(object.getClass());
		for (Field field : fields) {
			// 获取注解
			if (field.isAnnotationPresent(CreateTime.class) && field.getType().equals(Long.class)) {
				ReflectUtil.setFieldValue(object, field, time);
			}
		}
	}

	/**
	 * 将带有@IgnoreColumn的字段设为null;
	 * 
	 * @param object 对象
	 */
	private void ignoreColumn(Object object) {
		Field[] fields = ReflectUtil.getFields(object.getClass());
		for (Field field : fields) {
			// 获取注解
			if (field.isAnnotationPresent(IgnoreColumn.class)) {
				ReflectUtil.setFieldValue(object, field, null);
			}
		}
	}

	/**
	 * 根据id更新
	 * 
	 * @param object 对象
	 * @return String 对象id
	 */
	public String updateById(Object object) {
		if (StrUtil.isEmpty((String) ReflectUtil.getFieldValue(object, Constant.ID))) {
			return null;
		}
		if (findById((String) ReflectUtil.getFieldValue(object, Constant.ID), object.getClass()) == null) {
			return null;
		}
		return insertOrUpdate(object);
	}

	/**
	 * 根据id更新全部字段
	 * 
	 * @param object 对象
	 * @return String 对象id
	 */
	public String updateAllColumnById(Object object) {

		if (StrUtil.isEmpty((String) ReflectUtil.getFieldValue(object, Constant.ID))) {
			return null;
		}
		if (findById((String) ReflectUtil.getFieldValue(object, Constant.ID), object.getClass()) == null) {
			return null;
		}
		Long time = System.currentTimeMillis();
		setUpdateTime(object, time);
		mongoTemplate.save(object);
		logSave(object, time, false);

		return (String) ReflectUtil.getFieldValue(object, Constant.ID);
	}

	/**
	 * 更新查到的第一项
	 * 
	 * @param criteriaWrapper 查询
	 * @param updateBuilder   更新
	 * @param clazz           类
	 * @return UpdateResult 更新结果
	 */
	public UpdateResult updateFirst(CriteriaWrapper criteriaWrapper, UpdateBuilder updateBuilder, Class<?> clazz) {
		Long time = System.currentTimeMillis();
		Query query = new Query(criteriaWrapper.build());

		UpdateResult updateResult = mongoTemplate.updateFirst(query, updateBuilder.toUpdate(), clazz);
		logUpdate(clazz, query, updateBuilder, false, time);

		return updateResult;
	}

	/**
	 * 更新查到的全部项
	 * 
	 * @param criteriaWrapper 查询
	 * @param updateBuilder   更新
	 * @param clazz           类
	 * @return UpdateResult 更新结果
	 */
	public UpdateResult updateMulti(CriteriaWrapper criteriaWrapper, UpdateBuilder updateBuilder, Class<?> clazz) {

		Long time = System.currentTimeMillis();
		Query query = new Query(criteriaWrapper.build());
		UpdateResult updateResult = mongoTemplate.updateMulti(new Query(criteriaWrapper.build()),
				updateBuilder.toUpdate(), clazz);
		logUpdate(clazz, query, updateBuilder, true, time);

		return updateResult;
	}

	/**
	 * 根据id删除
	 * 
	 * @param id    对象
	 * @param clazz 类
	 * @return DeleteResult 删除结果
	 */
	public DeleteResult deleteById(String id, Class<?> clazz) {

		if (StrUtil.isEmpty(id)) {
			return null;
		}
		return deleteByQuery(new CriteriaAndWrapper().eq(Constant::getId, id), clazz);
	}

	/**
	 * 根据id删除
	 * 
	 * @param id    对象
	 * @param clazz 类
	 * @return DeleteResult 删除结果
	 */
	public DeleteResult deleteByIds(List<String> ids, Class<?> clazz) {

		if (ids == null || ids.size() == 0) {
			return null;
		}

		return deleteByQuery(new CriteriaAndWrapper().in(Constant::getId, ids), clazz);
	}

	/**
	 * 根据条件删除
	 * 
	 * @param criteria 查询
	 * @param clazz    类
	 * @return DeleteResult 删除结果
	 */
	public DeleteResult deleteByQuery(CriteriaWrapper criteriaWrapper, Class<?> clazz) {
		Long time = System.currentTimeMillis();
		Query query = new Query(criteriaWrapper.build());
		DeleteResult deleteResult = mongoTemplate.remove(query, clazz);
		logDelete(clazz, query, time);

		return deleteResult;
	}

	/**
	 * 设置默认值
	 * 
	 * @param object 对象
	 */
	private void setDefaultVaule(Object object) {
		Field[] fields = ReflectUtil.getFields(object.getClass());
		for (Field field : fields) {
			// 获取注解
			if (field.isAnnotationPresent(InitValue.class)) {
				InitValue defaultValue = field.getAnnotation(InitValue.class);

				String value = defaultValue.value();

				if (ReflectUtil.getFieldValue(object, field) == null) {
					// 获取字段类型
					Class<?> type = field.getType();
					if (type.equals(String.class)) {
						ReflectUtil.setFieldValue(object, field, value);
					}
					if (type.equals(Short.class)) {
						ReflectUtil.setFieldValue(object, field, Short.parseShort(value));
					}
					if (type.equals(Integer.class)) {
						ReflectUtil.setFieldValue(object, field, Integer.parseInt(value));
					}
					if (type.equals(Long.class)) {
						ReflectUtil.setFieldValue(object, field, Long.parseLong(value));
					}
					if (type.equals(Float.class)) {
						ReflectUtil.setFieldValue(object, field, Float.parseFloat(value));
					}
					if (type.equals(Double.class)) {
						ReflectUtil.setFieldValue(object, field, Double.parseDouble(value));
					}
					if (type.equals(Boolean.class)) {
						ReflectUtil.setFieldValue(object, field, Boolean.parseBoolean(value));
					}
				}
			}
		}
	}

	/**
	 * 累加某一个字段的数量,原子操作
	 * 
	 * @param object
	 * @return UpdateResult 更新结果
	 */
	public <R, E> UpdateResult addCountById(String id, SerializableFunction<E, R> property, Number count,
			Class<?> clazz) {
		UpdateBuilder updateBuilder = new UpdateBuilder().inc(property, count);

		return updateFirst(new CriteriaAndWrapper().eq(Constant::getId, id), updateBuilder, clazz);
	}

	/**
	 * 按查询条件获取Page
	 * 
	 * @param criteria 查询
	 * @param page     分页
	 * @param clazz    类
	 * @return Page 分页
	 */
	public <T> Page<T> findPage(CriteriaWrapper criteriaWrapper, Page<?> page, Class<T> clazz) {
		SortBuilder sortBuilder = new SortBuilder(Constant::getId, Direction.DESC);
		return findPage(criteriaWrapper, sortBuilder, page, clazz);
	}

	/**
	 * 按查询条件获取Page
	 * 
	 * @param criteria 查询
	 * @param sort     排序
	 * @param clazz    类
	 * @return Page 分页
	 */
	public <T> Page<T> findPage(CriteriaWrapper criteriaWrapper, SortBuilder sortBuilder, Page<?> page,
			Class<T> clazz) {

		Page<T> pageResp = new Page<T>();
		pageResp.setCurr(page.getCurr());
		pageResp.setLimit(page.getLimit());

		// 查询出总条数
		if (page.getQueryCount()) {
			Long count = findCountByQuery(criteriaWrapper, clazz);
			pageResp.setCount(count);
		}

		// 查询List
		Query query = new Query(criteriaWrapper.build());
		query.with(sortBuilder.toSort());
		query.skip((page.getCurr() - 1) * page.getLimit());// 从那条记录开始
		query.limit(page.getLimit());// 取多少条记录

		Long systemTime = System.currentTimeMillis();
		List<T> list = mongoTemplate.find(query, clazz);
		logQuery(clazz, query, systemTime);

		pageResp.setList(list);

		return pageResp;
	}

	/**
	 * 按查询条件获取Page
	 * 
	 * @param criteria 查询
	 * @param sort     排序
	 * @param clazz    类
	 * @return Page 分页
	 */
	public <T> Page<T> findPage(SortBuilder sortBuilder, Page<?> page, Class<T> clazz) {
		return findPage(new CriteriaAndWrapper(), sortBuilder, page, clazz);
	}

	/**
	 * 获取Page
	 * 
	 * @param page  分页
	 * @param clazz 类
	 * @return Page 分页
	 */
	public <T> Page<T> findPage(Page<?> page, Class<T> clazz) {
		return findPage(new CriteriaAndWrapper(), page, clazz);
	}

	/**
	 * 根据id查找
	 * 
	 * @param id    id
	 * @param clazz 类
	 * @return T 对象
	 */
	public <T> T findById(String id, Class<T> clazz) {

		if (StrUtil.isEmpty(id)) {
			return null;
		}
		Long systemTime = System.currentTimeMillis();

		T t = (T) mongoTemplate.findById(id, clazz);

		CriteriaAndWrapper criteriaAndWrapper = new CriteriaAndWrapper().eq(Constant::getId, id);
		logQuery(clazz, new Query(criteriaAndWrapper.build()), systemTime);
		return t;
	}

	/**
	 * 根据条件查找单个
	 * 
	 * @param <T>      类型
	 * @param criteria
	 * @param clazz    类
	 * @return T 对象
	 */
	public <T> T findOneByQuery(CriteriaWrapper criteriaWrapper, Class<T> clazz) {
		SortBuilder sortBuilder = new SortBuilder(Constant::getId, Direction.DESC);
		return (T) findOneByQuery(criteriaWrapper, sortBuilder, clazz);
	}

	/**
	 * 根据条件查找单个
	 * 
	 * @param query 查询
	 * @param clazz 类
	 * @return T 对象
	 */
	public <T> T findOneByQuery(CriteriaWrapper criteriaWrapper, SortBuilder sortBuilder, Class<T> clazz) {

		Query query = new Query(criteriaWrapper.build());
		query.limit(1);
		query.with(sortBuilder.toSort());

		Long systemTime = System.currentTimeMillis();
		T t = (T) mongoTemplate.findOne(query, clazz);
		logQuery(clazz, query, systemTime);

		return t;

	}

	/**
	 * 根据条件查找单个
	 * 
	 * @param query 查询
	 * @param clazz 类
	 * @return T 对象
	 */
	public <T> T findOneByQuery(SortBuilder sortBuilder, Class<T> clazz) {
		return (T) findOneByQuery(new CriteriaAndWrapper(), sortBuilder, clazz);
	}

	/**
	 * 根据条件查找List
	 * 
	 * @param <T>      类型
	 * @param criteria 查询
	 * @param clazz    类
	 * @return List 列表
	 */
	public <T> List<T> findListByQuery(CriteriaWrapper criteriaWrapper, Class<T> clazz) {
		SortBuilder sortBuilder = new SortBuilder().add(Constant::getId, Direction.DESC);
		return findListByQuery(criteriaWrapper, sortBuilder, clazz);

	}

	/**
	 * 根据条件查找List
	 * 
	 * @param <T>      类型
	 * @param criteria 查询
	 * @param sort     排序
	 * @param clazz    类
	 * @return List 列表
	 */
	public <T> List<T> findListByQuery(CriteriaWrapper criteriaWrapper, SortBuilder sortBuilder, Class<T> clazz) {
		Query query = new Query(criteriaWrapper.build());
		query.with(sortBuilder.toSort());

		Long systemTime = System.currentTimeMillis();
		List<T> list = mongoTemplate.find(query, clazz);
		logQuery(clazz, query, systemTime);
		return list;

	}

	/**
	 * 根据条件查找某个属性
	 * 
	 * @param <T>           类型
	 * @param criteria      查询
	 * @param documentClass 类
	 * @param property      属性
	 * @param propertyClass 属性类
	 * @return List 列表
	 */
	public <T, R, E> List<T> findPropertiesByQuery(CriteriaWrapper criteriaWrapper, Class<?> documentClass,
			SerializableFunction<E, R> property, Class<T> propertyClass) {
		Query query = new Query(criteriaWrapper.build());
		query.fields().include(ReflectionUtil.getFieldName(property));

		Long systemTime = System.currentTimeMillis();
		List<?> list = mongoTemplate.find(query, documentClass);
		logQuery(documentClass, query, systemTime);

		List<T> propertyList = extractProperty(list, ReflectionUtil.getFieldName(property), propertyClass);
		return propertyList;
	}
	
	
	/**
	 * 根据条件查找某个属性
	 * 
	 * @param <T>           类型
	 * @param criteria      查询
	 * @param documentClass 类
	 * @param property      属性
	 * @param propertyClass 属性类
	 * @return List 列表
	 */
	public <T, R, E> List<T> findPropertiesByQuery(CriteriaWrapper criteriaWrapper, Class<?> documentClass,
			String property, Class<T> propertyClass) {
		Query query = new Query(criteriaWrapper.build());
		query.fields().include(property);

		Long systemTime = System.currentTimeMillis();
		List<?> list = mongoTemplate.find(query, documentClass);
		logQuery(documentClass, query, systemTime);

		List<T> propertyList = extractProperty(list, property, propertyClass);
		return propertyList;
	}

	/**
	 * 根据条件查找某个属性
	 * 
	 * @param <T>           类型
	 * @param criteria      查询
	 * @param documentClass 类
	 * @param property      属性
	 * @return List 列表
	 */
	public <R, E> List<String> findPropertiesByQuery(CriteriaWrapper criteriaWrapper, Class<?> documentClass,
			SerializableFunction<E, R> property) {
		return findPropertiesByQuery(criteriaWrapper, documentClass, property, String.class);
	}

	/**
	 * 根据id查找某个属性
	 * 
	 * @param <T>           类型
	 * @param criteria      查询
	 * @param documentClass 类
	 * @param property      属性
	 * @return List 列表
	 */
	public <R, E> List<String> findPropertiesByIds(List<String> ids, Class<?> clazz,
			SerializableFunction<E, R> property) {
		CriteriaAndWrapper criteriaAndWrapper = new CriteriaAndWrapper().in(Constant::getId, ids);
		return findPropertiesByQuery(criteriaAndWrapper, clazz, property);
	}

	/**
	 * 根据条件查找id
	 * 
	 * @param criteria 查询
	 * @param clazz    类
	 * @return List 列表
	 */
	public List<String> findIdsByQuery(CriteriaWrapper criteriaWrapper, Class<?> clazz) {
		return findPropertiesByQuery(criteriaWrapper, clazz, Constant::getId);
	}

	/**
	 * 根据id集合查找
	 * 
	 * @param List  ids id集合
	 * @param clazz 类
	 * @return List 列表
	 */
	public <T> List<T> findListByIds(Collection<String> ids, Class<T> clazz) {
		CriteriaAndWrapper criteriaAndWrapper = new CriteriaAndWrapper().in(Constant::getId, ids);
		return findListByQuery(criteriaAndWrapper, clazz);
	}

	/**
	 * 根据id集合查找
	 * 
	 * @param List  ids id集合
	 * @param clazz 类
	 * @return List 列表
	 */
	public <T> List<T> findListByIds(Collection<String> ids, SortBuilder sortBuilder, Class<T> clazz) {
		CriteriaAndWrapper criteriaAndWrapper = new CriteriaAndWrapper().in(Constant::getId, ids);
		return findListByQuery(criteriaAndWrapper, sortBuilder, clazz);
	}

	/**
	 * 根据id集合查找
	 * 
	 * @param Array ids id集合
	 * @param clazz 类
	 * @return List 列表
	 */
	public <T> List<T> findListByIds(String[] ids, SortBuilder sortBuilder, Class<T> clazz) {
		return findListByIds(Arrays.asList(ids), sortBuilder, clazz);
	}

	/**
	 * 根据id集合查找
	 * 
	 * @param Array ids id集合
	 * @param clazz 类
	 * @return List 列表
	 */
	public <T> List<T> findListByIds(String[] ids, Class<T> clazz) {
		SortBuilder sortBuilder = new SortBuilder(Constant::getId, Direction.DESC);
		return findListByIds(ids, sortBuilder, clazz);
	}

	/**
	 * 查询全部
	 * 
	 * @param <T>   类型
	 * @param clazz 类
	 * @return List 列表
	 */
	public <T> List<T> findAll(Class<T> clazz) {
		SortBuilder sortBuilder = new SortBuilder(Constant::getId, Direction.DESC);
		return findListByQuery(new CriteriaAndWrapper(), sortBuilder, clazz);
	}

	/**
	 * 查询全部
	 * 
	 * @param <T>   类型
	 * @param clazz 类
	 * @return List 列表
	 */
	public <T> List<T> findAll(SortBuilder sortBuilder, Class<T> clazz) {
		return findListByQuery(new CriteriaAndWrapper(), sortBuilder, clazz);
	}

	/**
	 * 查找全部的id
	 * 
	 * @param clazz 类
	 * @return List 列表
	 */
	public List<String> findAllIds(Class<?> clazz) {
		return findIdsByQuery(new CriteriaAndWrapper(), clazz);
	}

	/**
	 * 查找数量
	 * 
	 * @param criteria 查询
	 * @param clazz    类
	 * @return Long 数量
	 */
	public Long findCountByQuery(CriteriaWrapper criteriaWrapper, Class<?> clazz) {
		Long systemTime = System.currentTimeMillis();
		Long count = null;

		Query query = new Query(criteriaWrapper.build());
		if (query.getQueryObject().isEmpty()) {
			count = mongoTemplate.getCollection(mongoTemplate.getCollectionName(clazz)).estimatedDocumentCount();
		} else {
			count = mongoTemplate.count(query, clazz);
		}

		logCount(clazz, query, systemTime);
		return count;
	}

	/**
	 * 查找全部数量
	 * 
	 * @param clazz 类
	 * @return Long 数量
	 */
	public Long findAllCount(Class<?> clazz) {
		return findCountByQuery(new CriteriaAndWrapper(), clazz);
	}

	/**
	 * 获取list中对象某个属性,组成新的list
	 * 
	 * @param list     列表
	 * @param clazz    类
	 * @param property 属性名
	 * @return List<T> 属性列表
	 */
	@SuppressWarnings("unchecked")
	private <T> List<T> extractProperty(List<?> list, String property, Class<T> clazz) {
		Set<T> rs = new HashSet<T>();
		for (Object object : list) {
			Object value = ReflectUtil.getFieldValue(object, property);
			if (value != null && value.getClass().equals(clazz)) {
				rs.add((T) value);
			}
		}

		return new ArrayList<T>(rs);
	}

}

# mongoHelper

#### 介绍
spring-data-mongodb增强工具包，简化 CRUD 操作，提供类mybatis plus的数据库操作。传统关系型数据库及围绕它们构建的orm在项目开发中有很多难用的痛点，而mongodb这种文档性数据库的出现，完美的解决了sql数据库在项目开发中的诸多痛点，在mongodb4.0以后支持了事务，已经可以完美的用于工程项目。spring-data-mongodb已经对mongodb的操作做了一部分封装，但依然不够，Query Criteria Sort的操作依然有比较大的局限性，而且对于习惯sql操作的人来说，理解其使用法则依然稍显别扭。mongoHelper对spring-data-mongodb又进行了进一步封装，使其更易于使用，并添加了很多易于项目管理的功能。

#### 软件架构
本项目只适用于springBoot项目，项目也依赖springBoot相关库，springMVC项目无法使用，另外项目依赖了hutool提供的诸多Util工具，让代码更简洁。

演示应用项目：https://gitee.com/cym1102/mongoStudy

#### 安装教程

1.  引入maven库

```
    <dependency>
        <groupId>cn.craccd</groupId>
        <artifactId>mongoHelper</artifactId>
        <version>0.8.3</version>
    </dependency>
```

2.  配置springBoot配置文件application.yml

```
spring:
  data:
    mongodb:
      uri:     mongodb://user:pass@host:27017/study?replicaSet=rs0&authSource=admin&w=majority&j=true&wtimeout=5000&readPreference=primary
      print : true  #是否打印查询语句
	  slowQuery : true  #是否记录慢查询到数据库中
	  slowTime : 1000 #慢查询最短时间,默认为1000毫秒
```

3.  在合适的地方添加MongoConfig配置类

```
@Configuration
@ComponentScan(basePackages = {"cn.craccd"}) // 必须填写此包路径
public class MongoConfig {

    // 开启事务(如使用单机mongodb,可不配置此@Bean)
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}

```

4.  在springBoot主运行类中加入@EnableTransactionManagement注解开启事务(如使用单机mongodb,可不配置此项)

5.  注意，mongodb必须在集群配置下才能支持事务，本orm可以在集群mongodb下使用,也可以在单机mongodb下使用，但事务只能在集群下才起作用，单机下使用事务会报错，mongodb集群配置请查阅相关文档，如果业务量不大，可以配置一个单一实例集群，同样可支持事务。
    springBoot配置文件application.yml中spring.data.mongodb.uri如果配置为集群uri。如

```
mongodb://user:pass@host:27017/electric?replicaSet=rs0&authSource=admin&w=majority&j=true&wtimeout=5000&readPreference=primary
```


#### 使用说明

###### 1.  基本操作
本orm会在容器中注入一个对象MongoHelper，这个对象拥有诸多单表查询功能，如下
- 按id删除：deleteById(String, Class<?>)
- 按条件删除：deleteByQuery(Criteria, Class<?>)
- 查询所有：findAll(Class<T>)
- 查询数量：findCount(Class<?>)
- 根据id查询：findById(String, Class<T>)
- 根据条件查询：findListByQuery(Criteria, Class<?>)
- 根据条件查询并分页：findPage(Criteria, Page, Class<?>)
- 插入：insert(Object)
- 插入或更新：insertOrUpdate(Object)
- 根据id更新：updateById(Object)
- 根据id更新全部字段：updateAllColumnById(Object)
- 根据条件更新第一项：updateFirst(Criteria, Update, Class<?>)
- 根据条件更新所有项：updateMulti(Criteria, Update, Class<?>)
- 累加某一个字段的数量, 原子操作：addCountById(String id, String property, Long count, Class<?> clazz)

这个mongoHelper能够完成所有查询任务，插入和更新操作能够自动判断pojo的类型操作对应表，查询操作根据传入的Class进行对应表操作，本orm所有数据库操作都基于mongoHelper的功能，不用像mybatis一样，每个表都要建立一套Mapper，xml，Service，model，大大减少数据层的代码量。可以将mongoHelper直接注入到controller层，简单的操作直接调用mongoHelper进行操作，不需要调用service层。

而复杂的操作及事务处理需要service层，将mongoHelper注入service，并使用service层的@Transactional注解就能使用springBoot管理的事务功能。


###### 2.  复杂查询功能
本orm的查询功能都在MongoHelper的findByQuery，findPage方法中，封装条件的对象使用spring-data-mongodb的Criteria或Query，spring-data-mongodb的查询对象Criteria封装比较死板且不宜用，不太适合像sql一样根据条件拼接，本orm提供了CriteriaAndWrapper与CriteriaOrWrapper。类似于sql中的and连接与or连接，能够组装为近似sql的查询条件。

```
// 根据输入条件进行查询
public List<User> search(String word, Integer type) {
	CriteriaAndWrapper criteriaAndWrapper = new CriteriaAndWrapper();

	if (StrUtil.isNotEmpty(word)) {
		criteriaAndWrapper.and(new CriteriaOrWrapper().like(User::getName, word).like(User::getPhone, word));
	}
	if (type != null) {
		criteriaAndWrapper.eq(User::getType, type);
	}
		
	List<User> userList = mongoHelper.findListByQuery(criteriaAndWrapper, User.class);

	return userList ;
}
```
以上代码组装了类似于select * from user where (name like '%xxx%' or phone like '%xxx%') and type = xxx的查询语句，简单易懂。当然如果习惯使用Criteria进行查询也可以直接使用Criteria。

###### 3.  分页查询，
本orm提供一个Page类，包含count总记录数，limit每页记录数，curr起始页（从1开始），records结果列表四个属性，只要将包含curr和limit数据的Page对象传入findPage，即可查询出records，count的数据并自动返回到Page对象中。这里三个属性参考了layui的分页参数，可直接无缝对接layui的分页控件。

```
public Page search(Page page, String word, Integer type) {
        CriteriaAndWrapper criteriaAndWrapper = new CriteriaAndWrapper();

	if (StrUtil.isNotEmpty(word)) {
		criteriaAndWrapper.and(new CriteriaOrWrapper().like(User::getName, word).like(User::getPhone, word));
	}
	if (type != null) {
		criteriaAndWrapper.eq(User::getType, type);
	}
	page = mongoHelper.findPage(criteriaAndWrapper, new SortBuilder(User::getCreatTime, Direction.DESC), page, User.class);

	return page;
}
```

###### 4.  表映射对象
项目启动时本orm会扫描项目下所有@Document注解的类并建立相应表、索引、字段默认值，这个@Document注解是spring-data-mongodb定义的，另外本项目新增了一个属性注解@InitValue，用于提供字段初始值，和mysql的默认值类似，注意mongodb本身字段是没有默认值的，这里是项目本身用代码实现默认值的功能。如要建立索引，使用@Indexed标记属性建立相应索引，也是spring-data-mongodb定义的。如果有不需要在数据库建立字段的属性，使用@IgnoreColumn注解忽略此属性。

如果想继承被@Document注解的类，但又不想被父类的注解影响生成新的表，可使用@IgnoreDocument，防止mongoHelper生成不需要的表。

```
@Document
public class User extends BaseModel {
	@InitValue("0")
	Integer type; // 类型 0客户 1经销商

	@Indexed // 添加索引
	String name;

	@IgnoreColumn // 忽略字段
	String phone;

    public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
        public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
}
```
另外本orm提供了一个基础model，包含id（用@Id注解），createTime，updateTime三个必备属性，@CreateTime和@UpdateTime注解可在插入和更新时会自动填充时间戳，其他pojo类可继承此BaseModel，简化代码编写。

```
@Document
public class BaseModel implements Serializable{
	@Id // 主键注解
	String id;
	@CreateTime // 自动插入创建时间戳的注解
	Long createTime;
	@UpdateTime // 自动插入更新时间戳的注解
	Long updateTime;
		
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	public Long getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}
}

```

###### 5.数据库导入导出工具
本orm提供一个数据库导入导出工具ImportExportUtil注入到容器中，拥有exportDb和importDb两个方法，可按照pojo类导入导出相应表结构和数据，可使用定时任务定期备份数据库。

###### 6.打印查询语句
spring-data-mongodb默认的打印语句方式为修改配置文件logging.level.root: debug。但这里打印出来的语句基本不可读，也不能像sql一样直接复制出来到数据库中进行执行，处于集群模式下还每隔数秒发送一次检测当前数据库isMaster的命令，很干扰debug。本orm重写了查询语句的打印功能，只要配置spring.data.mongodb.print：true就能打印出如下的语句：
```
db.admin.find({
    "$and": [
        {
            "name": {
                "$regex": "^.*ad.*$",
                "$options": "i"
            }
        }
    ]
}).projection({
    "name": 1
}).sort({
    "id": -1
});
```
可直接复制到mongodb客户端中进行执行，非常方便调试。

###### 7.多数据源
如果需要连接两个以上的mongodb数据库，可先在yml文件中配一个uri2:

```
spring:
  data:
    mongodb:
      uri2:     mongodb://user:pass@host:27017/study?replicaSet=rs0&authSource=admin&w=majority&j=true&wtimeout=5000&readPreference=primary
```

再配置一个MongoHelper2类继承MongoHelper:

```
@Service("mongoHelper2")
public class MongoHelper2 extends MongoHelper {

	@Value("${spring.data.mongodb.uri2}")
	String uri2;

	@PostConstruct
	public void init() {
		// 使用uri2初始化mongoTemplate,不支持事务
		this.mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(uri2));

		super.init();
	}
}

```

这样，注入MongoHelper2进行增删改查的操作就会被执行到uri2的数据库上

###### 8.获取原生mongoTemplate进行操作

如果觉得MongoHelper的方法无法满足项目需求, 可以用mongoHelper.getMongotemplate()的方式获取springboot原生的mongotemplate对象进行操作。 

# 一些关于orm的实践

传统以mysql为代表的关系型数据，依托其所建立的orm框架，如mybatis，mybatis-plus，jpa等，在敏捷开发大行其道的现在，已经不太适合快速开发的系统，其中关系型数据库的各种限制让需求快速变化的系统无法适应。

###### 1.表结构与字段快速变化
需求的快速变化必然引起表结构的快速变化，然而传统关系型数据库，要修改表结构必须使用alter，create等语句。为了保证项目中测试数据库与正式数据库或其他数据库结构一致，有了flyway这种东西，但实际使用中依然不便。首先flyway以sql文件名版本号的形式来维护数据库版本，项目时间一长，flyway文件夹的sql文件数量会变得非常庞大，另外一点，两个开发者同时想要修改表结构时，极易产生版本冲突，两人可能在同一时间都提交了同一个版本号的sql文件，导致flyway执行出错，这种问题处理起来及其麻烦。另外如果一个开发人员本地代码的pojo类与数据库表字段对不上（已经被另外一个开发人员的flyway更新），执行指定字段的select或insert语句是会报错的，此时他只能等待另外一名开放人员将新版pojo类提交。

得益于文档型数据库的特性，灵活的表机构形式，无需修改表结构，在插入新数据时，自动会新增字段和表，不再使用alter，create语句，我们只要修改pojo类，自动可同步数据库到新的表结构。不用管数据库中表结构是否与pojo属性一直，进行插入和查询都不会产生报错，大大降低开发人员的耦合性。

另外确实有一些框架可以实现通过扫描pojo类，自动修改数据库表结构的，达到抛弃flyway的效果，比如 https://gitee.com/sunchenbin/mybatis-enhance 这个框架，但依然解决不了一个开发人员等待另一个提交pojo类才能正常操作新版数据库的问题。与其花时间来做pojo与表结构的同步方案，不如一劳永逸的使用mongodb来解决表结构问题。


###### 2.联表查询
关系型数据库的连表查询能解决很多问题，但在大公司中已不再推荐使用，因为很难做数据库优化，数据量庞大时查询时间很慢。需要连表查询时，先查出对方id集，再使用in进行包含查询，可以很方便的走索引，而且分库的时候很容易修改。这样使用的话，实际是将关系型数据库用成了近似文档型数据库，表之间不再产生关联。因此为什么不直接一步到位使用文档型数据库，彻底放弃连表查询。

基于以上理念，本orm还提供了一些小功能用于完善这种多次连接查询，在mongoHelper中有以下方法
 - 只查出表的id作为List返回：findIdsByQuery(Criteria criteria, Class<?> clazz)
 - 只查出表的某个字段作为List返回：findPropertiesByQuery(Criteria criteria,  Class<?> documentClass, String property, Class<T> propertyClass)

用法示例：

```
// 查出订单下的所有商品（OrderProduct.class为订单商品对照表）
public List<Product> getProductList(String orderId) {
	List<String> productIds = mongoHelper.findPropertiesByQuery(new CriteriaOrWrapper().eq(OrderProduct::getOrderId, orderId), OrderProduct.class,  OrderProduct::getProductId, String.class);
	return mongoHelper.findListByQuery(new CriteriaOrWrapper().in(Product::getId, productIds), Product.class);
}


// 根据产品名查出所有订单
public Page search(Page page, String keywords) {
	CriteriaOrWrapper criteriaOrWrapper = new CriteriaOrWrapper();
		
	if (StrUtil.isNotEmpty(keywords)) {
			
	    List<String> productIds = mongoHelper.findIdsByQuery(new CriteriaAndWrapper().like(Product::getName, keywords), Product.class);
	    List<String> orderIds = mongoHelper.findPropertiesByQuery(new CriteriaAndWrapper().in(OrderProduct::getProductId, productIds),OrderProduct.class,  OrderProduct::getOrderId, String.class);
	
	    criteriaOrWrapper.in(Order::getId, orderIds);
			
	}

	page = mongoHelper.findPage(criteriaOrWrapper, page, Order.class);
	return page;
}
```
###### 3.连接池
mongodb的驱动自带连接池功能，默认就使用连接池连接mongodb，也能够通过uri配置连接池大小，不再需要使用druid，hikari等连接池库了。


###### 4.集群与读写分离
Mysql实现集群和读写分离需要第三方组件，搭建难度较大，而且orm中要实现读写分离，即写入操作只在写入库操作，读取操作要分散在多个读库中，还是比较麻烦的。而mongodb天生对于集群的支持非常好，只靠自己就能建立复制，分片集群。而且集群读写偏好全部在uri中就指定清楚了。无需在orm中编写多数据源读写偏好代码。

MongoDB 5种ReadPreference模式：
 - primary 主节点，默认模式，读操作只在主节点，如果主节点不可用，报错或者抛出异常。
 - primaryPreferred 首选主节点，大多情况下读操作在主节点，如果主节点不可用，如故障转移，读操作在从节点。
 - secondary 从节点，读操作只在从节点， 如果从节点不可用，报错或者抛出异常。
 - secondaryPreferred 首选从节点，大多情况下读操作在从节点，特殊情况（如单主节点架构）读操作在主节点。
 - nearest 最邻近节点，读操作在最邻近的成员，可能是主节点或者从节点。
MongoDB 写入和事务处理只能在主节点


###### 5.一些感想
使用mongodb确实能解决很多敏捷开发中的数据库问题，多年使用mysql经验后，发现我们为了使用好mysql，给它套了太多太多东西了，mybatis、mybatis-plus、jpa、flyway、druid、mycat等等。但依然没有把mysql这头猛兽驯服，还增加了学习曲线的陡峭度。放弃mysql关系型数据库，直接投入文档性数据库mongodb的怀抱，原来很多问题都不再是问题了，用mongodb自带的特性就帮你解决了。


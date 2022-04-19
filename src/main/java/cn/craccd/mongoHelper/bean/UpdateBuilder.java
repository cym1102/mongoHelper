package cn.craccd.mongoHelper.bean;

import org.springframework.data.mongodb.core.query.Update;

import cn.craccd.mongoHelper.reflection.ReflectionUtil;
import cn.craccd.mongoHelper.reflection.SerializableFunction;

public class UpdateBuilder {
	Update update = new Update();

	public UpdateBuilder() {

	}

//	public UpdateBuilder(String key, Object value) {
//		update.set(key, value);
//	}

	public <E, R> UpdateBuilder(SerializableFunction<E, R> key, Object value) {
		update.set(ReflectionUtil.getFieldName(key), value);
	}

//	public UpdateBuilder set(String key, Object value) {
//		update.set(key, value);
//		return this;
//	}

	public <E, R> UpdateBuilder set(SerializableFunction<E, R> key, Object value) {
		update.set(ReflectionUtil.getFieldName(key), value);
		return this;
	}
	

	public <E, R> UpdateBuilder inc(SerializableFunction<E, R> key, Number count) {
		update.inc(ReflectionUtil.getFieldName(key), count);
		return this;
	}

	public Update toUpdate() {
		return update;
	}

}

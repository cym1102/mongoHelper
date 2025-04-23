package cn.craccd.mongoHelper.bean;

import org.springframework.data.mongodb.core.query.Update;

import cn.craccd.mongoHelper.reflection.ReflectionUtil;
import cn.craccd.mongoHelper.reflection.SerializableFunction;

public class UpdateBuilder {
	Update update = new Update();

	public UpdateBuilder() {

	}

	public UpdateBuilder(String key, Object value) {
		update.set(key, value);
	}

	public <E, R> UpdateBuilder(SerializableFunction<E, R> key, Object value) {
		update.set(ReflectionUtil.getFieldName(key), value);
	}

	public UpdateBuilder set(String key, Object value) {
		update.set(key, value);
		return this;
	}

	public <E, R> UpdateBuilder set(SerializableFunction<E, R> key, Object value) {
		update.set(ReflectionUtil.getFieldName(key), value);
		return this;
	}

	public UpdateBuilder inc(String key, Number count) {
		update.inc(key, count);
		return this;
	}

	public <E, R> UpdateBuilder inc(SerializableFunction<E, R> key, Number count) {
		update.inc(ReflectionUtil.getFieldName(key), count);
		return this;
	}

	public Update toUpdate() {
		return update;
	}

    public <E, R> UpdateBuilder rename(SerializableFunction<E, R> oldColumnName, String newColumnName) {
        update.rename(ReflectionUtil.getFieldName(oldColumnName), newColumnName);
        return this;
    }

    /**
     * 支持嵌套，例如data.name修改为data.firstName
     *
     * @param oldColumnName  旧列名
     * @param newColumnName  新列名
     * @return {@link UpdateBuilder }
     * @author lgc
     * @date 2025-03-27 15:13
     */
    public UpdateBuilder rename(String oldColumnName, String newColumnName) {
        update.rename(oldColumnName, newColumnName);
        return this;
    }
}

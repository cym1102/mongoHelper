package cn.craccd.mongoHelper.utils;

import java.util.Collection;

import org.springframework.data.mongodb.core.query.Criteria;

import cn.craccd.mongoHelper.reflection.ReflectionUtil;
import cn.craccd.mongoHelper.reflection.SerializableFunction;

/**
 * 查询语句生成器 AND连接
 *
 */
public class CriteriaAndWrapper extends CriteriaWrapper {

	public CriteriaAndWrapper() {
		andLink = true;
	}

	public CriteriaAndWrapper and(Criteria criteria) {
		list.add(criteria);
		return this;
	}

	public CriteriaAndWrapper and(CriteriaWrapper criteriaWrapper) {
		list.add(criteriaWrapper.build());
		return this;
	}

	/**
	 * 等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaWrapper
	 */
	public <E, R> CriteriaAndWrapper eq(SerializableFunction<E, R> column, Object params) {
		super.eq(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaWrapper
	 */
	public <E, R> CriteriaAndWrapper eq(String column, Object params) {
		super.eq(column, params);
		return this;
	}

	/**
	 * 不等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper ne(SerializableFunction<E, R> column, Object params) {
		super.ne(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 不等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper ne(String column, Object params) {
		super.ne(column, params);
		return this;
	}

	/**
	 * 小于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper lt(SerializableFunction<E, R> column, Object params) {
		super.lt(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 小于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper lt(String column, Object params) {
		super.lt(column, params);
		return this;
	}

	/**
	 * 小于或等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper lte(SerializableFunction<E, R> column, Object params) {
		super.lte(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 小于或等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper lte(String column, Object params) {
		super.lte(column, params);
		return this;
	}

	/**
	 * 大于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper gt(SerializableFunction<E, R> column, Object params) {
		super.gt(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 大于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper gt(String column, Object params) {
		super.gt(column, params);
		return this;
	}

	/**
	 * 大于或等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper gte(SerializableFunction<E, R> column, Object params) {
		super.gte(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 大于或等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper gte(String column, Object params) {
		super.gte(column, params);
		return this;
	}

	/**
	 * 包含
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper contain(SerializableFunction<E, R> column, Object params) {
		super.contain(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 包含
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper contain(String column, Object params) {
		super.contain(column, params);
		return this;
	}

	/**
	 * 包含,以或连接
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper containOr(SerializableFunction<E, R> column, Collection<?> params) {
		super.containOr(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 包含,以或连接
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper containOr(String column, Collection<?> params) {
		super.containOr(column, params);
		return this;
	}

	/**
	 * 包含,以或连接
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper containOr(SerializableFunction<E, R> column, Object[] params) {
		super.containOr(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 包含,以或连接
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper containOr(String column, Object[] params) {
		super.containOr(column, params);
		return this;
	}

	/**
	 * 包含,以且连接
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper containAnd(SerializableFunction<E, R> column, Collection<?> params) {
		super.containAnd(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 包含,以且连接
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper containAnd(String column, Collection<?> params) {
		super.containAnd(column, params);
		return this;
	}

	/**
	 * 包含,以且连接
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper containAnd(SerializableFunction<E, R> column, Object[] params) {
		super.containAnd(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 包含,以且连接
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper containAnd(String column, Object[] params) {
		super.containAnd(column, params);
		return this;
	}

	/**
	 * 相似于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper like(SerializableFunction<E, R> column, String params) {
		super.like(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 相似于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper like(String column, String params) {
		super.like(column, params);
		return this;
	}

	/**
	 * 在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper in(SerializableFunction<E, R> column, Collection<?> params) {
		super.in(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper in(String column, Collection<?> params) {
		super.in(column, params);
		return this;
	}

	/**
	 * 在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper in(SerializableFunction<E, R> column, Object[] params) {
		super.in(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper in(String column, Object[] params) {
		super.in(column, params);
		return this;
	}

	/**
	 * 不在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper nin(SerializableFunction<E, R> column, Collection<?> params) {
		super.nin(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 不在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper nin(String column, Collection<?> params) {
		super.nin(column, params);
		return this;
	}

	/**
	 * 不在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper nin(SerializableFunction<E, R> column, Object[] params) {
		super.nin(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 不在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper nin(String column, Object[] params) {
		super.nin(column, params);
		return this;
	}

	/**
	 * 为空
	 * 
	 *
	 * @param column 字段
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper isNull(SerializableFunction<E, R> column) {
		super.isNull(ReflectionUtil.getFieldName(column));
		return this;
	}

	/**
	 * 为空
	 * 
	 *
	 * @param column 字段
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper isNull(String column) {
		super.isNull(column);
		return this;
	}

	/**
	 * 不为空
	 * 
	 *
	 * @param column 字段
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper isNotNull(SerializableFunction<E, R> column) {
		super.isNotNull(ReflectionUtil.getFieldName(column));
		return this;
	}

	/**
	 * 不为空
	 * 
	 *
	 * @param column 字段
	 * @return CriteriaAndWrapper
	 */
	public <E, R> CriteriaAndWrapper isNotNull(String column) {
		super.isNotNull(column);
		return this;
	}

	/**
	 * 数组查询
	 * 
	 * @param arr    数组名
	 * @param column 字段名
	 * @param param  字段值
	 * @return
	 */
	public <E, R> CriteriaAndWrapper findArray(SerializableFunction<E, R> arr, SerializableFunction<E, R> column, Object param) {
		super.findArray(ReflectionUtil.getFieldName(arr), ReflectionUtil.getFieldName(column), param);
		return this;
	}

	/**
	 * 数组查询
	 * 
	 * @param arr    数组名
	 * @param column 字段名
	 * @param param  字段值
	 * @return
	 */
	public <E, R> CriteriaAndWrapper findArray(String arr, SerializableFunction<E, R> column, Object param) {
		super.findArray(arr, ReflectionUtil.getFieldName(column), param);
		return this;
	}

	/**
	 * 数组查询
	 * 
	 * @param arr    数组名
	 * @param column 字段名
	 * @param param  字段值
	 * @return
	 */
	public <E, R> CriteriaAndWrapper findArray(SerializableFunction<E, R> arr, String column, Object param) {
		super.findArray(ReflectionUtil.getFieldName(arr), column, param);
		return this;
	}

	/**
	 * 数组查询
	 * 
	 * @param arr    数组名
	 * @param column 字段名
	 * @param param  字段值
	 * @return
	 */
	public <E, R> CriteriaAndWrapper findArray(String arr, String column, Object param) {
		super.findArray(arr, column, param);
		return this;
	}

	/**
	 * 数组模糊查询
	 * 
	 * @param arr    数组名
	 * @param column 字段名
	 * @param param  字段值
	 * @return
	 */
	public <E, R> CriteriaAndWrapper findArrayLike(SerializableFunction<E, R> arr, SerializableFunction<E, R> column, String param) {
		super.findArrayLike(ReflectionUtil.getFieldName(arr), ReflectionUtil.getFieldName(column), param);
		return this;
	}

	/**
	 * 数组模糊查询
	 * 
	 * @param arr    数组名
	 * @param column 字段名
	 * @param param  字段值
	 * @return
	 */
	public <E, R> CriteriaAndWrapper findArrayLike(String arr, SerializableFunction<E, R> column, String param) {
		super.findArrayLike(arr, ReflectionUtil.getFieldName(column), param);
		return this;
	}

	/**
	 * 数组模糊查询
	 * 
	 * @param arr    数组名
	 * @param column 字段名
	 * @param param  字段值
	 * @return
	 */
	public <E, R> CriteriaAndWrapper findArrayLike(SerializableFunction<E, R> arr, String column, String param) {
		super.findArrayLike(ReflectionUtil.getFieldName(arr), column, param);
		return this;
	}

	/**
	 * 数组模糊查询
	 * 
	 * @param arr    数组名
	 * @param column 字段名
	 * @param param  字段值
	 * @return
	 */
	public <E, R> CriteriaAndWrapper findArrayLike(String arr, String column, String param) {
		super.findArrayLike(arr, column, param);
		return this;
	}
}

package cn.craccd.mongoHelper.bean;

import java.util.Collections;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 分页类
 * 
 */
public class Page<T> {
	@Schema(description="总记录数(非输入项)")
	Long count = 0l;
	@Schema(description="起始页,从1开始")
	Integer curr = 1; 
	@Schema(description="每页记录数,默认为10")
	Integer limit = 10;

	@Schema(description="是否查询总数量")
	Boolean queryCount = true;
	
	@Schema(description="内容列表(非输入项)")
	List<T> list = Collections.emptyList();

	
	public Boolean getQueryCount() {
		return queryCount;
	}

	public void setQueryCount(Boolean queryCount) {
		this.queryCount = queryCount;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Integer getCurr() {
		return curr;
	}

	public void setCurr(Integer curr) {
		this.curr = curr;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

}

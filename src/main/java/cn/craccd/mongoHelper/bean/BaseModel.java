package cn.craccd.mongoHelper.bean;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

public class BaseModel implements Serializable{
	@Id
	String id;
	@CreateTime
	Long createTime;
	@UpdateTime
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

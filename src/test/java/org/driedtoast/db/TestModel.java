package org.driedtoast.db;

public class TestModel {
	
	@Primary()
	private String id;

	@Indexed(name = "name", fieldNames = { "name" })
	private String name;

	private Integer count;

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
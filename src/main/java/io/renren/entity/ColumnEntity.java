package io.renren.entity;

/**
 * 列的属性
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年12月20日 上午12:01:45
 */
public class ColumnEntity {
	//列名
    private String columnName;
    //列名类型
    private String dataType;
    //列名备注
    private String comments;
    
    //属性名称(第一个字母大写)，如：user_name => UserName
    private String pascalAttrName;
    //属性名称(第一个字母小写)，如：user_name => userName
    private String camelAttrName;
    //属性类型
    private String attrType;
    //auto_increment
    private String extra;

	/**
	 * 是否允许为空
	 */
	private Boolean nullable;

	/**
	 * 字符串最大长度
	 */
	private String characterMaximumLength;


	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getPascalAttrName() {
		return pascalAttrName;
	}

	public void setPascalAttrName(String pascalAttrName) {
		this.pascalAttrName = pascalAttrName;
	}

	public String getCamelAttrName() {
		return camelAttrName;
	}

	public void setCamelAttrName(String camelAttrName) {
		this.camelAttrName = camelAttrName;
	}

	public String getAttrType() {
		return attrType;
	}

	public void setAttrType(String attrType) {
		this.attrType = attrType;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public Boolean getNullable() {
		return nullable;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	public String getCharacterMaximumLength() {
		return characterMaximumLength;
	}

	public void setCharacterMaximumLength(String characterMaximumLength) {
		this.characterMaximumLength = characterMaximumLength;
	}
}

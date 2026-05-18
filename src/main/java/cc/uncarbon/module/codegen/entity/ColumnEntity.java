package cc.uncarbon.module.codegen.entity;

import lombok.Data;

/**
 * 列的属性
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年12月20日 上午12:01:45
 */
@Data
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
}

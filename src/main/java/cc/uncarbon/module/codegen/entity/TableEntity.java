package cc.uncarbon.module.codegen.entity;

import lombok.Data;

import java.util.List;

/**
 * 表数据
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年12月20日 上午12:02:55
 */
@Data
public class TableEntity {
    //表的名称
    private String tableName;
    //表的备注
    private String comments;
    //表的主键
    private ColumnEntity pk;
    //表的列名(不包含主键)
    private List<ColumnEntity> columns;

    /**
     * 帕斯卡形式
     * 如：sys_user => SysUser
     */
    private String pascalCaseClassName;

    /**
     * 驼峰形式
     * 如：sys_user => sysUser
     */
    private String camelCaseClassName;

}

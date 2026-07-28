package cc.uncarbon.module.codegen.model.internal;

import cc.uncarbon.module.codegen.entity.ColumnEntity;
import cc.uncarbon.module.codegen.entity.TableEntity;
import cc.uncarbon.module.codegen.model.request.GenerateOptionsRequest;
import cc.uncarbon.module.codegen.model.setting.GeneratorSettings;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板占位符数据封装
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TemplatePlaceholder {

    private String tableName;
    private String comments;
    private ColumnEntity pk;

    /**
     * 帕斯卡形式
     * 如：SysUser
     */
    private String pascalCaseClassName;

    /**
     * 驼峰形式
     * 如：sysUser
     */
    private String camelCaseClassName;

    /**
     * 蛇形形式
     * 如：sys-user
     */
    private String kebabCaseClassName;

    /**
     * 全小写无符号形式
     * 如：sysuser
     */
    private String lowerCaseClassName;

    /**
     * 表字段列表
     */
    private List<ColumnEntity> columns;

    /**
     * 是否包含 BigDecimal 类型字段
     */
    private boolean hasBigDecimal;

    /**
     * 是否包含 YesOrNoEnum 类型字段
     */
    private boolean hasYesOrNoEnum;

    /**
     * 是否包含 EnabledStatusEnum 类型字段
     */
    private boolean hasEnabledStatusEnum;

    /**
     * 包名
     */
    private String packageName;

    /**
     * 模块名
     */
    private String moduleName;

    /**
     * 脚手架架构 = standalone
     */
    private boolean onStandaloneArch;

    /**
     * 生成查询条件
     */
    private boolean useQueryFormSchema;

    /**
     * 生成配套Mapper.xml
     */
    private boolean useMatchedMybatisXML;

    /**
     * 主键ID策略；对应 IdType.${idType}，AUTO=自增，ASSIGN_ID=雪花ID
     */
    private String idType;

    /**
     * 父级菜单 ID
     */
    private long parentMenuId;

    /**
     * 子级菜单 ID 1
     */
    private long childMenuId1;

    /**
     * 子级菜单 ID 2
     */
    private long childMenuId2;

    /**
     * 子级菜单 ID 3
     */
    private long childMenuId3;

    /**
     * 子级菜单 ID 4
     */
    private long childMenuId4;

    /**
     * 构造模板占位符
     */
    public static TemplatePlaceholder of(TableEntity tableEntity,
                                         GeneratorSettings settings,
                                         GenerateOptionsRequest request,
                                         ResolveTableColumnResult resolvedColumns) {
        TemplatePlaceholder placeholder = new TemplatePlaceholder();

        // 表注释预处理：末尾"表"去除，保留"报表"
        String tableComments = tableEntity.getComments();
        if (StrUtil.endWith(tableComments, "表") && !StrUtil.endWith(tableComments, "报表")) {
            tableComments = StrUtil.subBefore(tableComments, "表", true);
        }
        placeholder.setComments(tableComments);

        placeholder.setTableName(tableEntity.getTableName())
                .setPk(tableEntity.getPk())
                .setPascalCaseClassName(tableEntity.getPascalCaseClassName())
                .setCamelCaseClassName(tableEntity.getCamelCaseClassName())
                .setKebabCaseClassName(NamingCase.toKebabCase(tableEntity.getPascalCaseClassName()))
                .setLowerCaseClassName(tableEntity.getCamelCaseClassName().toLowerCase())
                .setColumns(tableEntity.getColumns())
                .setHasBigDecimal(resolvedColumns.hasBigDecimal)
                .setHasYesOrNoEnum(resolvedColumns.hasYesOrNoEnum)
                .setHasEnabledStatusEnum(resolvedColumns.hasEnabledStatusEnum)
                .setPackageName(settings.getPackageName())
                .setModuleName(settings.getModuleName())
                .setOnStandaloneArch("standalone".equalsIgnoreCase(request.getBackendArch()))
                .setUseQueryFormSchema(request.useQueryFormSchema())
                .setUseMatchedMybatisXML(request.useMatchedMybatisXML())
                .setIdType(request.useAutoIncrementId() ? "AUTO" : "ASSIGN_ID");

        // 生成后台管理菜单主键ID
        long menuId = Long.parseLong(LocalDateTimeUtil.format(LocalDateTimeUtil.now(),
                DatePattern.PURE_DATETIME_MS_FORMATTER));
        placeholder.setParentMenuId(menuId)
                .setChildMenuId1(menuId + 1)
                .setChildMenuId2(menuId + 2)
                .setChildMenuId3(menuId + 3)
                .setChildMenuId4(menuId + 4);
        return placeholder;
    }

    /**
     * 转换为 VelocityContext 使用的 Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> ret = new HashMap<>();
        BeanUtil.copyProperties(this, ret);
        return ret;
    }
}

package cc.uncarbon.module.codegen.utils;

import cc.uncarbon.module.codegen.model.internal.ResolveTableColumnResult;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cc.uncarbon.module.codegen.entity.ColumnEntity;
import cc.uncarbon.module.codegen.entity.TableEntity;
import cc.uncarbon.module.codegen.model.internal.TemplatePlaceholder;
import cc.uncarbon.module.codegen.model.setting.GeneratorSettings;
import cc.uncarbon.module.codegen.model.request.GenerateOptionsRequest;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器   工具类
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年12月19日 下午11:40:24
 */
public class GenUtils {

    public static List<String> getTemplates() {
        List<String> templates = new ArrayList<>(100);
        // 后端
        final String backendPathPrefix = "template/backend";
        FileUtil.walkFiles(FileUtil.file(backendPathPrefix), file -> templates.add(
                backendPathPrefix + CharSequenceUtil.subAfter(file.getPath(), backendPathPrefix, true)
        ));

        // 前端
        final String frontendPathPrefix = "template/frontend";
        FileUtil.walkFiles(FileUtil.file(backendPathPrefix), file -> templates.add(
                frontendPathPrefix + CharSequenceUtil.subAfter(file.getPath(), frontendPathPrefix, true)
        ));
        return templates;
    }

    /**
     * 生成代码
     */
    public static void generatorCode(Map<String, String> table,
                                     List<Map<String, String>> columns,
                                     ZipOutputStream zip,
                                     GenerateOptionsRequest request,
                                     GeneratorSettings settings
    ) {
        //表信息
        TableEntity tableEntity = new TableEntity();
        tableEntity.setTableName(table.get("tableName"));
        tableEntity.setComments(table.get("tableComment"));
        //表名转换成Java类名
        String[] tablePrefixArray = settings.getTablePrefix() != null ? settings.getTablePrefix().split(",") : new String[0];
        String pascalCaseClassName = tableToJava(tableEntity.getTableName(), tablePrefixArray);
        tableEntity.setPascalCaseClassName(pascalCaseClassName);
        tableEntity.setCamelCaseClassName(CharSequenceUtil.lowerFirst(pascalCaseClassName));

        //列信息
        var resolvedColumns = resolveTableColumn(request, tableEntity, columns);
        tableEntity.setColumns(resolvedColumns.columsList);

        //没主键，则第一个字段为主键
        if (tableEntity.getPk() == null) {
            tableEntity.setPk(CollUtil.getFirst(tableEntity.getColumns()));
        }

        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("resource.loader.file.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        VelocityEngine engine = new VelocityEngine(prop);

        // 构建模板占位符
        TemplatePlaceholder placeholder = TemplatePlaceholder.of(tableEntity, settings, request, resolvedColumns);
        VelocityContext context = new VelocityContext(placeholder.toMap());

        //获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = engine.getTemplate(template, StandardCharsets.UTF_8.name());
            tpl.merge(context, sw);

            try {
                String fileName = getFileName(template, tableEntity.getPascalCaseClassName(),
                        settings.getPackageName(), settings.getModuleName(), placeholder);
                if (fileName == null) {
                    continue;
                }
                //添加到zip
                zip.putNextEntry(new ZipEntry(fileName));
                zip.write(sw.toString().getBytes(StandardCharsets.UTF_8));
                IoUtil.close(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new ProjectBusinessException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
            }
        }
    }

    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return NamingCase.toPascalCase(columnName);
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String[] tablePrefixArray) {
        if (ArrayUtil.isNotEmpty(tablePrefixArray)) {
            for (String tablePrefix : tablePrefixArray) {
                if (tableName.startsWith(tablePrefix)) {
                    tableName = tableName.replaceFirst(tablePrefix, "");
                }
            }
        }
        return columnToJava(tableName);
    }

    /**
     * 获取DB类型 -> Java类型映射
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getTypeMapping() {
        Yaml yaml = new Yaml();
        try (InputStream is = GenUtils.class.getClassLoader().getResourceAsStream("type-mapping.yml")) {
            if (is == null) {
                throw new ProjectBusinessException("找不到 type-mapping.yml 配置文件");
            }
            return yaml.loadAs(is, Map.class);
        } catch (IOException e) {
            throw new ProjectBusinessException("读取 type-mapping.yml 失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String pascalCaseClassName, String packageName,
                                     String moduleName, TemplatePlaceholder placeholder) {
        // 路径分隔符
        final String sep = File.separator;
        /*
        多级文件夹，如src/main/cc/uncarbon/module/
        若需要可以自行加上
         */
        String packagePath = sep + "main" + sep + "java" + sep;
        if (StrUtil.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", sep) + sep + moduleName + sep;
        }

        /*
        后端代码
         */
        final String backendPathPrefix = "后端代码" + sep;
        if (template.contains("AdminController.java.vm")) {
            return backendPathPrefix + "controller" + sep + moduleName + sep + "Admin" + pascalCaseClassName + "Controller.java";
        }

        if (template.contains("AdminListQuery.java.vm")) {
            return backendPathPrefix + "model" + sep + "query" + sep + "Admin" + pascalCaseClassName + "ListQuery.java";
        }

        if (template.contains("AdminUpsertRequest.java.vm")) {
            return backendPathPrefix + "model" + sep + "request" + sep + "Admin" + pascalCaseClassName + "UpsertRequest.java";
        }

        if (template.contains("DTO.java.vm")) {
            return backendPathPrefix + "model" + sep + "valueobj" + sep + pascalCaseClassName + "DTO.java";
        }

        if (template.contains("Entity.java.vm")) {
            return backendPathPrefix + "dal" + sep + "entity" + sep + pascalCaseClassName + "Entity.java";
        }

        if (template.contains("Mapper.java.vm")) {
            return backendPathPrefix + "dal" + sep + "mapper" + sep + pascalCaseClassName + "Mapper.java";
        }

        if (template.contains("Mapper.xml.vm") && placeholder.isUseMatchedMybatisXML()) {
            return backendPathPrefix + "dal" + sep + "mapper" + sep + pascalCaseClassName + "Mapper.xml";
        }

        if (template.contains("ServiceImpl.java.vm")) {
            return backendPathPrefix + "service" + sep + "impl" + sep + pascalCaseClassName + "ServiceImpl.java";
        }

        if (template.contains("ServiceInterface.java.vm")) {
            return backendPathPrefix + "service" + sep + pascalCaseClassName + "Service.java";
        }

        if (template.contains("sys_menu.sql.vm")) {
            return backendPathPrefix + "后台管理菜单-" + pascalCaseClassName + ".sql";
        }

        /*
        前端代码
         */
//        String frontendPathPrefix = "前端代码" + sep + "src" + sep;
//        if (template.contains("Api.ts.vm")) {
//            return frontendPathPrefix + "api" + sep + moduleName + sep + pascalCaseClassName + "Api.ts";
//        }
//
//        if (template.contains("Model.ts.vm")) {
//            return frontendPathPrefix + "api" + sep + moduleName + sep + "model" + sep + pascalCaseClassName + "Model.ts";
//        }
//
//        if (template.contains("data.ts.vm")) {
//            return frontendPathPrefix + "views" + sep + moduleName + sep + pascalCaseClassName + sep + "data.ts";
//        }
//
//        if (template.contains("detail-drawer.vue.vm")) {
//            return frontendPathPrefix + "views" + sep + moduleName + sep + pascalCaseClassName + sep + "detail-drawer.vue";
//        }
//
//        if (template.contains("update-drawer.vue.vm")) {
//            return frontendPathPrefix + "views" + sep + moduleName + sep + pascalCaseClassName + sep + "update-drawer.vue";
//        }
//
//        if (template.contains("index.vue.vm")) {
//            return frontendPathPrefix + "views" + sep + moduleName + sep + pascalCaseClassName + sep + "index.vue";
//        }
        return null;
    }

    private static ResolveTableColumnResult resolveTableColumn(GenerateOptionsRequest request,
                                                               TableEntity tableEntity, List<Map<String, String>> columns) {
        var ret = new ResolveTableColumnResult();
        Map<String, String> typeMapping = getTypeMapping();
        List<ColumnEntity> columsList = new ArrayList<>(columns.size());
        for (Map<String, String> column : columns) {
            // 表中字段名
            String tableColumnName = column.get("columnName");
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setColumnName(tableColumnName);
            columnEntity.setDataType(column.get("dataType"));
            columnEntity.setComments(column.get("columnComment"));
            columnEntity.setExtra(column.get("extra"));

            //列名转换成Java属性名
            String attrName = columnToJava(columnEntity.getColumnName());
            columnEntity.setPascalAttrName(attrName);
            columnEntity.setCamelAttrName(StrUtil.lowerFirst(attrName));

            //列的数据类型，转换成Java类型
            String attrType = typeMapping.getOrDefault(columnEntity.getDataType(), columnToJava(columnEntity.getDataType()));
            columnEntity.setAttrType(attrType);

            // 是否允许空值
            columnEntity.setNullable("true".equalsIgnoreCase(column.getOrDefault("nullable", "true")));

            // 字符串最大长度
            columnEntity.setCharacterMaximumLength(column.getOrDefault("characterMaximumLength", ""));

            if ("BigDecimal".equals(attrType)) {
                ret.hasBigDecimal = true;
            }
            if (request.useYesOrNoEnum()) {
                if (StrUtil.endWithIgnoreCase(tableColumnName, "_flag")
                        || StrUtil.startWithIgnoreCase(tableColumnName, "is_")
                ) {
                    ret.hasYesOrNoEnum = true;
                    columnEntity.setAttrType("YesOrNoEnum");
                }
            }
            if (request.useEnabledStatusEnum()
                    && StrUtil.endWithIgnoreCase(tableColumnName, "status")
            ) {
                ret.hasEnabledStatusEnum = true;
                columnEntity.setAttrType("EnabledStatusEnum");
            }
            //是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() == null) {
                tableEntity.setPk(columnEntity);
            }
            columsList.add(columnEntity);
        }
        ret.columsList = columsList;
        return ret;
    }
}

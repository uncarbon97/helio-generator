package io.renren.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.StrUtil;
import io.renren.entity.ColumnEntity;
import io.renren.entity.TableEntity;
import io.renren.model.setting.GeneratorSettings;
import io.renren.model.request.GenerateOptionsRequest;
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

    public static List<String> getTemplates(GenerateOptionsRequest dto) {
        List<String> templates = new ArrayList<>();
        /*
        后端
         */
        templates.add("template/backend/Entity.java.vm");
        templates.add("template/backend/Mapper.java.vm");
        templates.add("template/backend/ServiceImpl.java.vm");
        templates.add("template/backend/AdminController.java.vm");
        templates.add("template/backend/AdminListDTO.java.vm");
        templates.add("template/backend/AdminInsertOrUpdateDTO.java.vm");
        templates.add("template/backend/BO.java.vm");
        templates.add("template/backend/sys_menu.sql.vm");
        templates.add("template/backend/UnitTest.java.vm");

        if (Constant.GENERATE_TYPE_CLOUD.equals(dto.getGenerateType())) {
            templates.add("template/backend/Facade.java.vm");
            templates.add("template/backend/FacadeImpl.java.vm");
        }

        if (dto.getServiceAndImpl()) {
            templates.add("template/backend/ServiceInterface.java.vm");
        }

        if (dto.getMybatisXML()) {
            templates.add("template/backend/Mapper.xml.vm");
        }

        /*
        前端(Vben Admin)
         */
        templates.add("template/frontend/api/Api.ts.vm");
        templates.add("template/frontend/api/Model.ts.vm");
        templates.add("template/frontend/views/data.ts.vm");
        templates.add("template/frontend/views/detail-drawer.vue.vm");
        templates.add("template/frontend/views/update-drawer.vue.vm");
        templates.add("template/frontend/views/index.vue.vm");

        return templates;
    }

    /**
     * 生成代码
     */
    public static void generatorCode(Map<String, String> table,
                                     List<Map<String, String>> columns,
                                     ZipOutputStream zip,
                                     GenerateOptionsRequest dto,
                                     GeneratorSettings settings
    ) {
        Map<String, String> typeMapping = getTypeMapping();
        boolean hasBigDecimal = false;
        boolean hasList = false;

        // configured from dto
        boolean queryFormSchemaFlag = dto.getQueryFormSchema();
        boolean serviceAndImplFlag = dto.getServiceAndImpl();
        boolean useYesOrNoEnum = dto.getUseYesOrNoEnum();
        boolean useEnabledStatusEnum = dto.getUseEnabledStatusEnum();
        //表信息
        TableEntity tableEntity = new TableEntity();
        tableEntity.setTableName(table.get("tableName"));
        tableEntity.setComments(table.get("tableComment"));
        //表名转换成Java类名
        String[] tablePrefixArray = settings.getTablePrefix() != null ? settings.getTablePrefix().split(",") : new String[0];
        String className = tableToJava(tableEntity.getTableName(), tablePrefixArray);
        tableEntity.setClassName(className);
        tableEntity.setClassname(StrUtil.lowerFirst(className));

        //列信息
        List<ColumnEntity> columsList = new ArrayList<>();
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

            if (!hasBigDecimal && "BigDecimal".equals(attrType)) {
                hasBigDecimal = true;
            }
            if (!hasList && "array".equals(columnEntity.getExtra())) {
                hasList = true;
            }
            if (useYesOrNoEnum) {
                if (StrUtil.endWithIgnoreCase(tableColumnName, "_flag")
                        || StrUtil.startWithIgnoreCase(tableColumnName, "is_")
                ) {
                    columnEntity.setAttrType("YesOrNoEnum");
                }
            }
            if (
                    useEnabledStatusEnum
                    && StrUtil.endWithIgnoreCase(tableColumnName, "status")
            ) {
                columnEntity.setAttrType("EnabledStatusEnum");
            }
            //是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() == null) {
                tableEntity.setPk(columnEntity);
            }

            columsList.add(columnEntity);
        }
        tableEntity.setColumns(columsList);

        //没主键，则第一个字段为主键
        if (tableEntity.getPk() == null) {
            tableEntity.setPk(tableEntity.getColumns().get(0));
        }

        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("resource.loader.file.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        VelocityEngine engine = new VelocityEngine(prop);

        // 封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableEntity.getTableName());

        // Helio: 如果最后一个字为「表」，但不以「报表」结尾，去除最后一个「表」字
        String tableComments = tableEntity.getComments();
        if (StrUtil.endWith(tableComments, "表") && !StrUtil.endWith(tableComments, "报表")) {
            tableComments = StrUtil.subBefore(tableComments, "表", true);
        }
        map.put("comments", tableComments);
        map.put("pk", tableEntity.getPk());
        map.put("className", tableEntity.getClassName());
        map.put("classname", tableEntity.getClassname());
        map.put("pathName", tableEntity.getClassname().toLowerCase());
        map.put("columns", tableEntity.getColumns());
        map.put("hasBigDecimal", hasBigDecimal);
        map.put("hasList", hasList);
        map.put("package", settings.getPackageName());
        map.put("moduleName", settings.getModuleName());
        // className 的 kebab-case 形式
        map.put("kebabCaseClassName", NamingCase.toKebabCase(tableEntity.getClassName()));

        // configured from dto
        map.put("generateType", dto.getGenerateType());
        map.put("helioFrameworkVersion", dto.getHelioFrameworkVersion());
        map.put("queryFormSchemaFlag", queryFormSchemaFlag);
        map.put("serviceAndImplFlag", serviceAndImplFlag);
        map.put("useYesOrNoEnum", useYesOrNoEnum);
        map.put("useEnabledStatusEnum", useEnabledStatusEnum);

        // 生成后台管理菜单主键ID
        long menuId = Long.parseLong(LocalDateTimeUtil.format(LocalDateTimeUtil.now(), DatePattern.PURE_DATETIME_MS_FORMATTER));
        map.put("parentMenuId", menuId);
        map.put("childMenuId1", menuId + 1);
        map.put("childMenuId2", menuId + 2);
        map.put("childMenuId3", menuId + 3);
        map.put("childMenuId4", menuId + 4);

        VelocityContext context = new VelocityContext(map);

        //获取模板列表
        List<String> templates = getTemplates(dto);
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = engine.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);

            try {
                //添加到zip
                zip.putNextEntry(
                        new ZipEntry(
                                getFileName(
                                        template,
                                        tableEntity.getClassName(),
                                        settings.getPackageName(),
                                        settings.getModuleName(),
                                        dto
                                )
                        )
                );
                zip.write(sw.toString().getBytes(StandardCharsets.UTF_8));
                IoUtil.close(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new RRException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
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
        if (null != tablePrefixArray && tablePrefixArray.length > 0) {
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
                throw new RRException("找不到 type-mapping.yml 配置文件");
            }
            return yaml.loadAs(is, Map.class);
        } catch (IOException e) {
            throw new RRException("读取 type-mapping.yml 失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String className, String packageName, String moduleName,
                                     GenerateOptionsRequest dto) {
        // 路径分隔符
        final String pathSeparator = File.separator;
        /*
        多级文件夹，如src/main/cc/uncarbon/module/
        若需要可以自行加上
         */
        String packagePath = pathSeparator + "main" + pathSeparator + "java" + pathSeparator;
        if (StrUtil.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", pathSeparator) + pathSeparator + moduleName + pathSeparator;
        }

        /*
        后端代码
         */
        String backendPathPrefix = "后端代码" + pathSeparator;

        if (template.contains("Entity.java.vm")) {
            return backendPathPrefix + "entity" + pathSeparator + className + "Entity.java";
        }

        if (template.contains("Mapper.java.vm")) {
            return backendPathPrefix + "mapper" + pathSeparator + className + "Mapper.java";
        }

        if (template.contains("Mapper.xml.vm")) {
            return backendPathPrefix + "mapper" + pathSeparator + "xml" + pathSeparator + className + "Mapper.xml";
        }

        if (template.contains("ServiceInterface.java.vm")) {
            return backendPathPrefix + "service" + pathSeparator + className + "Service.java";
        }

        if (template.contains("ServiceImpl.java.vm")) {
            if (dto.getServiceAndImpl()) {
                // 新建 impl 目录，以及文件名以 Impl 结尾
                return backendPathPrefix + "service" + pathSeparator + "impl" + pathSeparator + className + "ServiceImpl.java";
            } else {
                return backendPathPrefix + "service" + pathSeparator + className + "Service.java";
            }
        }

        if (template.contains("Facade.java.vm")) {
            return backendPathPrefix + "facade" + pathSeparator + className + "Facade.java";
        }

        if (template.contains("FacadeImpl.java.vm")) {
            return backendPathPrefix + "biz" + pathSeparator + className + "FacadeImpl.java";
        }

        if (template.contains("AdminController.java.vm")) {
            return backendPathPrefix + "web" + pathSeparator + moduleName + pathSeparator + "Admin" + className + "Controller.java";
        }

        if (template.contains("AdminListDTO.java.vm")) {
            return backendPathPrefix + "model" + pathSeparator + "request" + pathSeparator + "Admin" + className + "ListDTO.java";
        }

        if (template.contains("AdminInsertOrUpdateDTO.java.vm")) {
            return backendPathPrefix + "model" + pathSeparator + "request" + pathSeparator + "Admin" + className + "InsertOrUpdateDTO.java";
        }

        if (template.contains("BO.java.vm")) {
            return backendPathPrefix + "model" + pathSeparator + "response" + pathSeparator + className + "BO.java";
        }

        if (template.contains("sys_menu.sql.vm")) {
            return backendPathPrefix + "后台管理菜单-" + className + ".sql";
        }

        if (template.contains("UnitTest.java.vm")) {
            return backendPathPrefix + pathSeparator + className + "UnitTest.java";
        }

        /*
        前端代码(Vben Admin)
         */
        String frontendPathPrefix = "前端代码" + pathSeparator + "src" + pathSeparator;
        if (template.contains("Api.ts.vm")) {
            return frontendPathPrefix + "api" + pathSeparator + moduleName + pathSeparator + className + "Api.ts";
        }

        if (template.contains("Model.ts.vm")) {
            return frontendPathPrefix + "api" + pathSeparator + moduleName + pathSeparator + "model" + pathSeparator + className + "Model.ts";
        }

        if (template.contains("data.ts.vm")) {
            return frontendPathPrefix + "views" + pathSeparator + moduleName + pathSeparator + className + pathSeparator + "data.ts";
        }

        if (template.contains("detail-drawer.vue.vm")) {
            return frontendPathPrefix + "views" + pathSeparator + moduleName + pathSeparator + className + pathSeparator + "detail-drawer.vue";
        }

        if (template.contains("update-drawer.vue.vm")) {
            return frontendPathPrefix + "views" + pathSeparator + moduleName + pathSeparator + className + pathSeparator + "update-drawer.vue";
        }

        if (template.contains("index.vue.vm")) {
            return frontendPathPrefix + "views" + pathSeparator + moduleName + pathSeparator + className + pathSeparator + "index.vue";
        }


        return null;
    }
}

package io.renren.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import io.renren.config.MongoManager;
import io.renren.entity.ColumnEntity;
import io.renren.entity.TableEntity;
import io.renren.entity.mongo.MongoDefinition;
import io.renren.entity.mongo.MongoGeneratorEntity;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
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

    private static String currentTableName;

    public static List<String> getTemplates(String generateType) {
        List<String> templates = new ArrayList<>();
        /*
        后端
         */
        templates.add("template/backend/Entity.java.vm");
        templates.add("template/backend/Mapper.java.vm");
        templates.add("template/backend/Service.java.vm");
        templates.add("template/backend/AdminController.java.vm");
        templates.add("template/backend/AdminListDTO.java.vm");
        templates.add("template/backend/AdminInsertOrUpdateDTO.java.vm");
        templates.add("template/backend/BO.java.vm");
        templates.add("template/backend/sys_menu.sql.vm");
        templates.add("template/backend/UnitTest.java.vm");

        if (Constant.GENERATE_TYPE_CLOUD.equals(generateType)) {
            templates.add("template/backend/Facade.java.vm");
            templates.add("template/backend/FacadeImpl.java.vm");
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

        if (MongoManager.isMongo()) {
            // mongo不需要mapper、sql   实体类需要替换
            templates.remove(0);
            templates.remove(1);
            templates.remove(2);
            templates.add("template/useless/MongoEntity.java.vm");
        }
        return templates;
    }

    public static List<String> getMongoChildTemplates() {
        List<String> templates = new ArrayList<String>();
        templates.add("template/useless/MongoChildrenEntity.java.vm");
        return templates;
    }

    /**
     * 生成代码
     */
    public static void generatorCode(Map<String, String> table,
                                     List<Map<String, String>> columns,
                                     ZipOutputStream zip,
                                     String generateType,
                                     String queryFormSchema
    ) {
        //配置信息
        Configuration config = getConfig();
        boolean hasBigDecimal = false;
        boolean hasList = false;
        boolean hasGenericStatusEnum = false;
        boolean hasQueryFormSchema = "checked".equals(queryFormSchema);
        //表信息
        TableEntity tableEntity = new TableEntity();
        tableEntity.setTableName(table.get("tableName"));
        tableEntity.setComments(table.get("tableComment"));
        //表名转换成Java类名
        String className = tableToJava(tableEntity.getTableName(), config.getStringArray("tablePrefix"));
        tableEntity.setClassName(className);
        tableEntity.setClassname(StringUtils.uncapitalize(className));

        //列信息
        List<ColumnEntity> columsList = new ArrayList<>();
        for (Map<String, String> column : columns) {

            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setColumnName(column.get("columnName"));
            columnEntity.setDataType(column.get("dataType"));
            columnEntity.setComments(column.get("columnComment"));
            columnEntity.setExtra(column.get("extra"));

            //列名转换成Java属性名
            String attrName = columnToJava(columnEntity.getColumnName());
            columnEntity.setPascalAttrName(attrName);
            columnEntity.setCamelAttrName(StringUtils.uncapitalize(attrName));

            //列的数据类型，转换成Java类型
            String attrType = config.getString(columnEntity.getDataType(), columnToJava(columnEntity.getDataType()));
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
            if ("status".equalsIgnoreCase(attrName)) {
                // Helio: 特别指定为 GenericStatusEnum 类型
                hasGenericStatusEnum = true;
                columnEntity.setAttrType("GenericStatusEnum");
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
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);
        String mainPath = config.getString("mainPath");
        mainPath = StringUtils.isBlank(mainPath) ? "cc.uncarbon" : mainPath;

        // 封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableEntity.getTableName());
        map.put("comments", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("className", tableEntity.getClassName());
        map.put("classname", tableEntity.getClassname());
        map.put("pathName", tableEntity.getClassname().toLowerCase());
        map.put("columns", tableEntity.getColumns());
        map.put("hasBigDecimal", hasBigDecimal);
        map.put("hasList", hasList);
        map.put("hasGenericStatusEnum", hasGenericStatusEnum);
        map.put("mainPath", mainPath);
        map.put("package", config.getString("package"));
        map.put("moduleName", config.getString("moduleName"));
        map.put("author", config.getString("author"));
        map.put("generateType", generateType);
        map.put("hasQueryFormSchema", hasQueryFormSchema);

        // 生成后台管理菜单主键ID
        Snowflake snowflake = IdUtil.getSnowflake(30L, 30L);
        long snowflakeId = snowflake.nextId();
        map.put("parentMenuId", snowflakeId);
        map.put("childMenuId1", snowflakeId + 1);
        map.put("childMenuId2", snowflakeId + 2);
        map.put("childMenuId3", snowflakeId + 3);
        map.put("childMenuId4", snowflakeId + 4);

        VelocityContext context = new VelocityContext(map);

        //获取模板列表
        List<String> templates = getTemplates(generateType);
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);

            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, tableEntity.getClassName(), config.getString("package"), config.getString("moduleName"))));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new RRException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
            }
        }
    }

    /**
     * 生成mongo其他实体类的代码
     */
    public static void generatorMongoCode(String[] tableNames, ZipOutputStream zip) {
        for (String tableName : tableNames) {
            MongoDefinition info = MongoManager.getInfo(tableName);
            currentTableName = tableName;
            List<MongoGeneratorEntity> childrenInfo = info.getChildrenInfo(tableName);
            childrenInfo.remove(0);
            for (MongoGeneratorEntity mongoGeneratorEntity : childrenInfo) {
                generatorChildrenBeanCode(mongoGeneratorEntity, zip);
            }
        }
    }

    private static void generatorChildrenBeanCode(MongoGeneratorEntity mongoGeneratorEntity, ZipOutputStream zip) {
        //配置信息
        Configuration config = getConfig();
        boolean hasList = false;
        //表信息
        TableEntity tableEntity = mongoGeneratorEntity.toTableEntity();
        //表名转换成Java类名
        String className = tableToJava(tableEntity.getTableName(), config.getStringArray("tablePrefix"));
        tableEntity.setClassName(className);
        tableEntity.setClassname(StringUtils.uncapitalize(className));
        //列信息
        List<ColumnEntity> columsList = new ArrayList<>();
        for (Map<String, String> column : mongoGeneratorEntity.getColumns()) {
            ColumnEntity columnEntity = new ColumnEntity();
            String columnName = column.get("columnName");
            if (columnName.contains(".")) {
                columnName = columnName.substring(columnName.lastIndexOf(".") + 1);
            }
            columnEntity.setColumnName(columnName);
            columnEntity.setDataType(column.get("dataType"));
            columnEntity.setExtra(column.get("extra"));

            //列名转换成Java属性名
            String attrName = columnToJava(columnEntity.getColumnName());
            columnEntity.setPascalAttrName(attrName);
            columnEntity.setCamelAttrName(StringUtils.uncapitalize(attrName));

            //列的数据类型，转换成Java类型
            String attrType = config.getString(columnEntity.getDataType(), columnToJava(columnEntity.getDataType()));
            columnEntity.setAttrType(attrType);

            if (!hasList && "array".equals(columnEntity.getExtra())) {
                hasList = true;
            }
            columsList.add(columnEntity);
        }
        tableEntity.setColumns(columsList);

        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);
        String mainPath = config.getString("mainPath");
        mainPath = StringUtils.isBlank(mainPath) ? "io.renren" : mainPath;
        //封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableEntity.getTableName());
        map.put("comments", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("className", tableEntity.getClassName());
        map.put("classname", tableEntity.getClassname());
        map.put("pathName", tableEntity.getClassname().toLowerCase());
        map.put("columns", tableEntity.getColumns());
        map.put("hasList", hasList);
        map.put("mainPath", mainPath);
        map.put("package", config.getString("package"));
        map.put("moduleName", config.getString("moduleName"));
        map.put("author", config.getString("author"));
        map.put("email", config.getString("email"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        VelocityContext context = new VelocityContext(map);

        //获取模板列表
        List<String> templates = getMongoChildTemplates();
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);
            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, tableEntity.getClassName(), config.getString("package"), config.getString("moduleName"))));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
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
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
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
     * 获取配置信息
     */
    public static Configuration getConfig() {
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new RRException("获取配置文件失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String className, String packageName, String moduleName) {
        /*
        多级文件夹，如src/main/cc/uncarbon/module/
        若需要可以自行加上
         */
        String packagePath = File.separator + "main" + File.separator + "java" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator + moduleName + File.separator;
        }

        /*
        后端代码
         */
        String backendPathPrefix = "后端代码" + File.separator;

        if (template.contains("MongoChildrenEntity.java.vm")) {
            return backendPathPrefix + "entity" + File.separator + "inner" + File.separator + currentTableName + File.separator + splitInnerName(className) + "InnerEntity.java";
        }
        if (template.contains("Entity.java.vm") || template.contains("MongoEntity.java.vm")) {
            return backendPathPrefix + "entity" + File.separator + className + "Entity.java";
        }

        if (template.contains("Mapper.java.vm")) {
            return backendPathPrefix + "mapper" + File.separator + className + "Mapper.java";
        }

        if (template.contains("Service.java.vm")) {
            return backendPathPrefix + "service" + File.separator + className + "Service.java";
        }

        if (template.contains("Facade.java.vm")) {
            return backendPathPrefix + "facade" + File.separator + className + "Facade.java";
        }

        if (template.contains("FacadeImpl.java.vm")) {
            return backendPathPrefix + "biz" + File.separator + className + "FacadeImpl.java";
        }

        if (template.contains("AdminController.java.vm")) {
            return backendPathPrefix + "web" + File.separator + "Admin" + className + "Controller.java";
        }

        if (template.contains("AdminListDTO.java.vm")) {
            return backendPathPrefix + "model" + File.separator + "request" + File.separator + "AdminList" + className + "DTO.java";
        }

        if (template.contains("AdminInsertOrUpdateDTO.java.vm")) {
            return backendPathPrefix + "model" + File.separator + "request" + File.separator + "AdminInsertOrUpdate" + className + "DTO.java";
        }

        if (template.contains("BO.java.vm")) {
            return backendPathPrefix + "model" + File.separator + "response" + File.separator + className + "BO.java";
        }

        if (template.contains("sys_menu.sql.vm")) {
            return backendPathPrefix + "后台管理菜单-" + className + ".sql";
        }

        if (template.contains("UnitTest.java.vm")) {
            return backendPathPrefix + File.separator + className + "UnitTest.java";
        }

        /*
        前端代码(Vben Admin)
         */
        String frontendPathPrefix = "前端代码" + File.separator + "src" + File.separator;
        if (template.contains("Api.ts.vm")) {
            return frontendPathPrefix + "api" + File.separator + moduleName + File.separator + className + "Api.ts";
        }

        if (template.contains("Model.ts.vm")) {
            return frontendPathPrefix + "api" + File.separator + moduleName + File.separator + "model" + File.separator + className + "Model.ts";
        }

        if (template.contains("data.ts.vm")) {
            return frontendPathPrefix + "views" + File.separator + moduleName + File.separator + className + File.separator + "data.ts";
        }

        if (template.contains("detail-drawer.vue.vm")) {
            return frontendPathPrefix + "views" + File.separator + moduleName + File.separator + className + File.separator + "detail-drawer.vue";
        }

        if (template.contains("update-drawer.vue.vm")) {
            return frontendPathPrefix + "views" + File.separator + moduleName + File.separator + className + File.separator + "update-drawer.vue";
        }

        if (template.contains("index.vue.vm")) {
            return frontendPathPrefix + "views" + File.separator + moduleName + File.separator + className + File.separator + "index.vue";
        }


        return null;
    }

    private static String splitInnerName(String name) {
        name = name.replaceAll("\\.", "_");
        return name;
    }
}

package io.renren.model.setting;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class GeneratorSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    private String mainPath = "cc.uncarbon";

    @JSONField(name = "package")
    private String packageName = "cc.uncarbon.module";

    private String moduleName = "sys";

    private String tablePrefix = "tb_";

    public String getMainPath() {
        return mainPath;
    }

    public void setMainPath(String mainPath) {
        this.mainPath = mainPath;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }
}

package cc.uncarbon.module.codegen.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class GenerateOptionsRequest implements Serializable {

    /**
     * 脚手架架构
     * 目前只有 standalone
     */
    private String backendArch;

    /**
     * 生成分页列表查询条件
     */
    private boolean useQueryFormSchema;

    /**
     * 生成配套Mapper.xml
     */
    private boolean useMatchedMybatisXML;

    /**
     * 使用YesOrNoEnum枚举
     */
    private boolean useYesOrNoEnum;

    /**
     * 使用EnabledStatusEnum枚举
     */
    private boolean useEnabledStatusEnum;

    /**
     * 主键ID使用自增
     */
    private boolean useAutoIncrementId;

    public boolean useQueryFormSchema() {
        return useQueryFormSchema;
    }

    public boolean useMatchedMybatisXML() {
        return useMatchedMybatisXML;
    }

    public boolean useYesOrNoEnum() {
        return useYesOrNoEnum;
    }

    public boolean useEnabledStatusEnum() {
        return useEnabledStatusEnum;
    }

    public boolean useAutoIncrementId() {
        return useAutoIncrementId;
    }
}

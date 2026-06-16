package cc.uncarbon.module.codegen.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

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
    @Accessors(fluent = true)
    private boolean useQueryFormSchema;

    /**
     * 生成配套Mapper.xml
     */
    @Accessors(fluent = true)
    private boolean useMatchedMybatisXML;

    /**
     * 使用YesOrNoEnum枚举
     */
    @Accessors(fluent = true)
    private boolean useYesOrNoEnum;

    /**
     * 使用EnabledStatusEnum枚举
     */
    @Accessors(fluent = true)
    private boolean useEnabledStatusEnum;

}

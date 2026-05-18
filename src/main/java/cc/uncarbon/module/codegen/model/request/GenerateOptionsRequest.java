package cc.uncarbon.module.codegen.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class GenerateOptionsRequest implements Serializable {

    /**
     * 脚手架架构：单体or微服务
     */
    private String generateType;

    /**
     * 脚手架版本：v2 or v1
     */
    private String helioFrameworkVersion;

    /**
     * 生成分页列表查询条件
     */
    private Boolean queryFormSchema;

    /**
     * 按Service+Impl范式生成
     */
    private Boolean serviceAndImpl;

    /**
     * 生成配套Mapper.xml
     */
    private Boolean mybatisXML;

    /**
     * 对_flag结尾或is_开头的字段，Java代码使用YesOrNoEnum枚举
     */
    private Boolean useYesOrNoEnum;

    /**
     * 对status结尾的字段，Java代码使用EnabledStatusEnum枚举
     */
    private Boolean useEnabledStatusEnum;
}

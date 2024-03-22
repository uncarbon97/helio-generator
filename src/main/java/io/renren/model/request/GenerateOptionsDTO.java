package io.renren.model.request;

import java.io.Serializable;

public class GenerateOptionsDTO implements Serializable {

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


    public String getGenerateType() {
        return generateType;
    }

    public void setGenerateType(String generateType) {
        this.generateType = generateType;
    }

    public String getHelioFrameworkVersion() {
        return helioFrameworkVersion;
    }

    public void setHelioFrameworkVersion(String helioFrameworkVersion) {
        this.helioFrameworkVersion = helioFrameworkVersion;
    }

    public Boolean getQueryFormSchema() {
        return queryFormSchema;
    }

    public void setQueryFormSchema(Boolean queryFormSchema) {
        this.queryFormSchema = queryFormSchema;
    }

    public Boolean getServiceAndImpl() {
        return serviceAndImpl;
    }

    public void setServiceAndImpl(Boolean serviceAndImpl) {
        this.serviceAndImpl = serviceAndImpl;
    }

    public Boolean getMybatisXML() {
        return mybatisXML;
    }

    public void setMybatisXML(Boolean mybatisXML) {
        this.mybatisXML = mybatisXML;
    }

    public Boolean getUseYesOrNoEnum() {
        return useYesOrNoEnum;
    }

    public void setUseYesOrNoEnum(Boolean useYesOrNoEnum) {
        this.useYesOrNoEnum = useYesOrNoEnum;
    }

    public Boolean getUseEnabledStatusEnum() {
        return useEnabledStatusEnum;
    }

    public void setUseEnabledStatusEnum(Boolean useEnabledStatusEnum) {
        this.useEnabledStatusEnum = useEnabledStatusEnum;
    }
}

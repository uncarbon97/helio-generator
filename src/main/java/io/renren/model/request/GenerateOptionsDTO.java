package io.renren.model.request;

import java.io.Serializable;

public class GenerateOptionsDTO implements Serializable {

    /**
     * 脚手架版本：单体or微服务
     */
    private String generateType;

    /**
     * 生成分页列表查询条件
     */
    private Boolean queryFormSchema;

    /**
     * 按Service+Impl范式生成
     */
    private Boolean serviceAndImpl;


    public String getGenerateType() {
        return generateType;
    }

    public void setGenerateType(String generateType) {
        this.generateType = generateType;
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
}
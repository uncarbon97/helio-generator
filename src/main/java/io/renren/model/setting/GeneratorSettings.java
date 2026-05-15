package io.renren.model.setting;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GeneratorSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "package")
    private String packageName = "cc.uncarbon.module";

    private String moduleName = "sys";

    private String tablePrefix = "tb_";
}

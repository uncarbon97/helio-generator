package cc.uncarbon.module.codegen.model.internal;

import cc.uncarbon.module.codegen.entity.ColumnEntity;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 解析数据表列结果
 */
@NoArgsConstructor
public class ResolveTableColumnResult {

    /**
     * 列信息
     */
    public List<ColumnEntity> columsList = List.of();

    public boolean hasBigDecimal = false;
    public boolean hasYesOrNoEnum = false;
    public boolean hasEnabledStatusEnum = false;
}

package org.dromara.system.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import lombok.RequiredArgsConstructor;
import org.apache.fesod.sheet.converters.Converter;
import org.apache.fesod.sheet.enums.CellDataTypeEnum;
import org.apache.fesod.sheet.metadata.GlobalConfiguration;
import org.apache.fesod.sheet.metadata.data.ReadCellData;
import org.apache.fesod.sheet.metadata.data.WriteCellData;
import org.apache.fesod.sheet.metadata.property.ExcelContentProperty;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.TreeBuildUtils;
import org.dromara.system.domain.bo.SysDeptBo;
import org.dromara.system.service.ISysDeptService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Excel 部门转换处理
 */
@RequiredArgsConstructor
@Component
public class DeptExcelConverter implements Converter<Long> {

    private static final ThreadLocal<Map<Long, String>> TL_ID_TO_NAME = new ThreadLocal<>();

    private static final ThreadLocal<Map<String, Long>> TL_NAME_TO_ID = new ThreadLocal<>();

    private void initThreadCache() {
        Map<Long, String> idMap = TL_ID_TO_NAME.get();
        if (CollUtil.isNotEmpty(idMap)) {
            return;
        }

        Map<String, Tree<Long>> deptPathToTreeMap = TreeBuildUtils.buildTreeNodeMap(
            SpringUtils.getBean(ISysDeptService.class).selectDeptTreeList(new SysDeptBo()),
            "/",
            Tree::getName
        );

        Map<Long, String> idToName = new HashMap<>();
        Map<String, Long> nameToId = new HashMap<>();
        deptPathToTreeMap.forEach((name, treeNode) -> {
            Long deptId = treeNode.getId();
            idToName.put(deptId, name);
            nameToId.put(name, deptId);
        });

        TL_ID_TO_NAME.set(idToName);
        TL_NAME_TO_ID.set(nameToId);
    }

    @Override
    public Class<?> supportJavaTypeKey() {
        return Long.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Long convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        String deptName = cellData.getStringValue();
        if (StringUtils.isBlank(deptName)) {
            return null;
        }
        initThreadCache();
        return TL_NAME_TO_ID.get().get(deptName);
    }

    @Override
    public WriteCellData<?> convertToExcelData(Long value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("");
        }
        initThreadCache();
        String deptName = TL_ID_TO_NAME.get().getOrDefault(value, "");
        return new WriteCellData<>(deptName);
    }
}

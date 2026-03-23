package org.dromara.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.dromara.system.api.domain.vo.RemoteDictDataVo;
import org.dromara.system.api.domain.vo.RemoteDictTypeVo;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 字典服务
 *
 * @author Lion Li
 */
@FeignClient(contextId = "remoteDictService", name = "ruoyi-system", path = "/remote/dict", primary = false)
public interface RemoteDictService {

    /**
     * 根据字典类型查询信息
     *
     * @param dictType 字典类型
     * @return 字典类型
     */
    @GetMapping("/select-dict-type-by-type")
    RemoteDictTypeVo selectDictTypeByType(@RequestParam String dictType);

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    @GetMapping("/select-dict-data-by-type")
    List<RemoteDictDataVo> selectDictDataByType(@RequestParam String dictType);

}


package org.dromara.system.api;

import org.dromara.common.core.annotation.RemoteHttpService;
import org.dromara.system.api.domain.vo.RemoteDictDataVo;
import org.dromara.system.api.domain.vo.RemoteDictTypeVo;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

/**
 * 字典服务
 *
 * @author Lion Li
 */
@RemoteHttpService("ruoyi-system")
@HttpExchange("/remote/dict")
public interface RemoteDictService {

    /**
     * 根据字典类型查询信息
     *
     * @param dictType 字典类型
     * @return 字典类型
     */
    @GetExchange("/select-dict-type-by-type")
    RemoteDictTypeVo selectDictTypeByType(@RequestParam String dictType);

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    @GetExchange("/select-dict-data-by-type")
    List<RemoteDictDataVo> selectDictDataByType(@RequestParam String dictType);

}

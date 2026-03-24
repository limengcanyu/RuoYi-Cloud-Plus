package org.dromara.resource.api;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.annotation.RemoteHttpService;
import org.dromara.resource.api.domain.RemoteFile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * 文件服务
 *
 * @author Lion Li
 */
@RemoteHttpService(value = "ruoyi-resource", fallback = RemoteFileServiceFallback.class)
@HttpExchange("/remote/file")
public interface RemoteFileService {

    /**
     * 上传文件
     *
     * @param file 文件信息
     * @return 结果
     */
    @PostExchange("/upload")
    RemoteFile upload(@RequestParam String name, @RequestParam String originalFilename,
                      @RequestParam String contentType, @RequestBody byte[] file) throws ServiceException;

    /**
     * 通过ossId查询对应的url
     *
     * @param ossIds ossId串逗号分隔
     * @return url串逗号分隔
     */
    @GetExchange("/select-url-by-ids")
    String selectUrlByIds(@RequestParam String ossIds);

    /**
     * 通过ossId查询列表
     *
     * @param ossIds ossId串逗号分隔
     * @return 列表
     */
    @GetExchange("/select-by-ids")
    List<RemoteFile> selectByIds(@RequestParam String ossIds);
}

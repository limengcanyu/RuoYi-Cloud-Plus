package org.dromara.resource.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.resource.api.domain.RemoteFile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 文件服务
 *
 * @author Lion Li
 */
@FeignClient(contextId = "remoteFileService", name = "ruoyi-resource", path = "/remote/file",
    fallbackFactory = RemoteFileServiceFallbackFactory.class, primary = false)
public interface RemoteFileService {

    /**
     * 上传文件
     *
     * @param file 文件信息
     * @return 结果
     */
    @PostMapping("/upload")
    RemoteFile upload(@RequestParam String name, @RequestParam String originalFilename,
                      @RequestParam String contentType, @RequestBody byte[] file) throws ServiceException;

    /**
     * 通过ossId查询对应的url
     *
     * @param ossIds ossId串逗号分隔
     * @return url串逗号分隔
     */
    @GetMapping("/select-url-by-ids")
    String selectUrlByIds(@RequestParam String ossIds);

    /**
     * 通过ossId查询列表
     *
     * @param ossIds ossId串逗号分隔
     * @return 列表
     */
    @GetMapping("/select-by-ids")
    List<RemoteFile> selectByIds(@RequestParam String ossIds);
}


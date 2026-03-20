package org.dromara.resource.api.domain;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 群发模板短信请求.
 *
 * @author Lion Li
 */
public record RemoteSmsBatch(
    List<String> phones,
    String templateId,
    LinkedHashMap<String, String> messages
) {
}

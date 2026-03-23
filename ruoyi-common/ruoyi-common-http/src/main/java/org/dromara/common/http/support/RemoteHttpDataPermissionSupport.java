package org.dromara.common.http.support;

import org.dromara.common.mybatis.helper.DataPermissionHelper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据权限上下文访问工具。
 */
public final class RemoteHttpDataPermissionSupport {

    private RemoteHttpDataPermissionSupport() {
    }

    public static boolean hasContext() {
        return !DataPermissionHelper.getContext().isEmpty();
    }

    public static Map<String, Object> snapshotContext() {
        return new LinkedHashMap<>(DataPermissionHelper.getContext());
    }

    public static void replaceContext(Map<String, Object> context) {
        Map<String, Object> currentContext = DataPermissionHelper.getContext();
        currentContext.clear();
        if (context != null && !context.isEmpty()) {
            currentContext.putAll(context);
        }
    }
}

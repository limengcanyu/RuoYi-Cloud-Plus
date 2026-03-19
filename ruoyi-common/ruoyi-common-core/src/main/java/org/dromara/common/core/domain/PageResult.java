package org.dromara.common.core.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果对象
 *
 * @author Lion Li
 */
@Data
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 列表数据
     */
    private List<T> rows;

    public PageResult(List<T> list, long total) {
        this.rows = list;
        this.total = total;
    }

    public static <T> PageResult<T> build(List<T> list, long total) {
        PageResult<T> rspData = new PageResult<>();
        rspData.setRows(list);
        rspData.setTotal(total);
        return rspData;
    }

    public static <T> PageResult<T> build(List<T> list) {
        PageResult<T> rspData = new PageResult<>();
        rspData.setRows(list);
        rspData.setTotal(list.size());
        return rspData;
    }

    public static <T> PageResult<T> build() {
        return new PageResult<>();
    }

}

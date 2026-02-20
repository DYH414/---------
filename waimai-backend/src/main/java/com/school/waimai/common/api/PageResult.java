package com.school.waimai.common.api;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装
 *
 * @param <T> 列表项类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页条数
     */
    private Integer pageSize;

    /**
     * 当前页数据列表
     */
    private List<T> list;

    public PageResult() {
    }

    public PageResult(Long total, Integer page, Integer pageSize, List<T> list) {
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.list = list;
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(Long total, Integer page, Integer pageSize, List<T> list) {
        return new PageResult<>(total, page, pageSize, list);
    }
}

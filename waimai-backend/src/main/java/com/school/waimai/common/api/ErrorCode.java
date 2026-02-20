package com.school.waimai.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码接口
 */
public interface ErrorCode {

    /**
     * 获取错误码
     */
    Integer getCode();

    /**
     * 获取错误信息
     */
    String getMessage();

    /**
     * 通用错误码
     */
    @Getter
    @AllArgsConstructor
    enum Common implements ErrorCode {
        OK(0, "OK"),
        ERR_PARAM_INVALID(1000, "参数不合法"),
        ERR_RESOURCE_NOT_FOUND(1001, "数据不存在"),
        ERR_METHOD_NOT_ALLOWED(1002, "不支持的请求方式"),
        ERR_BAD_REQUEST(1003, "业务语义错误，无法处理"),
        ERR_DUPLICATE_OPERATION(1004, "重复操作"),
        ERR_CONFLICT_STATUS(1005, "状态冲突"),
        ERR_SYSTEM_BUSY(1010, "系统繁忙，请稍后再试");

        private final Integer code;
        private final String message;
    }

    /**
     * 鉴权错误码
     */
    @Getter
    @AllArgsConstructor
    enum Auth implements ErrorCode {
        ERR_UNAUTHORIZED(2000, "未登录或token缺失"),
        ERR_TOKEN_INVALID(2001, "token非法或无法解析"),
        ERR_TOKEN_EXPIRED(2002, "token已过期"),
        ERR_FORBIDDEN(2003, "没有权限访问"),
        ERR_ADMIN_LOGIN_FAILED(2010, "管理员登录失败"),
        ERR_ADMIN_DISABLED(2011, "管理员账号被禁用");

        private final Integer code;
        private final String message;
    }

    /**
     * 商家/分类错误码
     */
    @Getter
    @AllArgsConstructor
    enum Shop implements ErrorCode {
        ERR_SHOP_NOT_FOUND(3000, "商家不存在或已删除"),
        ERR_SHOP_OFFLINE(3001, "商家已下架"),
        ERR_SHOP_INVALID_STATUS(3002, "商家状态不符合当前操作"),
        ERR_SHOP_LINK_REQUIRED(3003, "上架前未配置任何有效平台链接"),
        ERR_CATEGORY_NOT_FOUND(3010, "分类不存在或已删除"),
        ERR_CATEGORY_IN_USE(3011, "分类被商家引用，禁止删除");

        private final Integer code;
        private final String message;
    }

    /**
     * 推荐错误码
     */
    @Getter
    @AllArgsConstructor
    enum Recommend implements ErrorCode {
        ERR_RECOMMEND_CONFIG_NOT_FOUND(4000, "当日无推荐配置"),
        ERR_RECOMMEND_MODE_INVALID(4001, "推荐模式非法"),
        ERR_RECOMMEND_MANUAL_SHOP_INVALID(4002, "手动推荐配置中包含不存在/下架商家");

        private final Integer code;
        private final String message;
    }

    /**
     * 评分错误码
     */
    @Getter
    @AllArgsConstructor
    enum Rating implements ErrorCode {
        ERR_RATING_SCORE_INVALID(5000, "评分分值非法（非1～5整数）"),
        ERR_RATING_SHOP_NOT_RATEABLE(5001, "当前商家不允许评分");

        private final Integer code;
        private final String message;
    }

    /**
     * 收藏错误码
     */
    @Getter
    @AllArgsConstructor
    enum Favorite implements ErrorCode {
        ERR_FAVORITE_SHOP_NOT_FOUND(6000, "收藏的商家不存在或已删除"),
        ERR_FAVORITE_ALREADY_EXISTS(6001, "已收藏"),
        ERR_FAVORITE_NOT_EXISTS(6002, "收藏记录不存在");

        private final Integer code;
        private final String message;
    }
}

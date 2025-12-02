package com.smallnine.apiserver.constants.enums;

public enum ResponseCode {
    SUCCESS(200, "成功"),
    CREATED(201, "創建成功"),
    BAD_REQUEST(400, "請求參數錯誤"),
    UNAUTHORIZED(401, "未授權"),
    FORBIDDEN(403, "禁止訪問"),
    NOT_FOUND(404, "資源不存在"),
    INTERNAL_SERVER_ERROR(500, "內部服務器錯誤"),
    
    // 業務異常
    USER_NOT_FOUND(1001, "用戶不存在"),
    USER_ALREADY_EXISTS(1002, "用戶已存在"),
    INVALID_PASSWORD(1003, "密碼錯誤"),
    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    INSUFFICIENT_STOCK(2002, "庫存不足"),
    CATEGORY_NOT_FOUND(2003, "分類不存在"),
    BRAND_NOT_FOUND(2004, "品牌不存在"),
    ORDER_NOT_FOUND(3001, "訂單不存在"),
    ORDER_STATUS_ERROR(3002, "訂單狀態錯誤"),
    
    // 文章相關
    ARTICLE_NOT_FOUND(4001, "文章不存在"),
    ARTICLE_CREATE_FAILED(4002, "文章創建失敗"),
    ARTICLE_UPDATE_FAILED(4003, "文章更新失敗"),
    ARTICLE_DELETE_FAILED(4004, "文章刪除失敗"),
    
    // VIP等級相關
    VIP_LEVEL_NOT_FOUND(5001, "VIP等級不存在"),
    VIP_LEVEL_NAME_EXISTS(5002, "VIP等級名稱已存在"),
    VIP_LEVEL_CREATE_FAILED(5003, "VIP等級創建失敗"),
    VIP_LEVEL_UPDATE_FAILED(5004, "VIP等級更新失敗"),
    VIP_LEVEL_DELETE_FAILED(5005, "VIP等級刪除失敗"),
    VIP_LEVEL_NAME_REQUIRED(5006, "VIP等級名稱不能為空"),
    VIP_LEVEL_NAME_TOO_LONG(5007, "VIP等級名稱過長"),
    INVALID_SPENDING_AMOUNT(5008, "無效的消費金額"),
    INVALID_MIN_SPENDING(5009, "最低消費金額無效"),
    INVALID_DISCOUNT_RATE(5010, "折扣率必須在0-1之間"),
    INVALID_POINTS_MULTIPLIER(5011, "積分倍數必須大於0"),
    INVALID_FREE_SHIPPING_THRESHOLD(5012, "免運費門檻無效");
    
    private final int code;
    private final String message;
    
    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
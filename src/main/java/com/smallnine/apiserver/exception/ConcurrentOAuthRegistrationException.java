package com.smallnine.apiserver.exception;

/**
 * 兩個 OAuth2 登入請求對同一個 provider sub 同時建立帳號時，
 * 第二個會撞上 DB UNIQUE constraint。本例外用來把 race 訊號從
 * @Transactional 邊界拋出去，讓呼叫端在「新的」 transaction 裡 re-fetch
 * （PostgreSQL 一旦 abort 當前 txn，後續 SELECT 會被拒絕）。
 */
public class ConcurrentOAuthRegistrationException extends RuntimeException {

    private final String providerSub;

    public ConcurrentOAuthRegistrationException(String providerSub, Throwable cause) {
        super("Concurrent OAuth registration race for sub=" + providerSub, cause);
        this.providerSub = providerSub;
    }

    public String getProviderSub() {
        return providerSub;
    }
}
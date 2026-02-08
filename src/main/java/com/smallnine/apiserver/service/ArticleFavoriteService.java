package com.smallnine.apiserver.service;

import java.util.List;

public interface ArticleFavoriteService {

    List<Long> getFavoriteArticleIds(Long memberId);

    int addFavorite(Long memberId, Long articleId);

    int removeFavorite(Long memberId, Long articleId);

    int countByArticleId(Long articleId);

    boolean isFavorite(Long memberId, Long articleId);
}

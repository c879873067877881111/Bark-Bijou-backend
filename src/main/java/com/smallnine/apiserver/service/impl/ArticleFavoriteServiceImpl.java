package com.smallnine.apiserver.service.impl;

import com.smallnine.apiserver.dao.ArticleFavoriteDao;
import com.smallnine.apiserver.service.ArticleFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleFavoriteServiceImpl implements ArticleFavoriteService {

    private final ArticleFavoriteDao articleFavoriteDao;

    @Override
    public List<Long> getFavoriteArticleIds(Long memberId) {
        return articleFavoriteDao.findArticleIdsByMemberId(memberId);
    }

    @Override
    @Transactional
    public int addFavorite(Long memberId, Long articleId) {
        if (articleFavoriteDao.existsByMemberIdAndArticleId(memberId, articleId)) {
            return 0;
        }
        return articleFavoriteDao.insert(memberId, articleId);
    }

    @Override
    @Transactional
    public int removeFavorite(Long memberId, Long articleId) {
        return articleFavoriteDao.deleteByMemberIdAndArticleId(memberId, articleId);
    }

    @Override
    public int countByArticleId(Long articleId) {
        return articleFavoriteDao.countByArticleId(articleId);
    }

    @Override
    public boolean isFavorite(Long memberId, Long articleId) {
        return articleFavoriteDao.existsByMemberIdAndArticleId(memberId, articleId);
    }
}

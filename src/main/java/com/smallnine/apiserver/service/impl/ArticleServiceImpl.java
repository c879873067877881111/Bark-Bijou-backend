package com.smallnine.apiserver.service.impl;

import com.smallnine.apiserver.constants.enums.ResponseCode;
import com.smallnine.apiserver.dao.ArticleDao;
import com.smallnine.apiserver.entity.Article;
import com.smallnine.apiserver.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl {
    
    private final ArticleDao articleDao;
    
    /**
     * 根據ID查詢文章
     */
    public Article findById(Long id) {
        return articleDao.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.ARTICLE_NOT_FOUND));
    }
    
    /**
     * 查詢所有文章（分頁）
     */
    public List<Article> findAll(int page, int size) {
        int offset = page * size;
        return articleDao.findAll(offset, size);
    }
    
    /**
     * 根據作者查詢文章
     */
    public List<Article> findByAuthor(String author) {
        return articleDao.findByAuthor(author);
    }
    
    /**
     * 根據分類查詢文章
     */
    public List<Article> findByCategoryName(String categoryName) {
        return articleDao.findByCategoryName(categoryName);
    }
    
    /**
     * 搜索文章
     */
    public List<Article> searchByTitle(String title, int page, int size) {
        int offset = page * size;
        return articleDao.searchByTitle(title, offset, size);
    }
    
    /**
     * 根據成員ID查詢文章
     */
    public List<Article> findByMemberId(Long memberId) {
        return articleDao.findByMemberId(memberId);
    }
    
    /**
     * 創建文章
     */
    @Transactional
    public Article createArticle(Article article) {
        if (article.getCreatedDate() == null) {
            article.setCreatedDate(LocalDateTime.now());
        }
        if (article.getValid() == null) {
            article.setValid(1);
        }
        
        int result = articleDao.insert(article);
        if (result == 0) {
            throw new BusinessException(ResponseCode.ARTICLE_CREATE_FAILED);
        }
        
        log.info("文章創建成功: id={}, title={}", article.getId(), article.getTitle());
        return article;
    }
    
    /**
     * 更新文章
     */
    @Transactional
    public Article updateArticle(Article article) {
        Article existingArticle = findById(article.getId());
        
        int result = articleDao.update(article);
        if (result == 0) {
            throw new BusinessException(ResponseCode.ARTICLE_UPDATE_FAILED);
        }
        
        log.info("文章更新成功: id={}, title={}", article.getId(), article.getTitle());
        return findById(article.getId());
    }
    
    /**
     * 刪除文章
     */
    @Transactional
    public void deleteArticle(Long id) {
        Article article = findById(id);
        
        int result = articleDao.deleteById(id);
        if (result == 0) {
            throw new BusinessException(ResponseCode.ARTICLE_DELETE_FAILED);
        }
        
        log.info("文章刪除成功: id={}, title={}", id, article.getTitle());
    }
    
    /**
     * 統計文章總數
     */
    public long count() {
        return articleDao.count();
    }
    
    /**
     * 統計有效文章數量
     */
    public long countValid() {
        return articleDao.countValid();
    }
}
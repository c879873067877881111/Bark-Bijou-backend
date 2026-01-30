package com.smallnine.apiserver.service;

import com.smallnine.apiserver.entity.Article;

import java.util.List;

public interface ArticleService {

    Article findById(Long id);

    List<Article> findAll(int page, int size);

    List<Article> findByAuthor(String author);

    List<Article> findByCategoryName(String categoryName);

    List<Article> searchByTitle(String title, int page, int size);

    List<Article> findByMemberId(Long memberId);

    Article createArticle(Article article);

    Article updateArticle(Article article);

    void deleteArticle(Long id);

    long count();

    long countValid();
}

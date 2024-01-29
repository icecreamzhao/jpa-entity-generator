package com.example.entity;

import java.io.Serializable;
import java.sql.*;
import jakarta.persistence.*;

/**
 * Note: auto-generated by jpa-entity-generator
 */
@Entity(name = "com.example.entity.BlogArticleTag")
@Table(name = "article_tag")
public class BlogArticleTag implements Serializable {

  public Integer getId() { return this.id; }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "\"id\"", nullable = false)
  private Integer id;
  /**
   * database comment for article_id
   * The field is non-null value
   */
  @Column(name = "\"article_id\"", nullable = false)
  private Integer articleId;
  /**
   * database comment for blog_id
   * The field is non-null value
   */
  @Column(name = "\"tag_id\"", nullable = false)
  private Integer tagId;
  @Column(name = "\"created_at\"", nullable = false)
  private Timestamp createdAt;

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getArticleId() {
    return articleId;
  }

  public void setArticleId(Integer articleId) {
    this.articleId = articleId;
  }

  public Integer getTagId() {
    return tagId;
  }

  public void setTagId(Integer tagId) {
    this.tagId = tagId;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }
}
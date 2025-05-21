package com.example.be.repository;

import com.example.be.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // PostRepository에 추가
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.comments ORDER BY p.createDate DESC")
    Page<Post> findAllWithCommentsOrderByCreateDateDesc(Pageable pageable);}

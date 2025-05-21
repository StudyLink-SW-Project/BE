package com.example.be.repository;

import com.example.be.domain.Post;
import com.example.be.domain.PostLike;
import com.example.be.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);

    long countByPost(Post post);

    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);
}
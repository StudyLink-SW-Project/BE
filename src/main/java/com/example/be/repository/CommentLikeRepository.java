package com.example.be.repository;

import com.example.be.domain.Comment;
import com.example.be.domain.CommentLike;
import com.example.be.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

    boolean existsByUserAndComment(User user, Comment comment);

    long countByComment(Comment comment);

    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.comment.id = :commentId")
    long countByCommentId(@Param("commentId") Long commentId);
}
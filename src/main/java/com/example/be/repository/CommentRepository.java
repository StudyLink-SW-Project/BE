package com.example.be.repository;

import com.example.be.domain.Comment;
import com.example.be.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}

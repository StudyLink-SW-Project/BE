package com.example.be.repository;

import com.example.be.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByOrderByCreateDateDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"comments", "comments.user", "user"})
    Optional<Post> findById(Long id);

    Optional<Post> findByIdAndUserId(Long id, Long userId);
}

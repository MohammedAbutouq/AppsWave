package com.appswave.rest.assignment.repository;

import com.appswave.rest.assignment.entity.News;
import com.appswave.rest.assignment.entity.User;
import com.appswave.rest.assignment.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface NewsRepository extends CrudRepository<News, Long> {
    Page<News> findByUser_IdAndStatusIsNot(Long id, Status status, Pageable pageable);

    Page<News> findByStatusIsNot(Status status, Pageable pageable);

    News findByIdAndStatusIsNot(Long id, Status status);

    News findByIdAndUser_IdAndStatusIsNot(Long id, Long id1, Status status);

    @Transactional
    @Modifying
    @Query("delete from News n where n.user = ?1")
    void deleteByUser(User user);

    Page<News> findByStatus(Status status, Pageable pageable);

    News findByIdAndStatus(Long id, Status status);


    @Transactional

    List<News> findByStatusAndPublishDateIsBefore(Status status, LocalDate publishDate);

    Page<News> findByStatusIsNotAndSoftDeletedIsTrue(Status status, Pageable pageable);

    Page<News> findByStatusIsNotAndUser_IdAndSoftDeletedTrue(Status status, Long id, Pageable pageable);





}
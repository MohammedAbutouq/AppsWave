package com.appswave.rest.assignment.service;

import com.appswave.rest.assignment.helper.ErrorResponse;
import com.appswave.rest.assignment.helper.SuccessResponse;
import com.appswave.rest.assignment.entity.News;
import com.appswave.rest.assignment.entity.User;
import com.appswave.rest.assignment.enums.Status;
import com.appswave.rest.assignment.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;

    @Autowired
    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }




    public Page<News> findAll(Pageable pageable) {
        return newsRepository.findByStatusIsNot(Status.DELETED, pageable);
    }

    public Page<News> findAllByUserId(Pageable pageable, Long userId) {
        return newsRepository.findByUser_IdAndStatusIsNot(userId, Status.DELETED, pageable);
    }

    public News findById(Long newsId) {
        return newsRepository.findByIdAndStatusIsNot(newsId, Status.DELETED);
    }

    public News findByUserIdAndNewsId(Long userId, Long newsId) {
        return newsRepository.findByIdAndUser_IdAndStatusIsNot(newsId, userId, Status.DELETED);
    }

    public ResponseEntity<?> createNews(News news, User user) {
        List<String> errors = validateNews(news);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", errors));
        }

        news.setStatus(Status.PENDING);
        news.setUser(user);
        news.setId(0L);

        return ResponseEntity.ok(newsRepository.save(news));
    }

    public ResponseEntity<?> updateNews(Long id, News news, Long userId) {
        News updatedNews = newsRepository.findByIdAndUser_IdAndStatusIsNot(id, userId, Status.DELETED);
        return updateNewsCommon(updatedNews, news);
    }

    public ResponseEntity<?> updateNews(Long id, News news) {
        News updatedNews = newsRepository.findByIdAndStatusIsNot(id, Status.DELETED);
        return updateNewsCommon(updatedNews, news);
    }

    private ResponseEntity<?> updateNewsCommon(News updatedNews, News news) {
        if (updatedNews == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", List.of("News not found!")));
        }

        List<String> errors = validateNews(news);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", errors));
        }

        updatedNews.setDescription(news.getDescription());
        updatedNews.setImageUrl(news.getImageUrl());
        updatedNews.setDescriptionArabic(news.getDescriptionArabic());
        updatedNews.setPublishDate(news.getPublishDate());
        updatedNews.setTitle(news.getTitle());
        updatedNews.setTitleArabic(news.getTitleArabic());
        updatedNews.setStatus(Status.PENDING);

        return new ResponseEntity<>(newsRepository.save(updatedNews), HttpStatus.OK);
    }




    public ResponseEntity<?> deleteNews(Long newsId, Long userId) {
        News updatedNews = (userId == null) ?
                newsRepository.findByIdAndStatusIsNot(newsId, Status.DELETED) :
                newsRepository.findByIdAndUser_IdAndStatusIsNot(newsId, userId, Status.DELETED);

        if (updatedNews == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", List.of("News not found!")));
        }

        if (userId == null){
            updatedNews.setStatus(Status.DELETED);
            updatedNews.setSoftDeleted(false);

            return new ResponseEntity<>(new SuccessResponse("News deleted successfully!"), HttpStatus.OK);

        }

        if (updatedNews.getStatus() == Status.APPROVED){
            updatedNews.setSoftDeleted(true);
            return new ResponseEntity<>(new SuccessResponse("The admin has been notified to delete this news successfully!"), HttpStatus.OK);

        } else {
            updatedNews.setStatus(Status.DELETED);
            updatedNews.setSoftDeleted(false);
            return new ResponseEntity<>(new SuccessResponse("News deleted successfully!"), HttpStatus.OK);
        }


    }

    private List<String> validateNews(News news) {
        List<String> errors = new ArrayList<>();
        if (news.getTitle() == null || news.getTitle().trim().length() < 2) {
            errors.add("Title is required and must be at least 2 characters long");
        }
        if (news.getDescription() == null || news.getDescription().trim().length() < 2) {
            errors.add("Description is required and must be at least 2 characters long");
        }
        if (news.getPublishDate() == null) {
            errors.add("PublishDate is required");
        } else if (news.getPublishDate().isBefore(LocalDate.now())) {
            errors.add("PublishDate must be today or in the future");
        }
        return errors;
    }

    public ResponseEntity<?>  updateNewsStatus(Long id, Status status) {


        if (status == Status.DELETED){
            ErrorResponse errorResponse = new ErrorResponse("Validation failed", List.of("Status is not valid. It should be one of the following: [ REJECTED, PENDING, APPROVED] "));
            return ResponseEntity.badRequest().body(errorResponse);
        }


        News updatedNews = newsRepository.findByIdAndStatusIsNot(id, Status.DELETED);

        if (updatedNews == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", List.of("News not found!")));
        }

        updatedNews.setStatus(status);
        return ResponseEntity.ok(newsRepository.save(updatedNews));
    }

    public Page<News> findPublishedNews(Pageable pageable) {

        return newsRepository.findByStatus(Status.APPROVED,pageable);


    }

    public ResponseEntity<?> findPublishedNews(Long id) {

        News news = newsRepository.findByIdAndStatus(id,Status.APPROVED);
        if (news == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", List.of("News not found!")));
        }
        return new ResponseEntity<>(news, HttpStatus.OK);
    }

    public Page<News> findPendingDelete(Pageable pageable) {
       return newsRepository.findByStatusIsNotAndSoftDeletedIsTrue(Status.DELETED,pageable);
    }

    public Page<News> findPendingDelete(Pageable pageable,Long userId) {
        return newsRepository.findByStatusIsNotAndUser_IdAndSoftDeletedTrue(Status.DELETED,userId,pageable);
    }
}

package com.appswave.rest.assignment.controller;

import com.appswave.rest.assignment.helper.ErrorResponse;
import com.appswave.rest.assignment.security.JwtUserDetails;
import com.appswave.rest.assignment.dto.NewsStatusUpdateRequest;
import com.appswave.rest.assignment.entity.News;
import com.appswave.rest.assignment.enums.Role;
import com.appswave.rest.assignment.helper.Helper;
import com.appswave.rest.assignment.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final Helper helper;
    private final NewsService newsService;

    @Autowired
    public NewsController(Helper helper, NewsService newsService) {
        this.helper = helper;
        this.newsService = newsService;
    }

    @GetMapping
    public Page<News> getAllNews(@RequestParam(defaultValue = "0") int pageNumber,
                                 @RequestParam(defaultValue = "10") int pageSize,
                                 @RequestParam(defaultValue = "id") String sortBy,
                                 @RequestParam(defaultValue = "asc") String sortOrder) {

        Sort sort = sortOrder.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        JwtUserDetails jwtUserDetails = helper.getUserDetailsFromToken();

        if (jwtUserDetails == null) {
            return Page.empty();
        }

        if (jwtUserDetails.getRole() == Role.ADMIN) {
            return newsService.findAll(pageable);
        }

        return newsService.findAllByUserId(pageable, jwtUserDetails.getUserId());
    }



    @GetMapping("/pendingDelete")
    public Page<News> getPendingDelete(@RequestParam(defaultValue = "0") int pageNumber,
                                 @RequestParam(defaultValue = "10") int pageSize,
                                 @RequestParam(defaultValue = "id") String sortBy,
                                 @RequestParam(defaultValue = "asc") String sortOrder) {

        Sort sort = sortOrder.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        JwtUserDetails jwtUserDetails = helper.getUserDetailsFromToken();

        if (jwtUserDetails == null) {
            return Page.empty();
        }

        if (jwtUserDetails.getRole() == Role.ADMIN) {
            return newsService.findPendingDelete(pageable);
        }

        return newsService.findPendingDelete(pageable, jwtUserDetails.getUserId());
    }



    @GetMapping("/{id}")
    public ResponseEntity<?> getNewsById(@PathVariable Long id) {
        JwtUserDetails jwtUserDetails = helper.getUserDetailsFromToken();

        if (jwtUserDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        News news = (jwtUserDetails.getRole() == Role.ADMIN) ?
                newsService.findById(id) :
                newsService.findByUserIdAndNewsId(jwtUserDetails.getUserId(), id);

        if (news == null) {
            return ResponseEntity.status(404).body(new ErrorResponse("News not found"));
        }

        return ResponseEntity.ok(news);
    }

    @PostMapping
    public ResponseEntity<?> createNews(@RequestBody News news) {
        JwtUserDetails jwtUserDetails = helper.getUserDetailsFromToken();

        if (jwtUserDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        return newsService.createNews(news, jwtUserDetails.getUser());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNews(@PathVariable Long id, @RequestBody News news) {
        JwtUserDetails jwtUserDetails = helper.getUserDetailsFromToken();

        if (jwtUserDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (jwtUserDetails.getRole() == Role.ADMIN) {
            return newsService.updateNews(id, news);
        }

        return newsService.updateNews(id, news, jwtUserDetails.getUserId());
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateNewsStatus(@PathVariable Long id, @RequestBody NewsStatusUpdateRequest newsStatusUpdateRequest) {

            JwtUserDetails jwtUserDetails = helper.getUserDetailsFromToken();

            if (jwtUserDetails == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            if (jwtUserDetails.getRole() != Role.ADMIN){
                return ResponseEntity.status(401).body("Unauthorized");
            }


           return newsService.updateNewsStatus(id, newsStatusUpdateRequest.getStatus());

    }




    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNews(@PathVariable Long id) {
        JwtUserDetails jwtUserDetails = helper.getUserDetailsFromToken();

        if (jwtUserDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (jwtUserDetails.getRole() == Role.ADMIN) {
            return newsService.deleteNews(id,null);
        }

        return newsService.deleteNews(id, jwtUserDetails.getUserId());
    }


    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse("An error occurred", List.of(ex.getMessage()));


        if (ex.getMessage().contains("[DELETED, REJECTED, PENDING, APPROVED]")){
            errorResponse = new ErrorResponse("Validation failed", List.of("Status is not valid. It should be one of the following: [ REJECTED, PENDING, APPROVED] "));
            return ResponseEntity.badRequest().body(errorResponse);


        }
        return ResponseEntity.status(500).body(errorResponse);
    }




}

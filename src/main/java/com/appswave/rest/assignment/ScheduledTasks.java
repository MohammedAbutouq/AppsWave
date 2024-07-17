package com.appswave.rest.assignment;


import com.appswave.rest.assignment.entity.News;
import com.appswave.rest.assignment.enums.Status;
import com.appswave.rest.assignment.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ScheduledTasks {
    @Autowired
    private NewsRepository newsRepository ;

    @Scheduled(cron = "0 1 0 * * ?")

    public void markOldNewsAsDeleted() {

        LocalDate today = LocalDate.now();

        System.out.println("Marking news with status  " + Status.APPROVED + " and publish date before " + today + " as DELETED");

        List<News> oldNews = newsRepository.findByStatusAndPublishDateIsBefore(Status.APPROVED, today);
        oldNews.forEach(news -> news.setStatus(Status.DELETED));
        newsRepository.saveAll(oldNews);

    }






    }

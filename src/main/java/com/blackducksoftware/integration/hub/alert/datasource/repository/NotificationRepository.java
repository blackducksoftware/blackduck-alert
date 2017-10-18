package com.blackducksoftware.integration.hub.alert.datasource.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;

@Transactional
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    @Query("select entity from NotificationEntity entity where entity.createdAt between ?1 and ?2 order by created_at asc")
    List<NotificationEntity> findByCreatedAtBetween(final Date startDate, final Date endDate);
}

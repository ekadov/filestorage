package com.eugene.filestorage.repo;

import com.eugene.filestorage.entity.EventEntity;
import com.eugene.filestorage.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<EventEntity, Integer> {

    List<EventEntity> findByUser_Id(Integer userId);

    @Query("select e.user.id from EventEntity e where e.file.id = :fileId and e.status = :status order by e.timestamp asc")
    List<Integer> findOwnerIdsForFile(@Param("fileId") Integer fileId, @Param("status") EventStatus status);

    default Optional<Integer> findOwnerIdForFile(Integer fileId) {
        List<Integer> ids = findOwnerIdsForFile(fileId, EventStatus.CREATED);
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.getFirst());
    }
}

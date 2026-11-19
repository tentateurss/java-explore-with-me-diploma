package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HitRepository extends JpaRepository<Hit, Long> {

    // все посещения, с фильтром по URI
    @Query("SELECT new ru.practicum.stats.dto.ViewStats(h.app, h.uri, COUNT(h.id)) "
            + "FROM Hit h "
            + "WHERE h.uri IN :uris "
            + "AND h.timestamp BETWEEN :start AND :end "
            + "GROUP BY h.app, h.uri "
            + "ORDER BY COUNT(h.id) DESC")
    List<ViewStats> getHitsByDateLimits(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);

    // уникальные посещения, с фильтром по URI
    @Query("SELECT new ru.practicum.stats.dto.ViewStats(h.app, h.uri, COUNT(DISTINCT h.ip)) "
            + "FROM Hit h "
            + "WHERE h.uri IN :uris "
            + "AND h.timestamp BETWEEN :start AND :end "
            + "GROUP BY h.app, h.uri "
            + "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStats> getHitsByDateAndUniqueIpLimits(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);

    // все посещения, без фильтра по URI
    @Query("SELECT new ru.practicum.stats.dto.ViewStats(h.app, h.uri, COUNT(h.id)) "
            + "FROM Hit h "
            + "WHERE h.timestamp BETWEEN :start AND :end "
            + "GROUP BY h.app, h.uri "
            + "ORDER BY COUNT(h.id) DESC")
    List<ViewStats> getHitsByDateLimitsAll(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // уникальные посещения, без фильтра по URI
    @Query("SELECT new ru.practicum.stats.dto.ViewStats(h.app, h.uri, COUNT(DISTINCT h.ip)) "
            + "FROM Hit h "
            + "WHERE h.timestamp BETWEEN :start AND :end "
            + "GROUP BY h.app, h.uri "
            + "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStats> getHitsByDateAndUniqueIpLimitsAll(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
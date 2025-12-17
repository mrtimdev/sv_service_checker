package timdev.timdev.repository;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Survey;
import timdev.timdev.entity.SurveyResponse;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, UUID> {

    void deleteBySurveyId(UUID surveyId);
    
    long countBySurveyId(UUID surveyId);

    @Query("SELECT sr FROM SurveyResponse sr WHERE sr.survey.id = :surveyId ORDER BY sr.submittedAt DESC")
    List<SurveyResponse> findBySurveyIdOrderBySubmittedAtDesc(@Param("surveyId") UUID surveyId);
    
    @Query("SELECT COUNT(sr) FROM SurveyResponse sr WHERE sr.survey.id = :surveyId")
    Long countResponsesBySurveyId(@Param("surveyId") UUID surveyId);

    // Optional<Survey> findByAccessCode(String accessCode);
    
    // Get all answers for a survey
    @Query("SELECT sr.answers FROM SurveyResponse sr WHERE sr.survey.id = :surveyId")
    List<Map<UUID, Integer>> findAllAnswersBySurveyId(@Param("surveyId") UUID surveyId);
    
    // Get responses with driver info
    @Query("SELECT sr FROM SurveyResponse sr LEFT JOIN FETCH sr.driverInfo WHERE sr.survey.id = :surveyId")
    List<SurveyResponse> findResponsesWithDriverInfoBySurveyId(@Param("surveyId") UUID surveyId);

    Optional<SurveyResponse> findByIdAndSurveyId(String responseId, UUID surveyId);

    boolean existsBySurveyIdAndIpAddressAndUserAgent(UUID surveyId, String ipAddress, String userAgent);
    SurveyResponse findBySurveyIdAndIpAddressAndUserAgent(UUID surveyId, String ipAddress, String userAgent);


    @Query("SELECT sr FROM SurveyResponse sr " +
           "JOIN sr.driverInfo di " +
           "WHERE sr.survey.id = :surveyId " +
           "AND di.fullName = :fullName " +
           "AND di.phoneNumber = :phoneNumber")
    Optional<SurveyResponse> findBySurveyIdAndDriverInfo(
            @Param("surveyId") UUID surveyId,
            @Param("fullName") String fullName,
            @Param("phoneNumber") String phoneNumber);
}
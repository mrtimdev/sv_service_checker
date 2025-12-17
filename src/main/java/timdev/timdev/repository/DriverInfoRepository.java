package timdev.timdev.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.DriverInfo;

@Repository
public interface DriverInfoRepository extends JpaRepository<DriverInfo, String> {

    boolean existsBySurveyResponse_IdAndFullNameAndPhoneNumber(
            String surveyResponseId,
            String fullName,
            String phoneNumber
    );

    // optional
    Optional<DriverInfo> findBySurveyResponse_IdAndFullNameAndPhoneNumber(
            String surveyResponseId,
            String fullName,
            String phoneNumber
    );

    @Query("SELECT di FROM DriverInfo di " +
           "JOIN di.surveyResponse sr " +
           "WHERE di.fullName = :fullName " +
           "AND di.phoneNumber = :phoneNumber " +
           "AND sr.survey.id = :surveyId")
    Optional<DriverInfo> findByFullNameAndPhoneNumberAndSurveyId(
            @Param("fullName") String fullName,
            @Param("phoneNumber") String phoneNumber,
            @Param("surveyId") UUID surveyId);

}

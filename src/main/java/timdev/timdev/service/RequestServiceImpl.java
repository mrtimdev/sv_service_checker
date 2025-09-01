package timdev.timdev.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import timdev.timdev.entity.Request;
import timdev.timdev.entity.User;
import timdev.timdev.enums.ApprovalLevel;
import timdev.timdev.enums.ApprovalStatus;
import timdev.timdev.repository.RequestRepository;

@Service
public class RequestServiceImpl implements RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Override
    public Request createRequest(Request request, User createdBy) {
        request.setCreatedBy(createdBy);
        return requestRepository.save(request);
    }

    @Override
    public Request updateRequest(Request request) {
        return requestRepository.save(request);
    }

    @Override
    public Request save(Request request) {
        return requestRepository.save(request);
    }

    @Override
    public void deleteRequest(Long id) {
        requestRepository.deleteById(id);
    }

    @Override
    public Optional<Request> findById(Long id) {
        return requestRepository.findById(id);
    }

    @Override
    public List<Request> findAllRequests() {
        return requestRepository.findAll();
    }

    @Override
    public List<Request> findAllPendingRequests() {
        return requestRepository.findAllPendingRequests();
    }

    @Override
    public List<Request> findAllApprovedRequests() {
        return requestRepository.findAllApprovedRequests();
    }

    @Override
    public List<Request> findAllCancelledRequests() {
        return requestRepository.findAllCancelledRequests();
    }

    @Override
    public List<Request> findAllRejectedRequests() {
        return requestRepository.findAllRejectedRequests();
    }

    @Override
    public List<Request> findByUser(User user) {
        return requestRepository.findByCreatedBy(user);
    }

    @Override
    public List<Request> findByStatus(ApprovalStatus status) {
        return requestRepository.findByStatus(status);
    }

    @Override
    public List<Request> findByUserAndStatus(User user, ApprovalStatus status) {
        return requestRepository.findByCreatedByAndStatus(user, status);
    }

    @Override
    public Request changeStatus(Long requestId, ApprovalStatus status) {
        Optional<Request> requestOpt = requestRepository.findById(requestId);
        if (requestOpt.isPresent()) {
            Request request = requestOpt.get();
            request.setStatus(status);
            return requestRepository.save(request);
        }
        return null;
    }

    @Override
    public List<Request> findByIds(List<Long> ids) 
    {
        return requestRepository.findAllById(ids);
    }

    @Override
    public List<Request> findByStatusAndApprovalLevel(ApprovalStatus status, ApprovalLevel level)
    {
        return requestRepository.findByStatusAndApprovalLevel(
            status, 
            level
        );
    }
    @Override
    public List<Request> findByStatusAndCurrentLevel(ApprovalStatus status, ApprovalLevel level)
    {
        return requestRepository.findByStatusAndCurrentLevel(
            status, 
            level
        );
    }

    @Override
    public List<Request> findByStatusInAndApprovalLevel(List<ApprovalStatus> statuses, ApprovalLevel level)
    {
        return requestRepository.findByStatusInAndApprovalLevel(
            statuses, 
            level
        );
    }
     @Override
    public List<Request> findByStatusInAndCurrentLevel(List<ApprovalStatus> statuses, ApprovalLevel level)
    {
        return requestRepository.findByStatusInAndCurrentLevel(
            statuses, 
            level
        );
    }

    // Getters and setters (if needed)
    public RequestRepository getRequestRepository() {
        return requestRepository;
    }

    public void setRequestRepository(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

}

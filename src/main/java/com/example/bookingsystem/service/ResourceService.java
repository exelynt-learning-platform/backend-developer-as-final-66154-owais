package com.example.bookingsystem.service;



import com.example.bookingsystem.dto.DtoMapper;
import com.example.bookingsystem.dto.ResourceRequest;
import com.example.bookingsystem.dto.ResourceResponse;
import com.example.bookingsystem.entity.ReservationStatus;
import com.example.bookingsystem.entity.Resource;
import com.example.bookingsystem.exception.ResourceNotFoundException;
import com.example.bookingsystem.repository.ReservationRepository;
import com.example.bookingsystem.repository.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;

    public ResourceService(ResourceRepository resourceRepository,
                           ReservationRepository reservationRepository) {
        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponse> getAllResources(Pageable pageable) {
        return resourceRepository.findAll(pageable)
                .map(DtoMapper::toResourceResponse);
    }

    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        return DtoMapper.toResourceResponse(resource);
    }

    @Transactional
    public ResourceResponse createResource(ResourceRequest request) {
        Resource resource = Resource.builder()
                .name(request.name())
                .description(request.description())
                .available(request.available())
                .pricePerHour(request.pricePerHour())
                .build();
        Resource saved = resourceRepository.save(resource);
        return DtoMapper.toResourceResponse(saved);
    }

    @Transactional
    public ResourceResponse updateResource(Long id, ResourceRequest request) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        resource.setName(request.name());
        resource.setDescription(request.description());
        resource.setAvailable(request.available());
        resource.setPricePerHour(request.pricePerHour());
        return DtoMapper.toResourceResponse(resourceRepository.save(resource));
    }

    @Transactional
    public void deleteResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        boolean hasActive = reservationRepository.existsByResourceIdAndStatusNot(id, ReservationStatus.CANCELLED);
        if (hasActive) {
            throw new IllegalStateException("Cannot delete resource with active reservations");
        }
        resourceRepository.delete(resource);
    }
}

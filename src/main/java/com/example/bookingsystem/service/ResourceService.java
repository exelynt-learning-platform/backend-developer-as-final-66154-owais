package com.example.bookingsystem.service;



import com.example.bookingsystem.dto.DtoMapper;
import com.example.bookingsystem.dto.ResourceRequest;
import com.example.bookingsystem.dto.ResourceResponse;
import com.example.bookingsystem.entity.Resource;
import com.example.bookingsystem.exception.ResourceNotFoundException;
import com.example.bookingsystem.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResources() {
        return resourceRepository.findAll().stream()
                .map(DtoMapper::toResourceResponse)
                .collect(Collectors.toList());
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
        return DtoMapper.toResourceResponse(resourceRepository.save(resource));
    }

    @Transactional
    public void deleteResource(Long id) {
        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found with id: " + id);
        }
        resourceRepository.deleteById(id);
    }
}

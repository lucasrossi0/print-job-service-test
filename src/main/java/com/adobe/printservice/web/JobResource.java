package com.adobe.printservice.web;

import com.adobe.printservice.model.JobStatus;
import com.adobe.printservice.repository.JobRepository;
import com.adobe.printservice.repository.RenderTemplateRepository;
import com.adobe.printservice.web.api.JobInputApi;
import com.adobe.printservice.web.api.JobOutputApi;
import com.adobe.printservice.web.api.JobResultOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobResource{

    private static final String TEMPLATE_NOT_FOUND = "Template not found";
    private static final String JOB_NOT_FOUND = "Job not found";

    private final JobRepository jobRepository;
    private final RenderTemplateRepository renderTemplateRepository;

    @GetMapping
    public ResponseEntity<List<JobOutputApi>> getAllJobs(@RequestParam(required = false) JobStatus status){
        List<JobOutputApi> jobs = (status == null ? jobRepository.findAll() : jobRepository.findByStatus(status))
                .stream()
                .map(JobOutputApi::fromJob)
                .toList();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<JobStatus, Long>> getMetrics(){
        return ResponseEntity.ok(Map.of(
                JobStatus.QUEUED, jobRepository.countByStatus(JobStatus.QUEUED),
                JobStatus.PROCESSING, jobRepository.countByStatus(JobStatus.PROCESSING),
                JobStatus.DONE, jobRepository.countByStatus(JobStatus.DONE),
                JobStatus.FAILED, jobRepository.countByStatus(JobStatus.FAILED)
        ));
    }

    @PostMapping
    public ResponseEntity<JobOutputApi> createJob(@RequestBody JobInputApi jobInputApi){
        if (jobInputApi.getTemplateId() == null || !renderTemplateRepository.existsById(jobInputApi.getTemplateId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TEMPLATE_NOT_FOUND);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(JobOutputApi.fromJob(jobRepository.save(jobInputApi.toJob())));
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<JobResultOutput> getResult(@PathVariable String id){
        return jobRepository.findById(id)
                .map(JobResultOutput::fromJob)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, JOB_NOT_FOUND));
    }
}

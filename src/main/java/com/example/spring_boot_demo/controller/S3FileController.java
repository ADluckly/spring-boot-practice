package com.example.spring_boot_demo.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.spring_boot_demo.dto.S3UploadResponse;
import com.example.spring_boot_demo.dto.UploadTaskResponse;
import com.example.spring_boot_demo.service.S3FileService;
import com.example.spring_boot_demo.service.S3FileService.DownloadFileResult;
import com.example.spring_boot_demo.service.UploadWorkflowService;

@RestController
@RequestMapping("/api/files")
public class S3FileController {

    private final S3FileService s3FileService;
    private final UploadWorkflowService uploadWorkflowService;

    public S3FileController(S3FileService s3FileService, UploadWorkflowService uploadWorkflowService) {
        this.s3FileService = s3FileService;
        this.uploadWorkflowService = uploadWorkflowService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<S3UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "key", required = false) String key
    ) {
        return ResponseEntity.ok(s3FileService.uploadFile(file, key));
    }

    @PostMapping(value = "/upload/pending", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadTaskResponse> uploadPending(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "key", required = false) String key
    ) {
        UploadTaskResponse response = uploadWorkflowService.uploadThenQueue(file, key);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/upload/status/{taskId}")
    public ResponseEntity<UploadTaskResponse> getUploadStatus(@PathVariable Long taskId) {
        return ResponseEntity.ok(uploadWorkflowService.getTask(taskId));
    }

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam("key") String key) {
        DownloadFileResult result = s3FileService.downloadFile(key);

        String filename = extractFilename(result.objectKey());
        ByteArrayResource body = new ByteArrayResource(result.content());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .contentLength(result.content().length)
                .body(body);
    }

    private String extractFilename(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return "download.bin";
        }

        int index = objectKey.lastIndexOf('/');
        if (index >= 0 && index < objectKey.length() - 1) {
            return objectKey.substring(index + 1);
        }

        return objectKey;
    }
}

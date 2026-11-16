package edu.handong.web11.controller;

import java.util.Set;
import edu.handong.web11.domain.Attachment;
import edu.handong.web11.repository.AttachmentRepository;
import edu.handong.web11.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {
    private final AttachmentRepository attachments;
    private final FileStorageService storage;
    public AttachmentController(AttachmentRepository attachments, FileStorageService storage) {
        this.attachments = attachments; this.storage = storage;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {

        // 파일 ID를 이용해 DB에서 파일 정보를 찾는다.
        // 파일이 없으면 404 오류를 반환한다.
        Attachment file = attachments.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일이 없습니다."));

        // 서버에 저장된 실제 파일을 불러온다.
        Resource resource = storage.load(file);

        // 다운로드할 때 원래 파일 이름을 사용하도록 설정한다.
        String disposition = ContentDisposition.attachment()
                .filename(file.getOriginalFilename(), StandardCharsets.UTF_8)
                .build().toString();

        // 파일을 브라우저로 전달하여 다운로드할 수 있게 한다.
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(resource);
    }

    // STUDENT TODO: GET /attachments/{id}/view (image preview).

    // 이미지 미리보기 기능
    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> view(@PathVariable Long id) {

        // 파일 ID로 DB에서 파일 정보를 찾는다.
        // 파일이 없으면 404 오류를 반환한다.
        Attachment file = attachments.findById(id).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "첨부파일이 없습니다."
                )
        );

        // 파일 종류를 확인한다.
        String type = file.getContentType();

        // 이미지가 아닌 파일은 미리보기를 할 수 없게 한다.
        if (!Set.of("image/jpeg", "image/png", "image/gif").contains(type)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "이미지 파일이 아닙니다."
            );
        }

        // 서버에 저장된 실제 파일을 가져온다.
        Resource resource = storage.load(file);

        // 이미지를 브라우저에 보여준다.
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(type))
                .body(resource);
    }

}

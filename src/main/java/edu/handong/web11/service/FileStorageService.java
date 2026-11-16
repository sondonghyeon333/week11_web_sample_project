package edu.handong.web11.service;

import edu.handong.web11.domain.Attachment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "gif", "pdf", "txt");
    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload-dir}") String path) throws IOException {
        uploadDir = Path.of(path).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
    }


    public Attachment store(MultipartFile file) {

        // 빈 파일인지 확인한다.
        if (file.isEmpty()) throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");

        // 업로드한 파일의 원래 이름을 가져온다.
        String original = file.getOriginalFilename();

        // 파일 이름이 잘못되었는지 확인한다.
        if (original == null || original.isBlank() || original.contains("/") || original.contains("\\") || original.contains(".."))
            throw new IllegalArgumentException("잘못된 파일 이름입니다.");

        // 파일 이름이 너무 긴지 확인한다.
        if (original.length() > 240) throw new IllegalArgumentException("파일 이름이 너무 깁니다.");

        // 파일 이름에서 확장자를 가져온다.
        int dot = original.lastIndexOf('.');
        String ext = dot < 0 ? "" : original.substring(dot + 1).toLowerCase(Locale.ROOT);

        // 허용된 파일 형식인지 확인한다.
        if (!ALLOWED.contains(ext)) throw new IllegalArgumentException("허용 형식: jpg, jpeg, png, gif, pdf, txt");

        // 파일 크기가 10MB 이하인지 확인한다.
        if (file.getSize() > 10L * 1024 * 1024) throw new IllegalArgumentException("파일은 10MB 이하만 가능합니다.");

        // 파일 이름이 겹치지 않도록 UUID로 새로운 이름을 만든다.
        String stored = UUID.randomUUID() + "." + ext;

        // 파일을 저장할 경로를 만든다.
        Path target = uploadDir.resolve(stored).normalize();

        // 지정된 저장 폴더 밖으로 나가지 않도록 확인한다.
        if (!target.startsWith(uploadDir)) throw new IllegalArgumentException("잘못된 파일 경로입니다.");

        try (InputStream input = file.getInputStream()) {

            // 실제 파일을 서버 폴더에 저장한다.
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);

            // 확장자에 따라 파일 종류를 정한다.
            String contentType = switch (ext) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "gif" -> "image/gif";
                case "pdf" -> "application/pdf";
                default -> "text/plain";
            };

            // 파일 정보를 만들어 반환한다.
            return new Attachment(original, stored, contentType, file.getSize());

        } catch (IOException e) {

            // 저장 중 오류가 나면 남아 있는 파일을 삭제한다.
            try { Files.deleteIfExists(target); } catch (IOException ignored) {}

            // 파일 저장에 실패했다는 오류를 전달한다.
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 실패", e);
        }
    }


    public Resource load(Attachment attachment) {
        Path target = uploadDir.resolve(attachment.getStoredFilename()).normalize();
        if (!target.startsWith(uploadDir) || !Files.isRegularFile(target))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일이 없습니다.");
        return new PathResource(target);
    }

    public void deleteIfExists(Attachment attachment) {
        try {
            Files.deleteIfExists(uploadDir.resolve(attachment.getStoredFilename()).normalize());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제 실패", e);
        }
    }
}

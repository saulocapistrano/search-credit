package br.com.searchcredit.infrastructure.storage;

import br.com.searchcredit.infrastructure.storage.exception.StorageException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MinioStorageService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".pdf", ".png", ".jpg");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB em bytes
    private static final String BUCKET_NAME = "comprovantes-renda";

    private final MinioClient minioClient;
    private final String minioUrl;

    public MinioStorageService(
            @Value("${minio.endpoint}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.url}") String minioUrl) {
        this.minioUrl = minioUrl;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        initializeBucket();
    }

    private void initializeBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(BUCKET_NAME)
                    .build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(BUCKET_NAME)
                        .build());
                log.info("Bucket '{}' criado com sucesso", BUCKET_NAME);
            } else {
                log.info("Bucket '{}' já existe", BUCKET_NAME);
            }
        } catch (Exception e) {
            log.error("Erro ao inicializar bucket: {}", e.getMessage(), e);
            throw new StorageException("Erro ao inicializar bucket MinIO", e);
        }
    }

    public String upload(File file) {
        if (file == null || !file.exists()) {
            throw new StorageException("Arquivo não pode ser nulo ou não encontrado", HttpStatus.BAD_REQUEST);
        }

        // Validação de tamanho (2MB máximo)
        if (file.length() > MAX_FILE_SIZE) {
            throw new StorageException(
                    String.format("Arquivo excede o tamanho máximo permitido de 2MB. Tamanho atual: %d bytes", file.length()),
                    HttpStatus.PAYLOAD_TOO_LARGE
            );
        }

        // Validação de extensão
        String extension = getExtension(file.getName()).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new StorageException(
                    String.format("Extensão de arquivo não permitida. Extensões aceitas: %s", ALLOWED_EXTENSIONS),
                    HttpStatus.PAYLOAD_TOO_LARGE
            );
        }

        String fileName = generateFileName(file.getName());
        String contentType = getContentTypeFromExtension(file.getName());

        try (InputStream inputStream = new java.io.FileInputStream(file)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(fileName)
                            .stream(inputStream, file.length(), -1)
                            .contentType(contentType)
                            .build()
            );

            String fileUrl = String.format("%s/%s/%s", minioUrl, BUCKET_NAME, fileName);
            log.info("Arquivo '{}' enviado com sucesso para MinIO. URL: {}", fileName, fileUrl);
            return fileUrl;
        } catch (MinioException e) {
            log.error("Erro ao fazer upload do arquivo no MinIO: {}", e.getMessage(), e);
            throw new StorageException("Erro ao fazer upload do arquivo no MinIO", e);
        } catch (Exception e) {
            log.error("Erro inesperado ao fazer upload: {}", e.getMessage(), e);
            throw new StorageException("Erro inesperado ao fazer upload", e);
        }
    }

    public InputStream download(String filename) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(filename)
                            .build()
            );
        } catch (MinioException e) {
            log.error("Erro ao fazer download do arquivo '{}' do MinIO: {}", filename, e.getMessage(), e);
            throw new StorageException("Erro ao fazer download do arquivo", e);
        } catch (Exception e) {
            log.error("Erro inesperado ao fazer download: {}", e.getMessage(), e);
            throw new StorageException("Erro inesperado ao fazer download", e);
        }
    }

    public String uploadComprovante(MultipartFile file) {
        validateFile(file);
        
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String contentType = file.getContentType();

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(BUCKET_NAME)
                                .object(fileName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(contentType)
                                .build()
                );
            }

            String fileUrl = String.format("%s/%s/%s", minioUrl, BUCKET_NAME, fileName);
            log.info("Comprovante '{}' enviado com sucesso para MinIO. URL: {}", fileName, fileUrl);
            return fileUrl;
        } catch (MinioException e) {
            log.error("Erro ao fazer upload do comprovante no MinIO: {}", e.getMessage(), e);
            throw new StorageException("Erro ao fazer upload do comprovante no MinIO", e);
        } catch (Exception e) {
            log.error("Erro inesperado ao fazer upload do comprovante: {}", e.getMessage(), e);
            throw new StorageException("Erro inesperado ao fazer upload do comprovante", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Arquivo não pode ser nulo ou vazio", HttpStatus.BAD_REQUEST);
        }

        // Validação de tamanho (2MB máximo)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new StorageException(
                    String.format("Arquivo excede o tamanho máximo permitido de 2MB. Tamanho atual: %d bytes", file.getSize()),
                    HttpStatus.PAYLOAD_TOO_LARGE
            );
        }

        // Validação de extensão
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new StorageException("Nome do arquivo não pode ser vazio", HttpStatus.BAD_REQUEST);
        }

        String extension = getExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new StorageException(
                    String.format("Extensão de arquivo não permitida. Extensões aceitas: %s", ALLOWED_EXTENSIONS),
                    HttpStatus.PAYLOAD_TOO_LARGE
            );
        }

        // Validação de MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new StorageException(
                    String.format("Tipo de arquivo não permitido. Tipos aceitos: %s", ALLOWED_MIME_TYPES),
                    HttpStatus.PAYLOAD_TOO_LARGE
            );
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String getContentTypeFromExtension(String filename) {
        String extension = getExtension(filename).toLowerCase();
        return switch (extension) {
            case ".pdf" -> "application/pdf";
            case ".png" -> "image/png";
            case ".jpg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }

    private String generateFileName(String originalFilename) {
        String extension = getExtension(originalFilename);
        return UUID.randomUUID().toString() + extension;
    }
}

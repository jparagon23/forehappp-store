package com.forehapp.store.productModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.general.storage.StorageService;
import com.forehapp.store.productModule.application.dto.ProductImageResponse;
import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductImage;
import com.forehapp.store.productModule.domain.ports.in.IProductImageService;
import com.forehapp.store.productModule.domain.ports.out.IProductDao;
import com.forehapp.store.productModule.domain.ports.out.IProductImageDao;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
public class ProductImageServiceImpl implements IProductImageService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024L;

    private final StorageService storageService;
    private final IProductImageDao imageDao;
    private final IProductDao productDao;
    private final IStoreProfileDao storeProfileDao;

    public ProductImageServiceImpl(StorageService storageService,
                                   IProductImageDao imageDao,
                                   IProductDao productDao,
                                   IStoreProfileDao storeProfileDao) {
        this.storageService = storageService;
        this.imageDao = imageDao;
        this.productDao = productDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional
    public ProductImageResponse upload(Long productId, MultipartFile file, Long userId) {
        validateFile(file);
        Long sellerId = resolveSellerId(userId);
        Product product = productDao.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        StorageService.UploadResult result = storageService.upload(file, "products/" + productId);

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setS3Key(result.key());
        image.setUrl(result.key());
        image.setDisplayOrder(0);

        ProductImage saved = imageDao.save(image);
        String presignedUrl = storageService.presign(saved.getS3Key(), Duration.ofDays(7));
        return new ProductImageResponse(saved, presignedUrl);
    }

    @Override
    @Transactional
    public void delete(Long productId, Long imageId, Long userId) {
        Long sellerId = resolveSellerId(userId);
        productDao.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        ProductImage image = imageDao.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Imagen no encontrada"));

        if (!image.getProduct().getId().equals(productId)) {
            throw new BadRequestException("La imagen no pertenece al producto indicado");
        }

        storageService.delete(image.getS3Key());
        imageDao.delete(image);
    }

    private Long resolveSellerId(Long userId) {
        return storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Store profile not found"))
                .getId();
    }

    @Override
    public List<ProductImageResponse> getByProduct(Long productId) {
        return imageDao.findByProductId(productId).stream()
                .map(img -> new ProductImageResponse(img, storageService.presign(img.getS3Key(), Duration.ofDays(7))))
                .toList();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo está vacío");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException("El archivo no puede superar 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BadRequestException("Formato no permitido. Use JPEG, PNG o WebP");
        }
        try {
            byte[] header = file.getBytes();
            if (!hasValidMagicBytes(header, contentType)) {
                throw new BadRequestException("El contenido del archivo no coincide con su tipo declarado");
            }
        } catch (IOException e) {
            throw new BadRequestException("No se pudo leer el archivo");
        }
    }

    private boolean hasValidMagicBytes(byte[] bytes, String contentType) {
        if (bytes.length < 4) return false;
        return switch (contentType) {
            case "image/jpeg" -> bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF;
            case "image/png"  -> bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47;
            case "image/webp" -> bytes.length >= 12
                    && bytes[0] == 0x52 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x46
                    && bytes[8] == 0x57 && bytes[9] == 0x45 && bytes[10] == 0x42 && bytes[11] == 0x50;
            default -> false;
        };
    }
}

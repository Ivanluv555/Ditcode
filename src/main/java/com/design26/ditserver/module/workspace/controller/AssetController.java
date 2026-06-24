package com.design26.ditserver.module.workspace.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * AssetController - 静态资源上传与获取接口（持久化至本地磁盘）。
 *
 * 功能：
 *  - POST /api/assets/{name}  : 接收 multipart/form-data 的文件（字段名 file），将文件保存到 data/assets/{name}。
 *  - GET  /api/assets/{name}  : 返回已保存的文件二进制流，自动探测 Content-Type，并添加 Cache-Control 头以便浏览器缓存。
 *
 * 说明：文件保存在应用工作目录下的 data/assets（物理磁盘），不是内存缓存。GET 支持可选参数 ?width= 用作预留（当前为 stub，不做图片缩放）。
 */
@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final Path storageDir = Paths.get("data", "assets").toAbsolutePath().normalize();

    public AssetController() {
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new RuntimeException("无法创建资产存储目录: " + storageDir, e);
        }
    }

    @PostMapping("/{name}")
    public Map<String, Object> uploadAsset(@PathVariable("name") String name, @RequestParam("file") MultipartFile file) {
        String safeName = Paths.get(name).getFileName().toString(); // 防止路径成分
        if (safeName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的文件名");
        }

        Path target = storageDir.resolve(safeName).normalize();
        // 防止越过 storageDir
        if (!target.startsWith(storageDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的文件名");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "保存文件失败");
        }

        return Map.of("ok", true, "url", "/api/assets/" + safeName);
    }

    @GetMapping("/{name}")
    public ResponseEntity<Resource> getAsset(
        @PathVariable("name") String name,
        @RequestParam(value = "width", required = false) Integer width
    ) {
        // width 参数目前为 stub（不做缩放），保留以便未来实现按需缩放/缩略图
        String safeName = Paths.get(name).getFileName().toString();
        if (safeName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的文件名");
        }

        Path target = storageDir.resolve(safeName).normalize();
        if (!target.startsWith(storageDir) || !Files.exists(target)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "资源未找到");
        }

        try {
            Resource resource = new UrlResource(target.toUri());
            String contentType = Files.probeContentType(target);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            return ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=86400")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "无法读取资源");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "无法读取文件类型");
        }
    }
}

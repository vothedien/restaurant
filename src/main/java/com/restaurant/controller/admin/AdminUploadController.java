package com.restaurant.controller.admin;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.restaurant.dto.admin.UploadImageResponse;
import com.restaurant.service.admin.ImgBbService;

@RestController
@RequestMapping("/api/admin/uploads")
public class AdminUploadController {

    private final ImgBbService imgBbService;

    public AdminUploadController(ImgBbService imgBbService) {
        this.imgBbService = imgBbService;
    }

    @PostMapping(value = "/menu-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadImageResponse uploadMenuImage(@RequestPart("file") MultipartFile file) {
        String url = imgBbService.upload(file);
        return new UploadImageResponse(url);
    }
}

package com.karakoc.scraper.cloudflare;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/r2/files")
@AllArgsConstructor
public class R2FileController {
private final R2Service r2Service;


//    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
      return r2Service.uploadFile(file);
    }



}

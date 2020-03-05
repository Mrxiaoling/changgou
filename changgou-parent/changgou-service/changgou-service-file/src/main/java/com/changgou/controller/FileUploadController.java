package com.changgou.controller;

import com.changgou.file.FastDFSFile;
import com.changgou.util.FastDFSFUtil;
import entity.Result;
import entity.StatusCode;
import org.csource.common.MyException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/upload")
@CrossOrigin
public class FileUploadController {
    @PostMapping
    public Result upload(@RequestParam(value = "file")MultipartFile file) throws Exception {
        //调用FSDF工具类将文件传入FastDSFF
        FastDFSFile fastDFSFile = new FastDFSFile(file.getOriginalFilename(),file.getBytes(), StringUtils.getFilenameExtension(file.getOriginalFilename()));
        String[] upload = FastDFSFUtil.upload(fastDFSFile);
        //拼接访问地址 URL=http://192.168.1.111:8080/.......
        String url = "http://192.168.1.111:8080/" + upload[0]+"/" + upload[1];
        System.out.println(url);
        return new Result(true, StatusCode.OK,"上传成功");
    }
}

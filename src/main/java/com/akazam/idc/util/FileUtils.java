package com.akazam.idc.util;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 */
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * @param file     文件
     * @param path     文件存放路径
     * @param fileName 原文件名
     * @return 结果
     */
    public static boolean upload(MultipartFile file, String path, String fileName) {

        // 生成新的文件名
//        String realPath = path + "/" + FileNameUtils.getFileName(fileName);

        //使用原文件名
        String realPath = path + "/" + fileName;

        File dest = new File(realPath);

        //判断文件父目录是否存在
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdir();
        }

        try {
            //保存文件
            file.transferTo(dest);
            return true;
        } catch (IllegalStateException e) {
            logger.error("", e);
            return false;
        } catch (IOException e) {
            logger.error("", e);
            return false;
        }

    }
}

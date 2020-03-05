package com.changgou.util;

import com.changgou.file.FastDFSFile;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FastDFSFUtil {
    static {
        try{
            String path = new ClassPathResource("fdfs_client.conf").getPath();
            ClientGlobal.init(path);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 文件上传
     * @param fastDFSFile  上传文件的信息封装
     * @return
     */
    public static String[] upload(FastDFSFile fastDFSFile) throws Exception{
        //附加参数，可选
        NameValuePair[] meteList = new NameValuePair[1];
        meteList[0] = new NameValuePair("author",fastDFSFile.getAuthor());
        //创建一个Tracker访问的客户端对象TrackerClient
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient访问TrackerServer服务，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();
        //通过TrackerServerde的连接信息可以获取Storage的链接信息，创建StorageClient对象存储Storage的链接信息
        StorageClient storageClient = new StorageClient(trackerServer,null);
        //通过StorageClient访问Storage，实现文件上传，并且获取文件上传后的存储信息
        /**
         * 第三个参数是附加信息
         * upload_file[]
         *  upload_file[0] 文件所存储的storage的组名字
         *  upload_file[1] 文件存储在storage的文件名字
         */
        String[] upload_file = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meteList);

        return upload_file;
    }

    /**
     * @param groupName 文件的组名
     * @param remoteFilename 文件的存储的路径名字
     */
    public static FileInfo getFile(String groupName , String remoteFilename) throws IOException, MyException {
//        try {
//            //获取StorageClient对象
//            StorageClient storageClient = getStorageClient();
//            //获取文件信息
//            return storageClient.get_file_info(groupName,remoteFileName);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;

        //创建一个trackerClient对象，通过trackerClient对象访问trackerserver
        TrackerClient trackerClient = new TrackerClient();
        //通过trackerClient获取TrackerService的链接对象
        TrackerServer trackerServer = trackerClient.getConnection();
        //通过TrackerService获取storage的信息，创建storageclient对象存储storage信息
        StorageClient storageClient = new StorageClient(trackerServer,null);
        return storageClient.get_file_info(groupName, remoteFilename);
    }

    /***
     * 文件下载
     * @param groupName:组名
     * @param remoteFileName：文件存储完整名
     * @return
     */
    public static InputStream downFile(String groupName, String remoteFileName) throws IOException, MyException {
        //创建一个trackerClient对象，通过trackerClient对象访问trackerserver
        TrackerClient trackerClient = new TrackerClient();
        //通过trackerClient获取TrackerService的链接对象
        TrackerServer trackerServer = trackerClient.getConnection();
        //通过TrackerService获取storage的信息，创建storageclient对象存储storage信息
        StorageClient storageClient = new StorageClient(trackerServer,null);
        byte[] bytes = storageClient.download_file(groupName, remoteFileName);
        return new ByteArrayInputStream(bytes);
    }

    /***
     * 文件删除实现
     * @param groupName:组名
     * @param remoteFileName：文件存储完整名
     */
    public static void deleteFile(String groupName,String remoteFileName) throws IOException, MyException {
        //创建一个trackerClient对象，通过trackerClient对象访问trackerserver
        TrackerClient trackerClient = new TrackerClient();
        //通过trackerClient获取TrackerService的链接对象
        TrackerServer trackerServer = trackerClient.getConnection();
        //通过TrackerService获取storage的信息，创建storageclient对象存储storage信息
        StorageClient storageClient = new StorageClient(trackerServer,null);
        storageClient.delete_file(groupName, remoteFileName);
    }

    /***
     * 获取组信息
     * @param groupName :组名
     */
    public static StorageServer getStorages(String groupName) throws IOException {
        //创建一个trackerClient对象，通过trackerClient对象访问trackerserver
        TrackerClient trackerClient = new TrackerClient();
        //通过trackerClient获取TrackerService的链接对象
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerClient.getStoreStorage(trackerServer);
    }

    /***
     * 根据文件组名和文件存储路径获取Storage服务的IP、端口信息
     * @param groupName :组名
     * @param remoteFileName ：文件存储完整名
     */
    public static ServerInfo[] getServerInfo(String groupName, String remoteFileName) throws IOException {
//        try {
//            //创建TrackerClient对象
//            TrackerClient trackerClient = new TrackerClient();
//            //通过TrackerClient获取TrackerServer对象
//            TrackerServer trackerServer = trackerClient.getConnection();
//            //获取服务信息
//            return trackerClient.getFetchStorages(trackerServer,groupName,remoteFileName);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
        TrackerClient trackerClient = new TrackerClient();
        //通过trackerClient获取TrackerService的链接对象
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);

    }

    /***
     * 获取Tracker服务地址
     */
    public static String getTrackerUrl(){
        try {
            //创建TrackerClient对象
            TrackerClient trackerClient = new TrackerClient();
            //通过TrackerClient获取TrackerServer对象
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取Tracker地址
            return "http://"+trackerServer.getInetSocketAddress().getHostString()+":"+ClientGlobal.getG_tracker_http_port();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /***
     * 获取TrackerServer,是上面一些通过操作的抽取，用于简化，优化代码
     */
    public static TrackerServer getTrackerServer() throws Exception{
        //创建TrackerClient对象
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient获取TrackerServer对象
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerServer;
    }

    /***
     * 获取StorageClient 是上面一些通过操作的抽取，用于简化，优化代码
     * @return
     * @throws Exception
     */
    public static StorageClient getStorageClient() throws Exception{
        //获取TrackerServer
        TrackerServer trackerServer = getTrackerServer();
        //通过TrackerServer创建StorageClient
        StorageClient storageClient = new StorageClient(trackerServer,null);
        return storageClient;
    }


}

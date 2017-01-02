package com.entity;

import java.util.List;

/**
 * Created by xinshuhao on 16/7/24.
 */
public class Banner {

    /**
     * code : 0
     * message : 获取列表成功
     * data : [{"id":"1","url":"http://idting.com/Files/upload/20150906/55ec15071de56.jpg"},{"id":"2","url":"http://idting.com/Files/upload/20150909/55f0007a56b64.jpg"},{"id":"3","url":"http://idting.com/Files/upload/20150922/560119a4c645e.jpg"},{"id":"4","url":"http://idting.com/Files/upload/20150906/55ec146c8e7d2.jpg"},{"id":"5","url":"http://idting.com/Files/upload/20150909/55f0031578788.jpg"}]
     */

    private int code;
    private String message;
    private List<DataEntity> data;

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(List<DataEntity> data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<DataEntity> getData() {
        return data;
    }

    public static class DataEntity {
        /**
         * id : 1
         * url : http://idting.com/Files/upload/20150906/55ec15071de56.jpg
         */

        private String id;
        private String url;

        public void setId(String id) {
            this.id = id;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }
    }
}

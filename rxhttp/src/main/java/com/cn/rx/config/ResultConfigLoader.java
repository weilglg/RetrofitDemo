package com.cn.rx.config;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultConfigLoader {

    private static Config config;

    private final static String CONFIG_NAME = "result-config.json";

    /**
     * 初始化
     */
    public static void init(Context context) {
        loadConfig(context);
    }

    private static void loadConfig(Context context) {
        if (config != null) {
            return;
        }
        String jsonStr = loadFromAssets(context, CONFIG_NAME);
        if (TextUtils.isEmpty(jsonStr)) {
            return;
        }
        jsonStr = jsonStr.replace("\r\n","");
        config = JSON.parseObject(jsonStr, Config.class);
    }

    /**
     * 获取自定义失败对应的说明信息
     */
    private static HashMap<String, String> getErrorConfig() {
        return config.getErrorInfo();
    }

    public static boolean checkErrorCode(int errorCode) {
        return getErrorConfig() != null && getErrorConfig().containsKey(String.valueOf(errorCode));
    }

    public static String errorDesc(int errorCode) {
        if (checkErrorCode(errorCode)) {
            getErrorConfig().get(String.valueOf(errorCode));
        }
        return "未知错误";
    }

    public static String getMsgKey() {
        if (config == null) {
            return "msg";
        }
        return config.getMsgKey();
    }

    /**
     * 获取状态码对应的键
     */
    public static String getCodeKey() {
        if (config == null) {
            return "code";
        }
        return config.getCodeKey();
    }

    /**
     * 数据对应的键
     */
    public static List<String> getDataKey() {
        if (config == null) {
            List<String> dataKeyList = new ArrayList<>();
            dataKeyList.add("data");
            return dataKeyList;
        }
        return config.getDataKey();
    }

    /**
     * 判断是否请求成功
     */
    public static boolean checkSuccess(String code) {
        return config == null || config.getSuccessCode().contains(code);
    }


    private static String loadFromAssets(Context context, String fileName) {
        BufferedReader reader = null;
        try {
            InputStream in = context.getResources().getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(in));

            char[] buf = new char[1024];
            int count = 0;
            StringBuilder sb = new StringBuilder(in.available());
            while ((count = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, count);
                sb.append(readData);
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "";
    }

    public static class Config {

        private List<String> successCode;
        private String codeKey;
        private List<String> dataKey;
        private String msgKey;
        private HashMap<String, String> errorInfo;

        public List<String> getSuccessCode() {
            return successCode;
        }

        public void setSuccessCode(List<String> successCode) {
            this.successCode = successCode;
        }

        public String getCodeKey() {
            return codeKey;
        }

        public void setCodeKey(String codeKey) {
            this.codeKey = codeKey;
        }

        public List<String> getDataKey() {
            return dataKey;
        }

        public void setDataKey(List<String> dataKey) {
            this.dataKey = dataKey;
        }

        public String getMsgKey() {
            return msgKey;
        }

        public void setMsgKey(String msgKey) {
            this.msgKey = msgKey;
        }

        public HashMap<String, String> getErrorInfo() {
            return errorInfo;
        }

        public void setErrorInfo(HashMap<String, String> errorInfo) {
            this.errorInfo = errorInfo;
        }
    }

}

package com.retrofit.network;

public class Demo {

    private String status;
    private String msg;
    private String results;
    private boolean success;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "Demo{" +
                "status='" + status + '\'' +
                ", msg='" + msg + '\'' +
                ", results='" + results + '\'' +
                ", success=" + success +
                '}';
    }
}

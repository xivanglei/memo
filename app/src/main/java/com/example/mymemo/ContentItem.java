package com.example.mymemo;

/**
 * Created by xianglei on 2018/1/3.
 */

public class ContentItem {
    private String date;
    private String content;
    private int id;
    private long remindDate;
    private int isRemind;
    private int isLayUp;

    public ContentItem(String date, String content, int id, long remindDate, int isRemind) {
        this.date = date;
        this.content = content;
        this.id = id;
        this.remindDate = remindDate;
        this.isRemind = isRemind;
    }

    public ContentItem(String date, String content, int id, long remindData, int isRemind, int isLayUp) {
        this.date = date;
        this.content = content;
        this.id = id;
        this.remindDate = remindDate;
        this.isRemind = isRemind;
        this.isLayUp = isLayUp;
    }

    public int getIsRemind() {
        return isRemind;
    }

    public void setIsRemind(int isRemind) {
        this.isRemind = isRemind;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getRemindDate() {
        return remindDate;
    }

    public void setRemindDate(long remindDate) {
        this.remindDate = remindDate;
    }
    public int getIsLayUp() {
        return isLayUp;
    }
    public void setIsLayUp(int isLayUp) {
        this.isLayUp = isLayUp;
    }
}

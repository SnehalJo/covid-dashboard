package com.covid.dashboard.model;


import lombok.Data;

@Data
public class GitTree {
    private String path;
    private String mode;
    private String type;
    private String sha;
    private Integer size;
    private String url;
}

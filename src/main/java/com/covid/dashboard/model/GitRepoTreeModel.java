package com.covid.dashboard.model;

import lombok.Data;

import java.util.List;

@Data
public class GitRepoTreeModel {

    private String sha;
    private String url;
    private List<GitTree> tree;
    private boolean truncated;
}

package com.matrix_maeny.foodrecipes.posts;

public class PostModel {

    private String title, tagLine, ingredients, procedure, imageUrl, additionalIns = "";
    private String username = "";
    private String userUid = "";
    private int likes = 0, comments = 0;




    public PostModel() {
    }

    public PostModel(String userUid,String username,String title, String tagLine, String ingredients, String procedure, String imageUrl, String additionalIns) {
        this.userUid = userUid;
        this.username = username;
        this.title = title;
        this.tagLine = tagLine;
        this.ingredients = ingredients;
        this.procedure = procedure;
        this.imageUrl = imageUrl;
        this.additionalIns = additionalIns;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTagLine() {
        return tagLine;
    }

    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAdditionalIns() {
        return additionalIns;
    }

    public void setAdditionalIns(String additionalIns) {
        this.additionalIns = additionalIns;
    }
}

package com.example.resumebuilder.dto;

public class UserProfileForm {
    private String fullName;
    private String jobTitle;
    private String graduationSchool;
    private String educationLevel;
    private String phone;
    private String contactEmail;
    private String location;
    private String website;
    private String defaultAvatarDataUri;
    private Boolean removeDefaultAvatar = false;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getGraduationSchool() { return graduationSchool; }
    public void setGraduationSchool(String graduationSchool) { this.graduationSchool = graduationSchool; }
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getDefaultAvatarDataUri() { return defaultAvatarDataUri; }
    public void setDefaultAvatarDataUri(String defaultAvatarDataUri) { this.defaultAvatarDataUri = defaultAvatarDataUri; }
    public Boolean getRemoveDefaultAvatar() { return removeDefaultAvatar; }
    public void setRemoveDefaultAvatar(Boolean removeDefaultAvatar) { this.removeDefaultAvatar = removeDefaultAvatar; }
}

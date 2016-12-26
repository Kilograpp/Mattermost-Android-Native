package com.kilogramm.mattermost.model.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Evgeny on 25.07.2016.
 */
public class ClientCfg extends RealmObject {

    @PrimaryKey
    private long id;
    @SerializedName("AboutLink")
    @Expose
    private String aboutLink;
    @SerializedName("AllowCorsFrom")
    @Expose
    private String allowCorsFrom;
    @SerializedName("BuildDate")
    @Expose
    private String buildDate;
    @SerializedName("BuildEnterpriseReady")
    @Expose
    private String buildEnterpriseReady;
    @SerializedName("BuildHash")
    @Expose
    private String buildHash;
    @SerializedName("BuildNumber")
    @Expose
    private String buildNumber;
    @SerializedName("EnableCommands")
    @Expose
    private String enableCommands;
    @SerializedName("EnableDeveloper")
    @Expose
    private String enableDeveloper;
    @SerializedName("EnableIncomingWebhooks")
    @Expose
    private String enableIncomingWebhooks;
    @SerializedName("EnableOAuthServiceProvider")
    @Expose
    private String enableOAuthServiceProvider;
    @SerializedName("EnableOnlyAdminIntegrations")
    @Expose
    private String enableOnlyAdminIntegrations;
    @SerializedName("EnableOpenServer")
    @Expose
    private String enableOpenServer;
    @SerializedName("EnableOutgoingWebhooks")
    @Expose
    private String enableOutgoingWebhooks;
    @SerializedName("EnablePostIconOverride")
    @Expose
    private String enablePostIconOverride;
    @SerializedName("EnablePostUsernameOverride")
    @Expose
    private String enablePostUsernameOverride;
    @SerializedName("EnablePublicLink")
    @Expose
    private String enablePublicLink;
    @SerializedName("EnableSignInWithEmail")
    @Expose
    private String enableSignInWithEmail;
    @SerializedName("EnableSignInWithUsername")
    @Expose
    private String enableSignInWithUsername;
    @SerializedName("EnableSignUpWithEmail")
    @Expose
    private String enableSignUpWithEmail;
    @SerializedName("EnableSignUpWithGitLab")
    @Expose
    private String enableSignUpWithGitLab;
    @SerializedName("EnableSignUpWithGoogle")
    @Expose
    private String enableSignUpWithGoogle;
    @SerializedName("EnableTeamCreation")
    @Expose
    private String enableTeamCreation;
    @SerializedName("EnableUserCreation")
    @Expose
    private String enableUserCreation;
    @SerializedName("FeedbackEmail")
    @Expose
    private String feedbackEmail;
    @SerializedName("GoogleDeveloperKey")
    @Expose
    private String googleDeveloperKey;
    @SerializedName("HelpLink")
    @Expose
    private String helpLink;
    @SerializedName("PrivacyPolicyLink")
    @Expose
    private String privacyPolicyLink;
    @SerializedName("ProfileHeight")
    @Expose
    private String profileHeight;
    @SerializedName("ProfileWidth")
    @Expose
    private String profileWidth;
    @SerializedName("ReportAProblemLink")
    @Expose
    private String reportAProblemLink;
    @SerializedName("RequireEmailVerification")
    @Expose
    private String requireEmailVerification;
    @SerializedName("RestrictDirectMessage")
    @Expose
    private String restrictDirectMessage;
    @SerializedName("RestrictTeamNames")
    @Expose
    private String restrictTeamNames;
    @SerializedName("SegmentDeveloperKey")
    @Expose
    private String segmentDeveloperKey;
    @SerializedName("SendEmailNotifications")
    @Expose
    private String sendEmailNotifications;
    @SerializedName("ShowEmailAddress")
    @Expose
    private String showEmailAddress;
    @SerializedName("SiteName")
    @Expose
    private String siteName;
    @SerializedName("SupportEmail")
    @Expose
    private String supportEmail;
    @SerializedName("TermsOfServiceLink")
    @Expose
    private String termsOfServiceLink;
    @SerializedName("Version")
    @Expose
    private String version;
    @SerializedName("WebsocketPort")
    @Expose
    private String websocketPort;
    @SerializedName("WebsocketSecurePort")
    @Expose
    private String websocketSecurePort;

    /**
     * @return The aboutLink
     */
    public String getAboutLink() {
        return aboutLink;
    }

    /**
     * @param aboutLink The AboutLink
     */
    public void setAboutLink(String aboutLink) {
        this.aboutLink = aboutLink;
    }

    /**
     * @return The allowCorsFrom
     */
    public String getAllowCorsFrom() {
        return allowCorsFrom;
    }

    /**
     * @param allowCorsFrom The AllowCorsFrom
     */
    public void setAllowCorsFrom(String allowCorsFrom) {
        this.allowCorsFrom = allowCorsFrom;
    }

    /**
     * @return The buildDate
     */
    public String getBuildDate() {
        return buildDate;
    }

    /**
     * @param buildDate The BuildDate
     */
    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

    /**
     * @return The buildEnterpriseReady
     */
    public String getBuildEnterpriseReady() {
        return buildEnterpriseReady;
    }

    /**
     * @param buildEnterpriseReady The BuildEnterpriseReady
     */
    public void setBuildEnterpriseReady(String buildEnterpriseReady) {
        this.buildEnterpriseReady = buildEnterpriseReady;
    }

    /**
     * @return The buildHash
     */
    public String getBuildHash() {
        return buildHash;
    }

    /**
     * @param buildHash The BuildHash
     */
    public void setBuildHash(String buildHash) {
        this.buildHash = buildHash;
    }

    /**
     * @return The buildNumber
     */
    public String getBuildNumber() {
        return buildNumber;
    }

    /**
     * @param buildNumber The BuildNumber
     */
    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    /**
     * @return The enableCommands
     */
    public String getEnableCommands() {
        return enableCommands;
    }

    /**
     * @param enableCommands The EnableCommands
     */
    public void setEnableCommands(String enableCommands) {
        this.enableCommands = enableCommands;
    }

    /**
     * @return The enableDeveloper
     */
    public String getEnableDeveloper() {
        return enableDeveloper;
    }

    /**
     * @param enableDeveloper The EnableDeveloper
     */
    public void setEnableDeveloper(String enableDeveloper) {
        this.enableDeveloper = enableDeveloper;
    }

    /**
     * @return The enableIncomingWebhooks
     */
    public String getEnableIncomingWebhooks() {
        return enableIncomingWebhooks;
    }

    /**
     * @param enableIncomingWebhooks The EnableIncomingWebhooks
     */
    public void setEnableIncomingWebhooks(String enableIncomingWebhooks) {
        this.enableIncomingWebhooks = enableIncomingWebhooks;
    }

    /**
     * @return The enableOAuthServiceProvider
     */
    public String getEnableOAuthServiceProvider() {
        return enableOAuthServiceProvider;
    }

    /**
     * @param enableOAuthServiceProvider The EnableOAuthServiceProvider
     */
    public void setEnableOAuthServiceProvider(String enableOAuthServiceProvider) {
        this.enableOAuthServiceProvider = enableOAuthServiceProvider;
    }

    /**
     * @return The enableOnlyAdminIntegrations
     */
    public String getEnableOnlyAdminIntegrations() {
        return enableOnlyAdminIntegrations;
    }

    /**
     * @param enableOnlyAdminIntegrations The EnableOnlyAdminIntegrations
     */
    public void setEnableOnlyAdminIntegrations(String enableOnlyAdminIntegrations) {
        this.enableOnlyAdminIntegrations = enableOnlyAdminIntegrations;
    }

    /**
     * @return The enableOpenServer
     */
    public String getEnableOpenServer() {
        return enableOpenServer;
    }

    /**
     * @param enableOpenServer The EnableOpenServer
     */
    public void setEnableOpenServer(String enableOpenServer) {
        this.enableOpenServer = enableOpenServer;
    }

    /**
     * @return The enableOutgoingWebhooks
     */
    public String getEnableOutgoingWebhooks() {
        return enableOutgoingWebhooks;
    }

    /**
     * @param enableOutgoingWebhooks The EnableOutgoingWebhooks
     */
    public void setEnableOutgoingWebhooks(String enableOutgoingWebhooks) {
        this.enableOutgoingWebhooks = enableOutgoingWebhooks;
    }

    /**
     * @return The enablePostIconOverride
     */
    public String getEnablePostIconOverride() {
        return enablePostIconOverride;
    }

    /**
     * @param enablePostIconOverride The EnablePostIconOverride
     */
    public void setEnablePostIconOverride(String enablePostIconOverride) {
        this.enablePostIconOverride = enablePostIconOverride;
    }

    /**
     * @return The enablePostUsernameOverride
     */
    public String getEnablePostUsernameOverride() {
        return enablePostUsernameOverride;
    }

    /**
     * @param enablePostUsernameOverride The EnablePostUsernameOverride
     */
    public void setEnablePostUsernameOverride(String enablePostUsernameOverride) {
        this.enablePostUsernameOverride = enablePostUsernameOverride;
    }

    /**
     * @return The enablePublicLink
     */
    public String getEnablePublicLink() {
        return enablePublicLink;
    }

    /**
     * @param enablePublicLink The EnablePublicLink
     */
    public void setEnablePublicLink(String enablePublicLink) {
        this.enablePublicLink = enablePublicLink;
    }

    /**
     * @return The enableSignInWithEmail
     */
    public String getEnableSignInWithEmail() {
        return enableSignInWithEmail;
    }

    /**
     * @param enableSignInWithEmail The EnableSignInWithEmail
     */
    public void setEnableSignInWithEmail(String enableSignInWithEmail) {
        this.enableSignInWithEmail = enableSignInWithEmail;
    }

    /**
     * @return The enableSignInWithUsername
     */
    public String getEnableSignInWithUsername() {
        return enableSignInWithUsername;
    }

    /**
     * @param enableSignInWithUsername The EnableSignInWithUsername
     */
    public void setEnableSignInWithUsername(String enableSignInWithUsername) {
        this.enableSignInWithUsername = enableSignInWithUsername;
    }

    /**
     * @return The enableSignUpWithEmail
     */
    public String getEnableSignUpWithEmail() {
        return enableSignUpWithEmail;
    }

    /**
     * @param enableSignUpWithEmail The EnableSignUpWithEmail
     */
    public void setEnableSignUpWithEmail(String enableSignUpWithEmail) {
        this.enableSignUpWithEmail = enableSignUpWithEmail;
    }

    /**
     * @return The enableSignUpWithGitLab
     */
    public String getEnableSignUpWithGitLab() {
        return enableSignUpWithGitLab;
    }

    /**
     * @param enableSignUpWithGitLab The EnableSignUpWithGitLab
     */
    public void setEnableSignUpWithGitLab(String enableSignUpWithGitLab) {
        this.enableSignUpWithGitLab = enableSignUpWithGitLab;
    }

    /**
     * @return The enableSignUpWithGoogle
     */
    public String getEnableSignUpWithGoogle() {
        return enableSignUpWithGoogle;
    }

    /**
     * @param enableSignUpWithGoogle The EnableSignUpWithGoogle
     */
    public void setEnableSignUpWithGoogle(String enableSignUpWithGoogle) {
        this.enableSignUpWithGoogle = enableSignUpWithGoogle;
    }

    /**
     * @return The enableTeamCreation
     */
    public String getEnableTeamCreation() {
        return enableTeamCreation;
    }

    /**
     * @param enableTeamCreation The EnableTeamCreation
     */
    public void setEnableTeamCreation(String enableTeamCreation) {
        this.enableTeamCreation = enableTeamCreation;
    }

    /**
     * @return The enableUserCreation
     */
    public String getEnableUserCreation() {
        return enableUserCreation;
    }

    /**
     * @param enableUserCreation The EnableUserCreation
     */
    public void setEnableUserCreation(String enableUserCreation) {
        this.enableUserCreation = enableUserCreation;
    }

    /**
     * @return The feedbackEmail
     */
    public String getFeedbackEmail() {
        return feedbackEmail;
    }

    /**
     * @param feedbackEmail The FeedbackEmail
     */
    public void setFeedbackEmail(String feedbackEmail) {
        this.feedbackEmail = feedbackEmail;
    }

    /**
     * @return The googleDeveloperKey
     */
    public String getGoogleDeveloperKey() {
        return googleDeveloperKey;
    }

    /**
     * @param googleDeveloperKey The GoogleDeveloperKey
     */
    public void setGoogleDeveloperKey(String googleDeveloperKey) {
        this.googleDeveloperKey = googleDeveloperKey;
    }

    /**
     * @return The helpLink
     */
    public String getHelpLink() {
        return helpLink;
    }

    /**
     * @param helpLink The HelpLink
     */
    public void setHelpLink(String helpLink) {
        this.helpLink = helpLink;
    }

    /**
     * @return The privacyPolicyLink
     */
    public String getPrivacyPolicyLink() {
        return privacyPolicyLink;
    }

    /**
     * @param privacyPolicyLink The PrivacyPolicyLink
     */
    public void setPrivacyPolicyLink(String privacyPolicyLink) {
        this.privacyPolicyLink = privacyPolicyLink;
    }

    /**
     * @return The profileHeight
     */
    public String getProfileHeight() {
        return profileHeight;
    }

    /**
     * @param profileHeight The ProfileHeight
     */
    public void setProfileHeight(String profileHeight) {
        this.profileHeight = profileHeight;
    }

    /**
     * @return The profileWidth
     */
    public String getProfileWidth() {
        return profileWidth;
    }

    /**
     * @param profileWidth The ProfileWidth
     */
    public void setProfileWidth(String profileWidth) {
        this.profileWidth = profileWidth;
    }

    /**
     * @return The reportAProblemLink
     */
    public String getReportAProblemLink() {
        return reportAProblemLink;
    }

    /**
     * @param reportAProblemLink The ReportAProblemLink
     */
    public void setReportAProblemLink(String reportAProblemLink) {
        this.reportAProblemLink = reportAProblemLink;
    }

    /**
     * @return The requireEmailVerification
     */
    public String getRequireEmailVerification() {
        return requireEmailVerification;
    }

    /**
     * @param requireEmailVerification The RequireEmailVerification
     */
    public void setRequireEmailVerification(String requireEmailVerification) {
        this.requireEmailVerification = requireEmailVerification;
    }

    /**
     * @return The restrictDirectMessage
     */
    public String getRestrictDirectMessage() {
        return restrictDirectMessage;
    }

    /**
     * @param restrictDirectMessage The RestrictDirectMessage
     */
    public void setRestrictDirectMessage(String restrictDirectMessage) {
        this.restrictDirectMessage = restrictDirectMessage;
    }

    /**
     * @return The restrictTeamNames
     */
    public String getRestrictTeamNames() {
        return restrictTeamNames;
    }

    /**
     * @param restrictTeamNames The RestrictTeamNames
     */
    public void setRestrictTeamNames(String restrictTeamNames) {
        this.restrictTeamNames = restrictTeamNames;
    }

    /**
     * @return The segmentDeveloperKey
     */
    public String getSegmentDeveloperKey() {
        return segmentDeveloperKey;
    }

    /**
     * @param segmentDeveloperKey The SegmentDeveloperKey
     */
    public void setSegmentDeveloperKey(String segmentDeveloperKey) {
        this.segmentDeveloperKey = segmentDeveloperKey;
    }

    /**
     * @return The sendEmailNotifications
     */
    public String getSendEmailNotifications() {
        return sendEmailNotifications;
    }

    /**
     * @param sendEmailNotifications The SendEmailNotifications
     */
    public void setSendEmailNotifications(String sendEmailNotifications) {
        this.sendEmailNotifications = sendEmailNotifications;
    }

    /**
     * @return The showEmailAddress
     */
    public String getShowEmailAddress() {
        return showEmailAddress;
    }

    /**
     * @param showEmailAddress The ShowEmailAddress
     */
    public void setShowEmailAddress(String showEmailAddress) {
        this.showEmailAddress = showEmailAddress;
    }

    /**
     * @return The siteName
     */
    public String getSiteName() {
        return siteName;
    }

    /**
     * @param siteName The SiteName
     */
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    /**
     * @return The supportEmail
     */
    public String getSupportEmail() {
        return supportEmail;
    }

    /**
     * @param supportEmail The SupportEmail
     */
    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    /**
     * @return The termsOfServiceLink
     */
    public String getTermsOfServiceLink() {
        return termsOfServiceLink;
    }

    /**
     * @param termsOfServiceLink The TermsOfServiceLink
     */
    public void setTermsOfServiceLink(String termsOfServiceLink) {
        this.termsOfServiceLink = termsOfServiceLink;
    }

    /**
     * @return The version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version The Version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return The websocketPort
     */
    public String getWebsocketPort() {
        return websocketPort;
    }

    /**
     * @param websocketPort The WebsocketPort
     */
    public void setWebsocketPort(String websocketPort) {
        this.websocketPort = websocketPort;
    }

    /**
     * @return The websocketSecurePort
     */
    public String getWebsocketSecurePort() {
        return websocketSecurePort;
    }

    /**
     * @param websocketSecurePort The WebsocketSecurePort
     */
    public void setWebsocketSecurePort(String websocketSecurePort) {
        this.websocketSecurePort = websocketSecurePort;
    }

}
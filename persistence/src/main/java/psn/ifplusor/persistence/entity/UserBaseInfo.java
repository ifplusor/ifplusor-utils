package psn.ifplusor.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class UserBaseInfo implements java.io.Serializable {
    @Column(name = "ID", length = 50)
    private String id;

	@Column(name = "URL", length = 100)
    private String url;

    @Column(name = "USERID", length = 50)
    private String userid;

    @Column(name = "screen_name", length = 50)
    private String screenName;

	@Column(name = "provice", length = 30)
	private String province;

	@Column(name = "city", length = 30)
	private String city;

    @Column(name = "description", length = 300)
    private String description;

	@Column(name = "gender", length = 10)
	private String gender;

	@Column(name = "follower_count", length = 12)
	private String followersCount;

	@Column(name = "friends_count", length = 12)
	private String friendsCount;

	@Column(name = "statuses_count", length = 12)
	private String statusesCount;

    @Column(name = "first_statustime", length = 50)
    private String firstStatustime;

    @Column(name = "end_statustime", length = 50)
    private String endStatustime;

	@Column(name = "verified", length = 10)
	private String verified;

	@Column(name = "verified_type", length = 10)
	private String verifiedType;

	@Column(name = "verified_reason", length = 20)
	private String verifiedReason;

	@Column(name = "verified_level", length = 3)
	private String verifiedLevel;

	@Column(name = "description_local", length = 10)
	private String descriptionLocal;

	@Column(name = "description_school", length = 50)
	private String descriptionSchool;

	@Column(name = "description_Message", length = 300)
	private String descriptionMessage;

    @Column(name = "description_Mobile", length = 50)
    private String descriptionMobile;

    @Column(name = "description_EMAIL", length = 50)
    private String descriptionEmail;

	@Column(name = "description_ADDTIME", length = 50)
	private String descriptionAddtime;

    @Column(name = "description_QQ", length = 50)
    private String descriptionQq;

	@Column(name = "description_TAGS", length = 200)
	private String descriptionTags;

	@Column(name = "description_BLOGURL",length = 100)
	private String descriptionBlogurl;

	@Column(name = "description_nameid", length = 100)
	private String descriptionNameid;

	@Column(name = "description_other", length = 100)
	private String descriptionOther;

	@Column(name = "wechatname", length = 100)
	private String wechatname;

    @Column(name = "wechatnum", length = 100)
    private String wechatnum;

	@Column(name = "USERTYPE", length = 10)
	private String usertype;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(String followersCount) {
        this.followersCount = followersCount;
    }

    public String getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(String friendsCount) {
        this.friendsCount = friendsCount;
    }

    public String getStatusesCount() {
        return statusesCount;
    }

    public void setStatusesCount(String statusesCount) {
        this.statusesCount = statusesCount;
    }

    public String getFirstStatustime() {
        return firstStatustime;
    }

    public void setFirstStatustime(String firstStatustime) {
        this.firstStatustime = firstStatustime;
    }

    public String getEndStatustime() {
        return endStatustime;
    }

    public void setEndStatustime(String endStatustime) {
        this.endStatustime = endStatustime;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getVerifiedType() {
        return verifiedType;
    }

    public void setVerifiedType(String verifiedType) {
        this.verifiedType = verifiedType;
    }

    public String getVerifiedReason() {
        return verifiedReason;
    }

    public void setVerifiedReason(String verifiedReason) {
        this.verifiedReason = verifiedReason;
    }

    public String getVerifiedLevel() {
        return verifiedLevel;
    }

    public void setVerifiedLevel(String verifiedLevel) {
        this.verifiedLevel = verifiedLevel;
    }

    public String getDescriptionLocal() {
        return descriptionLocal;
    }

    public void setDescriptionLocal(String descriptionLocal) {
        this.descriptionLocal = descriptionLocal;
    }

    public String getDescriptionSchool() {
        return descriptionSchool;
    }

    public void setDescriptionSchool(String descriptionSchool) {
        this.descriptionSchool = descriptionSchool;
    }

    public String getDescriptionMessage() {
        return descriptionMessage;
    }

    public void setDescriptionMessage(String descriptionMessage) {
        this.descriptionMessage = descriptionMessage;
    }

    public String getDescriptionMobile() {
        return descriptionMobile;
    }

    public void setDescriptionMobile(String descriptionMobile) {
        this.descriptionMobile = descriptionMobile;
    }

    public String getDescriptionEmail() {
        return descriptionEmail;
    }

    public void setDescriptionEmail(String descriptionEmail) {
        this.descriptionEmail = descriptionEmail;
    }

    public String getDescriptionAddtime() {
        return descriptionAddtime;
    }

    public void setDescriptionAddtime(String descriptionAddtime) {
        this.descriptionAddtime = descriptionAddtime;
    }

    public String getDescriptionQq() {
        return descriptionQq;
    }

    public void setDescriptionQq(String descriptionQq) {
        this.descriptionQq = descriptionQq;
    }

    public String getDescriptionTags() {
        return descriptionTags;
    }

    public void setDescriptionTags(String descriptionTags) {
        this.descriptionTags = descriptionTags;
    }

    public String getDescriptionBlogurl() {
        return descriptionBlogurl;
    }

    public void setDescriptionBlogurl(String descriptionBlogurl) {
        this.descriptionBlogurl = descriptionBlogurl;
    }

    public String getDescriptionNameid() {
        return descriptionNameid;
    }

    public void setDescriptionNameid(String descriptionNameid) {
        this.descriptionNameid = descriptionNameid;
    }

    public String getDescriptionOther() {
        return descriptionOther;
    }

    public void setDescriptionOther(String descriptionOther) {
        this.descriptionOther = descriptionOther;
    }

    public String getWechatname() {
        return wechatname;
    }

    public void setWechatname(String wechatname) {
        this.wechatname = wechatname;
    }

    public String getWechatnum() {
        return wechatnum;
    }

    public void setWechatnum(String wechatnum) {
        this.wechatnum = wechatnum;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }
}
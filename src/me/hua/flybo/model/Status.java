package me.hua.flybo.model;

import java.util.Date;

import me.hua.flybo.model.Source;
import me.hua.flybo.model.Status;
import me.hua.flybo.model.User;
import me.hua.flybo.model.Visible;

public class Status {
	private User user = null;                            //作者信息(在需要使用的地方通过status.setUser( new User());来创建)
	private String createdAt;                              //status创建时间
	private String id;                                   //status id
	private String mid;                                  //微博MID
	private long idstr;                                  //保留字段，请勿使用                     
	private String text;                                 //微博内容
	private String source;                               //微博来源
	private boolean favorited;                           //是否已收藏
	private boolean truncated;
	private long inReplyToStatusId;                      //回复ID
	private long inReplyToUserId;                        //回复人ID
	private String inReplyToScreenName;                  //回复人昵称
	private String thumbnailPic;                         //微博内容中的图片的缩略地址
	private String bmiddlePic;                           //中型图片
	private String originalPic;                          //原始图片
	private Status retweetedStatus = null;               //转发的博文，内容为status，如果不是转发，则没有此字段
	private Geo geo = null;                                  //地理信息，保存经纬度，没有时不返回此字段
	private double latitude = -1;                        //纬度
	private double longitude = -1;                       //经度
	private int repostsCount;                            //转发数
	private int commentsCount;                           //评论数
	private String annotations;                          //元数据，没有时不返回此字段
	private int mlevel;
	private Visible visible;
	private boolean hasImage;							//是否包含图片
	private boolean hasRetweetedStatus; 				//是否有转发

	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public long getIdstr() {
		return idstr;
	}
	public void setIdstr(long idstr) {
		this.idstr = idstr;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public boolean isFavorited() {
		return favorited;
	}
	public void setFavorited(boolean favorited) {
		this.favorited = favorited;
	}
	public boolean isTruncated() {
		return truncated;
	}
	public void setTruncated(boolean truncated) {
		this.truncated = truncated;
	}
	public long getInReplyToStatusId() {
		return inReplyToStatusId;
	}
	public void setInReplyToStatusId(long inReplyToStatusId) {
		this.inReplyToStatusId = inReplyToStatusId;
	}
	public long getInReplyToUserId() {
		return inReplyToUserId;
	}
	public void setInReplyToUserId(long inReplyToUserId) {
		this.inReplyToUserId = inReplyToUserId;
	}
	public String getInReplyToScreenName() {
		return inReplyToScreenName;
	}
	public void setInReplyToScreenName(String inReplyToScreenName) {
		this.inReplyToScreenName = inReplyToScreenName;
	}
	public String getThumbnailPic() {
		return thumbnailPic;
	}
	public void setThumbnailPic(String thumbnailPic) {
		this.thumbnailPic = thumbnailPic;
	}
	public String getBmiddlePic() {
		return bmiddlePic;
	}
	public void setBmiddlePic(String bmiddlePic) {
		this.bmiddlePic = bmiddlePic;
	}
	public String getOriginalPic() {
		return originalPic;
	}
	public void setOriginalPic(String originalPic) {
		this.originalPic = originalPic;
	}
	public Status getRetweetedStatus() {
		return retweetedStatus;
	}
	public void setRetweetedStatus(Status retweetedStatus) {
		this.retweetedStatus = retweetedStatus;
	}
	public Geo getGeo() {
		return geo;
	}
	public void setGeo(Geo geo) {
		this.geo = geo;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public int getRepostsCount() {
		return repostsCount;
	}
	public void setRepostsCount(int repostsCount) {
		this.repostsCount = repostsCount;
	}
	public int getCommentsCount() {
		return commentsCount;
	}
	public void setCommentsCount(int commentsCount) {
		this.commentsCount = commentsCount;
	}
	public String getAnnotations() {
		return annotations;
	}
	public void setAnnotations(String annotations) {
		this.annotations = annotations;
	}
	public int getMlevel() {
		return mlevel;
	}
	public void setMlevel(int mlevel) {
		this.mlevel = mlevel;
	}
	public Visible getVisible() {
		return visible;
	}
	public void setVisible(Visible visible) {
		this.visible = visible;
	}
	public boolean isHasImage() {
		return hasImage;
	}
	public void setHasImage(boolean hasImage) {
		this.hasImage = hasImage;
	}
	public boolean isHasRetweetedStatus() {
		return hasRetweetedStatus;
	}
	public void setHasRetweetedStatus(boolean hasRetweetedStatus) {
		this.hasRetweetedStatus = hasRetweetedStatus;
	}
	
	
}

package my.anlights.data;

import java.util.Date;

/**
 * Created by Andreas on 09.06.13.
 */
public class HueUser {

	private Date lastUseDate;
	private Date createdDate;
	private String name;
	private String id;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getLastUseDate() {
		return lastUseDate;
	}

	public void setLastUseDate(Date lastUseDate) {
		this.lastUseDate = lastUseDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

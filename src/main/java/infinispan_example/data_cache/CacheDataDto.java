package infinispan_example.data_cache;

import java.util.Date;

import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Resolution;

/**
 * An example DTO which is to be stored in the cache and used in searches
 */
@Indexed
public class CacheDataDto {
	private int id;
	@Field @DateBridge(resolution=Resolution.SECOND) private Date someDate;
	private String description;
	
	public CacheDataDto(int id, Date date, String description) {
		this.id = id;
		this.someDate = date;
		this.description = description;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Date getSomeDate() {
		return someDate;
	}
	public void setSomeDate(Date someDate) {
		this.someDate = someDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}

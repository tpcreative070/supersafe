package co.tpcreative.supersafe.model;
import androidx.annotation.NonNull;
import java.util.Date;

public class HeaderItem extends ListItem {

	@NonNull
	private Date date;

	public HeaderItem(@NonNull Date date) {
		this.date = date;
	}

	@NonNull
	public Date getDate() {
		return date;
	}

	// here getters and setters
	// for title and so on, built
	// using date

	@Override
	public int getType() {
		return TYPE_HEADER;
	}

	@Override
	public EnumEvent getTypeEvent() {
		return null;
	}
}
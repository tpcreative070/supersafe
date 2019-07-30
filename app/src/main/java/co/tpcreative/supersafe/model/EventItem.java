package co.tpcreative.supersafe.model;
import androidx.annotation.NonNull;

public class EventItem extends ListItem {

	@NonNull
	private Event event;
	@NonNull EnumEvent enumEvent;


	public EventItem(@NonNull Event event,@NonNull EnumEvent enumEvent) {
		this.event = event;
		this.enumEvent = enumEvent;
	}

	@NonNull
	public Event getEvent() {
		return event;
	}

	// here getters and setters
	// for title and so on, built
	// using event

	@Override
	public int getType() {
		return TYPE_EVENT;
	}

	@Override
	public EnumEvent getTypeEvent() {
		return enumEvent;
	}

}
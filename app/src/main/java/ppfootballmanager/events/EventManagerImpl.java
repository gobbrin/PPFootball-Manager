package ppfootballmanager.events;

import com.ppstudios.footballmanager.api.contracts.event.IEvent;
import com.ppstudios.footballmanager.api.contracts.event.IEventManager;
import com.ppstudios.footballmanager.api.contracts.event.IGoalEvent;

/**
 * Implementation of IEventManager interface for managing match events
 */
public class EventManagerImpl implements IEventManager {
    private static final int MAX_EVENTS = 100; // Maximum number of events to store
    private IEvent[] events; // Array to store events
    private int eventCount; // Number of events currently stored

    /**
     * Constructor initializes an empty events array
     */
    public EventManagerImpl() {
        this.events = new IEvent[MAX_EVENTS];
        this.eventCount = 0;
    }

    /**
     * Constructor with custom max events capacity
     * 
     * @param maxEvents Maximum number of events this manager can store
     */
    public EventManagerImpl(int maxEvents) {
        this.events = new IEvent[maxEvents];
        this.eventCount = 0;
    }

    /**
     * Adds an event to the manager
     * 
     * @param event The event to add
     * @throws IllegalArgumentException if event is null
     * @throws IllegalStateException    if the event is already stored
     */
    @Override
    public void addEvent(IEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        // Check if the event is already stored
        for (int i = 0; i < eventCount; i++) {
            if (event.equals(events[i])) {
                throw new IllegalStateException("Event is already stored");
            }
        }

        if (eventCount >= events.length) {
            // If array is full, expand it
            expandEventArray();
        }

        events[eventCount++] = event;
    }

    /**
     * Gets the number of events currently stored
     * 
     * @return The number of events
     */
    @Override
    public int getEventCount() {
        return eventCount;
    }

    /**
     * Gets all events stored in the manager
     * 
     * @return Array of events
     */
    @Override
    public IEvent[] getEvents() {
        // Create a new array of the correct size to return
        IEvent[] result = new IEvent[eventCount];

        // Copy only the non-null events
        System.arraycopy(events, 0, result, 0, eventCount);

        return result;
    }

/**
 * Gets events filtered by type
 * 
 * @param eventType The type of events to filter
 * @return Array of filtered events
 */
public IEvent[] getEventsByType(EventType eventType) {
    if (eventType == null) {
        throw new IllegalArgumentException("Event type cannot be null");
    }

    // Count matching events
    int matchCount = 0;
    for (int i = 0; i < eventCount; i++) {
        if (events[i] != null && matchesEventType(events[i], eventType)) {
            matchCount++;
        }
    }

    // Create result array
    IEvent[] result = new IEvent[matchCount];
    
    // Fill result array
    int resultIndex = 0;
    for (int i = 0; i < eventCount; i++) {
        if (events[i] != null && matchesEventType(events[i], eventType)) {
            result[resultIndex++] = events[i];
        }
    }
    
    return result;
}

/**
 * Determines if an event matches a given event type
 */
private boolean matchesEventType(IEvent event, EventType eventType) {
    if (event == null || eventType == null) {
        return false;
    }
    
    // First check specialized interfaces
    if (eventType == EventType.GOAL && event instanceof IGoalEvent) {
        return true;
    }
    
    // Then check our implementation base class
    if (event instanceof EventImpl) {
        return ((EventImpl) event).getType() == eventType;
    }
    
    // Fallback to description-based matching
    String description = event.getDescription().toUpperCase();
    
    switch (eventType) {
        case GOAL: return description.contains("GOAL");
        case YELLOW_CARD: return description.contains("YELLOW CARD");
        case RED_CARD: return description.contains("RED CARD");
        // Add other event types...
        default: return false;
    }
}

    /**
     * Gets events that occurred within a specific minute range
     * 
     * @param startMinute The start minute (inclusive)
     * @param endMinute   The end minute (inclusive)
     * @return Array of events within the minute range
     */
    public IEvent[] getEventsByMinuteRange(int startMinute, int endMinute) {
        if (startMinute < 0 || endMinute < startMinute) {
            throw new IllegalArgumentException("Invalid minute range");
        }

        // Count matching events
        int matchCount = 0;
        for (int i = 0; i < eventCount; i++) {
            if (events[i] != null && events[i].getMinute() >= startMinute &&
                    events[i].getMinute() <= endMinute) {
                matchCount++;
            }
        }

        // Create result array
        IEvent[] result = new IEvent[matchCount];

        // Fill result array
        int resultIndex = 0;
        for (int i = 0; i < eventCount; i++) {
            if (events[i] != null && events[i].getMinute() >= startMinute &&
                    events[i].getMinute() <= endMinute) {
                result[resultIndex++] = events[i];
            }
        }

        return result;
    }

    /**
     * Clears all events from the manager
     */
    public void clearEvents() {
        for (int i = 0; i < events.length; i++) {
            events[i] = null;
        }
        eventCount = 0;
    }

    /**
     * Expands the events array when it gets full
     */
    private void expandEventArray() {
        // Create a new array with double the size
        IEvent[] newEvents = new IEvent[events.length * 2];

        // Copy all existing events to the new array
        System.arraycopy(events, 0, newEvents, 0, events.length);

        // Replace the old array with the new one
        events = newEvents;
    }

    /**
     * Sorts events by minute
     */
    public void sortEventsByMinute() {
        // Simple bubble sort implementation
        for (int i = 0; i < eventCount - 1; i++) {
            for (int j = 0; j < eventCount - i - 1; j++) {
                if (events[j].getMinute() > events[j + 1].getMinute()) {
                    // Swap events
                    IEvent temp = events[j];
                    events[j] = events[j + 1];
                    events[j + 1] = temp;
                }
            }
        }
    }

    /**
     * Returns a string representation of all events
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Event Manager: " + eventCount + " events\n");

        // Sort events by minute before displaying
        sortEventsByMinute();

        for (int i = 0; i < eventCount; i++) {
            if (events[i] != null) {
                sb.append(events[i].toString()).append("\n");
            }
        }

        return sb.toString();
    }
}
package gtp.projecttracker.model.mongodb;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Document class representing an audit log entry in the project tracking system.
 * 
 * An audit log records actions performed on entities within the system, such as
 * creation, updates, and deletions. Each log entry includes information about the
 * action type, the entity affected, when the action occurred, who performed it,
 * and additional payload data related to the action.
 * 
 * This class is stored in MongoDB rather than a relational database to allow for
 * flexible schema and efficient storage of high-volume audit data.
 */
@Document(collection = "audit_logs")
public class AuditLog {
    @Id
    private ObjectId id;

    private ActionType actionType;
    private String entityType;
    private String entityId;
    private Instant timestamp = Instant.now();
    private String actorName;
    private String payload;

    // Security-specific fields
    private String ipAddress;
    private String userAgent;
    private String endpoint;

    /**
     * Enumeration of possible audit action types.
     * These types represent the different kinds of actions that can be audited.
     */
    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE,
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        LOGOUT,
        ACCESS_DENIED,
        REGISTRATION_SUCCESS,
        REGISTRATION_FAILURE,
        INVALID_TOKEN
    }

    /**
     * Gets the unique identifier of the audit log entry.
     *
     * @return The audit log's unique identifier
     */
    public ObjectId getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the audit log entry.
     * This method is typically used by MongoDB and not in application code.
     *
     * @param id The unique identifier to set
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    /**
     * Gets the type of action recorded in this audit log entry.
     *
     * @return The action type
     * @see ActionType
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * Sets the type of action for this audit log entry.
     *
     * @param actionType The action type to set
     * @see ActionType
     */
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    /**
     * Gets the type of entity that was affected by the audited action.
     * This is typically the class name of the entity.
     *
     * @return The entity type
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Sets the type of entity that was affected by the audited action.
     *
     * @param entityType The entity type to set
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    /**
     * Gets the identifier of the specific entity that was affected by the audited action.
     *
     * @return The entity identifier
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Sets the identifier of the specific entity that was affected by the audited action.
     *
     * @param entityId The entity identifier to set
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * Gets the timestamp when the audited action occurred.
     *
     * @return The timestamp of the action
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp when the audited action occurred.
     *
     * @param now The timestamp to set
     */
    public void setTimestamp(Instant now) {
        this.timestamp = now;
    }

    /**
     * Gets the name of the actor (user or system) that performed the audited action.
     *
     * @return The actor's name
     */
    public String getActorName() {
        return actorName;
    }

    /**
     * Sets the name of the actor that performed the audited action.
     *
     * @param actorName The actor's name to set
     */
    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    /**
     * Gets the additional payload data associated with the audited action.
     * This typically contains details about the changes made to the entity.
     *
     * @return The payload data
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Sets the additional payload data associated with the audited action.
     *
     * @param payload The payload data to set
     */
    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}

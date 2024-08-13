package org.carlspring.strongbox.event.artifact;

/**
 * @author carlspring
 */
public enum ArtifactEventTypeEnum
{

    /**
     * Occurs when a directory has been created.
     *
     * TODO: Not yet implemented.
     */
    EVENT_ARTIFACT_DIRECTORY_CREATED(1),

    /**
     * Occurs when an artifact has commenced an upload operation.
     */
    EVENT_ARTIFACT_FILE_UPLOADING(2),

    /**
     * Occurs when an artifact metadata file has been stored.
     */
    EVENT_ARTIFACT_METADATA_STORED(7),

    /**
     * Occurs when an artifact metadata file has been updated.
     */
    EVENT_ARTIFACT_FILE_DOWNLOADING(8),

    /**
     * Occurs when an artifact download operation has completed.
     */
    EVENT_ARTIFACT_FILE_DOWNLOADED(9),

    /**
     * Occurs when an artifact file has been updated.
     */
    EVENT_ARTIFACT_FILE_UPDATED(10),

    /**
     * Occurs when an artifact has commenced an copy operation.
     */
    EVENT_ARTIFACT_FILE_COPYING(11),

    /**
     * Occurs when an artifact copy operation has completed.
     */
    EVENT_ARTIFACT_FILE_COPIED(12),

    /**
     * Occurs when an artifact has commenced a move operation.
     */
    EVENT_ARTIFACT_FILE_MOVING(13),

    /**
     * Occurs when an artifact file has been moved.
     */
    EVENT_ARTIFACT_FILE_MOVED(14),

    /**
     * Occurs when an artifact file has commenced a archived archiving operation.
     */
    EVENT_ARTIFACT_FILE_ARCHIVING(15),

    /**
     * Occurs when an artifact file has been archived.
     */
    EVENT_ARTIFACT_FILE_ARCHIVED(15),

    /**
     * Occurs when an artifact file has been deleted.
     */
    EVENT_ARTIFACT_PATH_DELETED(16),

    /**
     * Occurs when an artifact metadata download operation has completed.
     */
    EVENT_ARTIFACT_METADATA_DOWNLOADED(17),

    /**
     * Occurs when an artifact metadata has commenced an download operation.
     */
    EVENT_ARTIFACT_METADATA_DOWNLOADING(18),

    /**
     * Occurs when an artifact checksum download operation has completed.
     */
    EVENT_ARTIFACT_CHECKSUM_DOWNLOADED(19),

    /**
     * Occurs when an artifact checksum has commenced an download operation.
     */
    EVENT_ARTIFACT_CHECKSUM_DOWNLOADING(20),

    /**
     * Occurs when an artifact has been fetched from the remote repository.
     */
    EVENT_ARTIFACT_FILE_FETCHED_FROM_REMOTE(21),

    /**
     * Occurs when an artifact file has been physically stored.
     */
    EVENT_ARTIFACT_FILE_STORED(22);


    private int type;


    ArtifactEventTypeEnum(int type)
    {
        this.type = type;
    }

    public int getType()
    {
        return type;
    }

}

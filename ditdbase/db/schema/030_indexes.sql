-- Recommended indexes for core queries

CREATE INDEX IF NOT EXISTS idx_archives_owner_updated_desc
    ON archives (owner_id, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_archive_messages_archive_created_asc
    ON archive_messages (archive_id, created_at ASC);

CREATE INDEX IF NOT EXISTS idx_archive_tasks_archive_created_desc
    ON archive_tasks (archive_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_community_publications_active_updated_desc
    ON community_publications (is_active, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_community_publications_owner_created_desc
    ON community_publications (owner_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_remix_events_publication_created_desc
    ON remix_events (publication_id, created_at DESC);

-- At most one task with an image per archive (first-round image rule)
CREATE UNIQUE INDEX IF NOT EXISTS uq_archive_tasks_one_image_per_archive
    ON archive_tasks (archive_id)
    WHERE image_name IS NOT NULL OR image_preview IS NOT NULL;

